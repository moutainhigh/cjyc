package com.cjyc.common.system.service.impl;

import com.cjkj.common.redis.lock.RedisDistributedLock;
import com.cjkj.common.redis.template.StringRedisUtil;
import com.cjyc.common.model.dao.*;
import com.cjyc.common.model.dto.web.waybill.*;
import com.cjyc.common.model.entity.*;
import com.cjyc.common.model.enums.AdminStateEnum;
import com.cjyc.common.model.enums.SendNoTypeEnum;
import com.cjyc.common.model.enums.city.CityLevelEnum;
import com.cjyc.common.model.enums.order.OrderCarLocalStateEnum;
import com.cjyc.common.model.enums.order.OrderCarStateEnum;
import com.cjyc.common.model.enums.order.OrderCarTrunkStateEnum;
import com.cjyc.common.model.enums.order.OrderStateEnum;
import com.cjyc.common.model.enums.task.TaskStateEnum;
import com.cjyc.common.model.enums.waybill.*;
import com.cjyc.common.model.exception.ParameterException;
import com.cjyc.common.model.exception.ServerException;
import com.cjyc.common.model.keys.RedisKeys;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.util.LocalDateTimeUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.entity.defined.FullCity;
import com.cjyc.common.system.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;

/**
 * 运单业务类
 *
 * @author JPG
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CsWaybillServiceImpl implements ICsWaybillService {
    @Resource
    private IWaybillDao waybillDao;
    @Resource
    private IOrderCarDao orderCarDao;
    @Resource
    private IOrderDao orderDao;
    @Resource
    private StringRedisUtil redisUtil;
    @Resource
    private RedisDistributedLock redisLock;
    @Resource
    private ICsSendNoService sendNoService;
    @Resource
    private ICarrierDao carrierDao;
    @Resource
    private IAdminDao adminDao;
    @Resource
    private IWaybillCarDao waybillCarDao;
    @Resource
    private IDriverDao driverDao;
    @Resource
    private ITaskDao taskDao;
    @Resource
    private ITaskCarDao taskCarDao;
    @Resource
    private ICsCityService csCityService;
    @Resource
    private IVehicleRunningDao vehicleRunningDao;
    @Resource
    private ICsStoreService csStoreService;
    @Resource
    private ICsLineNodeService csLineNodeService;

    /**
     * 同城调度
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/5 17:07
     */
    @Override
    public ResultVo saveLocal(SaveLocalDto paramsDto) {
        Long currentMillisTime = System.currentTimeMillis();
        Set<String> lockSet = new HashSet<>();
        try {
            List<SaveLocalWaybillDto> list = paramsDto.getList();
            for (int i = 0; i < list.size(); i++) {
                int idx = i + 1;
                SaveLocalWaybillDto dto = list.get(i);
                if (dto == null) {
                    continue;
                }
                String orderCarNo = dto.getOrderCarNo();
                Long orderCarId = dto.getOrderCarId();
                Long carrierId = dto.getCarrierId();

                //是否分配司机任务标识
                boolean isAllotDriver = false;
                /**验证运单信息*/
                //【验证】承运商是否可以运营
                Long driverId = null;
                String driverName = null;
                if (dto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_CONSIGN.code || dto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_PILOT.code) {
                    Carrier carrier = carrierDao.selectById(carrierId);
                    if (carrier.getBusinessState() == null || carrier.getBusinessState() != 0) {
                        throw new ParameterException("序号为{0}运单,编号为{1}的车辆，所选承运商停运中", idx, orderCarNo);
                    }
                    //验证司机信息
                    if (carrier.getDriverNum() <= 1) {
                        Driver driver = driverDao.findTopByCarrierId(carrierId);
                        if (driver == null) {
                            throw new ParameterException("序号为{0}运单,编号为{1}的车辆，所选承运商没有运营中的司机", idx, orderCarNo);
                        }
                        isAllotDriver = true;
                        driverId = driver.getId();
                        driverName = driver.getName();
                    }
                } else if (dto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_ADMIN.code) {
                    Admin admin = adminDao.selectById(carrierId);
                    if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
                        throw new ParameterException("序号为{0}运单,编号为{1}的车辆，所选业务员已离职", idx, orderCarNo);
                    }
                    isAllotDriver = true;
                    driverId = admin.getId();
                    driverName = admin.getName();
                } else if (dto.getCarrierType() == WaybillCarrierTypeEnum.SELF.code) {
                    isAllotDriver = true;
                    driverId = dto.getLoadLinkUserId();
                    driverName = dto.getLoadLinkName();
                }

                /**验证运单车辆信息*/
                //加锁
                String lockKey = RedisKeys.getDispatchLock(orderCarNo);
                if (!redisLock.lock(lockKey)) {
                    throw new ParameterException("序号为{0}运单，编号为{1}的车辆，其他人正在调度", idx, orderCarNo);
                }
                lockSet.add(lockKey);

                //【验证】订单车辆状态
                OrderCar orderCar = orderCarDao.selectById(orderCarId);
                if (orderCar == null) {
                    throw new ParameterException("序号为{0}运单,编号为{1}的车辆，车辆所属订单车辆不存在", idx, orderCarNo);
                }

                if (orderCar.getState() == null
                        || (paramsDto.getType() == WaybillTypeEnum.PICK.code && orderCar.getPickState() >= OrderCarLocalStateEnum.WAIT_DISPATCH.code)
                        || (paramsDto.getType() == WaybillTypeEnum.BACK.code && orderCar.getBackState() >= OrderCarLocalStateEnum.WAIT_DISPATCH.code)) {
                    throw new ParameterException("序号为{0}运单,编号为{1}的车辆，当前车辆状态不能提车/配送调度", idx, orderCarNo);
                }
                //【验证】订单状态
                Order order = orderDao.selectById(orderCar.getOrderId());
                if (order == null) {
                    throw new ParameterException("序号为{0}运单,编号为{1}的车辆，当前车辆不存在", idx, orderCarNo);
                }
                if (order.getState() == null
                        || order.getState() < OrderStateEnum.CHECKED.code
                        || order.getState() > OrderStateEnum.FINISHED.code) {
                    throw new ParameterException("序号为{0}运单,编号为{1}的车辆，车辆所属订单状态不能提车/配送调度", idx, orderCarNo);
                }
                //【验证】是否调度过，提送车只能有效执行一次
                if (paramsDto.getType() == WaybillTypeEnum.PICK.code) {
                    int n = waybillDao.countWaybill(orderCarId, WaybillTypeEnum.PICK.code);
                    if (n > 0) {
                        throw new ParameterException("序号为{0}运单,编号为{1}的车辆，已经调度过提车运单", idx, orderCarNo);
                    }
                } else if (paramsDto.getType() == WaybillTypeEnum.BACK.code) {
                    int n = waybillDao.countWaybill(orderCarId, WaybillTypeEnum.BACK.code);
                    if (n > 0) {
                        throw new ParameterException("序号为{0}运单,编号为{1}的车辆，已经调度过送车运单", idx, orderCarNo);
                    }
                } else {
                    throw new ParameterException("序号为{0}运单,编号为{1}的车辆，无法识别的运单类型", idx, orderCarNo);
                }

                //【验证】配送调度，需验证干线调度是否完成
                if (paramsDto.getType() == WaybillTypeEnum.BACK.code) {
                    WaybillCar waybillCar = waybillCarDao.findLastTrunkWaybillCar(order.getEndCityCode(), orderCarId);
                    if (!waybillCar.getEndCityCode().equals(order.getEndCityCode())) {
                        throw new ParameterException("序号为{0}运单,编号为{1}的车辆，干线尚未调度完成", idx, orderCarNo);
                    }
                }

                /**1、添加运单信息*/
                Waybill waybill = new Waybill();
                //发号
                waybill.setNo(sendNoService.getNo(SendNoTypeEnum.ORDER));
                waybill.setType(paramsDto.getType());
                //承运商类型
                waybill.setSource(paramsDto.getUserId().equals(carrierId) ? WaybillSourceEnum.SELF.code : WaybillSourceEnum.MANUAL.code);
                waybill.setCarrierId(carrierId);
                waybill.setCarrierType(dto.getCarrierType());
                waybill.setCarNum(1);
                waybill.setState(WaybillStateEnum.WAIT_ALLOT_CONFIRM.code);
                //提送车费用逻辑，调度时不允许修改提送车费用，需要到订单中修改提送车费用，多则返还，少则后补
                waybill.setFreightFee(orderCar.getPickFee());
                waybill.setRemark(dto.getRemark());
                waybill.setCreateTime(currentMillisTime);
                waybill.setCreateUser(paramsDto.getUserName());
                waybill.setCreateUserId(paramsDto.getUserId());
                waybillDao.insert(waybill);

                /**2、添加运单车辆信息*/
                WaybillCar waybillCar = new WaybillCar();
                //初始copy赋值
                BeanUtils.copyProperties(dto, waybillCar);
                waybillCar.setWaybillId(waybill.getId());
                waybillCar.setWaybillNo(waybill.getNo());
                waybillCar.setFreightFee(paramsDto.getType() == WaybillTypeEnum.PICK.code ? orderCar.getPickFee() : orderCar.getPickFee());
                waybillCar.setSort(1);
                //地址赋值
                FullCity startFullCity = csCityService.findFullCity(dto.getStartAreaCode(), CityLevelEnum.PROVINCE);
                copyWaybillCarStartCity(startFullCity, waybillCar);
                FullCity endFullCity = csCityService.findFullCity(dto.getEndAreaCode(), CityLevelEnum.PROVINCE);
                copyWaybillCarEndCity(endFullCity, waybillCar);
                //运单车辆状态
                waybillCar.setState(WaybillCarStateEnum.WAIT_ALLOT.code);
                if (isAllotDriver) {
                    waybillCar.setState(WaybillCarStateEnum.ALLOTED.code);
                }
                waybillCar.setExpectStartTime(dto.getExpectStartTime());
                //TODO 计算预计到达时间，计算线路是否存在
                waybillCar.setCreateTime(currentMillisTime);
                waybillCarDao.insert(waybillCar);

                /**3、添加任务信息*/
                if (isAllotDriver) {
                    //只有一个司机添加任务信息
                    Task task = new Task();
                    task.setNo(sendNoService.getNo(SendNoTypeEnum.TASK));
                    task.setWaybillId(waybill.getId());
                    task.setCarNum(1);
                    task.setState(TaskStateEnum.WAIT_ALLOT_CONFIRM.code);
                    task.setDriverId(driverId);
                    task.setDriverName(driverName);
                    //添加运力信息
                    if (dto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_ADMIN.code) {
                        VehicleRunning vehicleRunning = vehicleRunningDao.findByDriverId(driverId);
                        if (vehicleRunning != null) {
                            task.setVehicleRunningId(vehicleRunning.getId());
                            task.setVehiclePlateNo(vehicleRunning.getPlateNo());
                        }
                    }
                    task.setCreateTime(currentMillisTime);
                    task.setCreateUser(paramsDto.getUserName());
                    task.setCreateUserId(paramsDto.getUserId());
                    taskDao.insert(task);

                    /**4、插入任务车辆关联表*/
                    TaskCar taskCar = new TaskCar();
                    taskCar.setTaskId(task.getId());
                    taskCar.setWaybillCarId(waybillCar.getId());
                    taskCarDao.insert(taskCar);

                    /**5、更新订单车辆状态*/
                    orderCarDao.updatePickStateById(
                            paramsDto.getType() == WaybillTypeEnum.PICK.code ? OrderCarLocalStateEnum.DISPATCHED.code : OrderCarLocalStateEnum.DISPATCHED.code,
                            orderCarId);
                }

            }
        } finally {
            for (String key : lockSet) {
                redisLock.releaseLock(key);
            }
        }
        return BaseResultUtil.success();
    }

    /**
     * 干线调度
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/5 17:23
     */
    @Override
    public ResultVo saveTrunk(SaveTrunkWaybillDto paramsDto) {
        Long currentMillisTime = System.currentTimeMillis();
        Long userId = paramsDto.getUserId();
        Set<String> lockSet = new HashSet<>();
        //【验证参数】操作人
        Admin admin = adminDao.findByUserId(userId);
        if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
            return BaseResultUtil.fail("当前业务员，不在职");
        }
        //【验证参数】业务中心ID
        //Store store = csStoreService.getById(paramsDto.getStoreId(), true);
        try {
            List<SaveTrunkWaybillCarDto> carList = paramsDto.getList();
            if (carList == null || carList.isEmpty()) {
                return BaseResultUtil.fail("车辆列表不能为空");
            }
            Long carrierId = paramsDto.getCarrierId();

            //是否分配任务
            boolean isAllotDriver = false;
            //【验证】承运商和司机信息
            Carrier carrier = carrierDao.selectById(carrierId);
            if (carrier.getBusinessState() == null || carrier.getBusinessState() != 0) {
                return BaseResultUtil.fail("运单，所选承运商，停运中");
            }
            Driver driver = new Driver();
            if (carrier.getDriverNum() <= 1) {
                driver = driverDao.findTopByCarrierId(carrierId);
                if (driver == null) {
                    return BaseResultUtil.fail("运单，所选承运商没有运营中的司机");
                }
                isAllotDriver = true;
            }

            /**1、组装运单信息*/
            Waybill waybill = new Waybill();
            waybill.setNo(sendNoService.getNo(SendNoTypeEnum.ORDER));
            waybill.setType(WaybillTypeEnum.TRUNK.code);
            waybill.setSource(userId.equals(carrierId) ? WaybillSourceEnum.SELF.code : WaybillSourceEnum.MANUAL.code);
            waybill.setGuideLine(paramsDto.getGuideLine());
            waybill.setRecommendLine(paramsDto.getRecommendLine());
            waybill.setCarrierId(carrierId);
            waybill.setCarrierType(carrier.getType());
            waybill.setCarNum(carList.size());
            waybill.setState(WaybillStateEnum.WAIT_ALLOT_CONFIRM.code);
            //提送车费用逻辑，调度时不允许修改提送车费用，需要到订单中修改提送车费用，多则返还，少则后补
            waybill.setFreightFee(paramsDto.getFreightFee());
            waybill.setCreateTime(currentMillisTime);
            waybill.setCreateUser(admin.getName());
            waybill.setCreateUserId(admin.getId());
            waybillDao.insert(waybill);

            //承运商有且仅有一个司机
            Task task = null;
            if (isAllotDriver) {
                /**1+、写入任务表*/
                task = new Task();
                task.setNo(sendNoService.getNo(SendNoTypeEnum.TASK));
                task.setWaybillId(waybill.getId());
                task.setCarNum(carList.size());
                task.setState(TaskStateEnum.WAIT_ALLOT_CONFIRM.code);
                task.setDriverId(driver.getId());
                task.setDriverName(driver.getName());
                //查询运力
                VehicleRunning vehicleRunning = vehicleRunningDao.getVehiRunByDriverId(driver.getId());
                if (vehicleRunning != null) {
                    task.setVehicleRunningId(vehicleRunning.getId());
                    task.setVehiclePlateNo(vehicleRunning.getPlateNo());
                }
                task.setCreateTime(currentMillisTime);
                task.setCreateUser(admin.getName());
                task.setCreateUserId(admin.getCreateUserId());
                taskDao.insert(task);
            }

            /**2、运单，车辆循环*/
            //合计运费
            BigDecimal sumFreightFee = BigDecimal.ZERO;
            for (SaveTrunkWaybillCarDto dto : carList) {
                if (dto == null) {
                    continue;
                }
                String orderCarNo = dto.getOrderCarNo();
                Long orderCarId = dto.getOrderCarId();

                //加锁
                String lockKey = RedisKeys.getDispatchLock(orderCarNo);
                if (!redisLock.lock(lockKey, 20000, 100, 300L)) {
                    throw new ParameterException("运单，编号为{1}的车辆，其他人正在调度", orderCarNo);
                }
                lockSet.add(lockKey);

                //验证订单车辆状态
                OrderCar orderCar = orderCarDao.selectById(orderCarId);
                if (orderCar == null) {
                    throw new ParameterException("运单，编号为{1}的车辆，不存在", orderCarNo);
                }
                if (orderCar.getState() == null) {
                    throw new ParameterException("运单，编号为{1}的车辆，无法提车调度", orderCarNo);
                }
                //验证订单状态
                Order order = orderDao.selectById(orderCar.getOrderId());
                if (order == null) {
                    throw new ParameterException("运单，编号为{1}的车辆，所属订单车辆不存在", orderCarNo);
                }

                if (order.getState() == null
                        || order.getState() < OrderStateEnum.CHECKED.code
                        || order.getState() > OrderStateEnum.FINISHED.code) {
                    throw new ParameterException("运单，编号为{1}的车辆，所属订单状态无法干线调度", orderCarNo);
                }

                /*//验证是否已经调度过,已经调度的为
                int n = waybillCarDao.countForValidateRepeatTrunkDisPatch(areaList);
                if (n > 0) {
                    throw new ParameterException("运单，编号为{1}的车辆，已经调度过", orderCarNo);
                }

                //验证出发地与上一次调度目的地是否一致
                int sort = 1;
                WaybillCar prevWaybillCar = waybillCarDao.findLastPrevByArea(orderCarId, areaList);
                if (prevWaybillCar != null) {
                    if (!prevWaybillCar.getEndAddress().equals(dto.getStartAddress())) {
                        throw new ServerException("本次调度出发地址与上次调度结束地址不一致");
                    }
                    sort = prevWaybillCar.getSort() + 1;
                }*/

                WaybillCar waybillCar = new WaybillCar();
                BeanUtils.copyProperties(dto, waybillCar);
                waybillCar.setWaybillId(waybill.getId());
                waybillCar.setWaybillNo(waybill.getNo());
                waybillCar.setOrderCarId(orderCar.getId());
                //地址赋值
                FullCity sVo = csCityService.findFullCity(dto.getStartAreaCode(), CityLevelEnum.PROVINCE);
                FullCity eVo = csCityService.findFullCity(dto.getEndAreaCode(), CityLevelEnum.PROVINCE);
                copyWaybillCarStartCity(sVo, waybillCar);
                copyWaybillCarEndCity(eVo, waybillCar);
                waybillCar.setState(WaybillCarStateEnum.WAIT_ALLOT.code);
                if (isAllotDriver) {
                    waybillCar.setState(WaybillCarStateEnum.ALLOTED.code);
                }
                waybillCar.setTakeType(2);
                waybillCar.setCreateTime(currentMillisTime);
                waybillCarDao.insert(waybillCar);

                sumFreightFee = sumFreightFee.add(dto.getFreightFee());
                //承运商有且仅有一个司机
                if (isAllotDriver) {
                    /**2+、组装任务车辆表数据*/
                    TaskCar taskCar = new TaskCar();
                    taskCar.setTaskId(task.getId());
                    taskCar.setWaybillCarId(waybillCar.getId());
                    taskCarDao.insert(taskCar);
                }

                //更新订单车辆状态
                orderCarDao.updateTrunkStateById(orderCar.getId());
            }
        } finally {
            for (String key : lockSet) {
                redisLock.releaseLock(key);
            }
        }
        return BaseResultUtil.success();
    }

    /**
     * 取消调度
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/5 17:33
     */
    @Override

    public ResultVo cancelDispatch(CancelWaybillDto paramsDto) {
        Long userId = paramsDto.getUserId();
        //验证用户
        Admin admin = adminDao.findByUserId(userId);
        if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
            return BaseResultUtil.fail("当前业务员，不在职");
        }
        // TODO 验证用户角色
        //取消运单
        List<Long> waybillIdList = paramsDto.getWaybillIdList();
        List<Waybill> waybillList = waybillDao.findListByIds(waybillIdList);
        if (!CollectionUtils.isEmpty(waybillList)) {
            BaseResultUtil.fail("运单不存在");
        }
        for (Waybill waybill : waybillList) {
            if (waybill == null) {
                continue;
            }
            //取消运单
            cancelWaybillCar(waybill);
        }
        waybillDao.updateStateBatchByIds(WaybillStateEnum.F_CANCEL.code, waybillIdList);
        return BaseResultUtil.success();
    }

    private void cancelWaybillCar(Waybill waybill) {

        //状态不大于待承接
        if (waybill.getState() > WaybillStateEnum.WAIT_ALLOT_CONFIRM.code) {
            throw new ServerException("运单{0},当前状态不能取消", waybill);
        }
        //修改运单主单状态
        waybill.setState(WaybillStateEnum.F_CANCEL.code);
        waybillDao.updateById(waybill);
        //修改任务主单状态
        taskDao.cancelBywaybillId(TaskStateEnum.F_CANCEL.code, waybill.getId());

        //修改车辆状态
        List<OrderCar> list = orderCarDao.findListByWaybillId(waybill.getId());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (OrderCar orderCar : list) {
            if (orderCar == null) {
                continue;
            }
            updateOrderCarDispatchState(waybill, orderCar);
        }

    }

    /**
     * 修改同城运单
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/9 8:59
     */
    @Override
    public ResultVo updateLocal(UpdateLocalDto paramsDto) {
        Long currentMillisTime = System.currentTimeMillis();
        Set<String> lockSet = new HashSet<>();
        try {
            String orderCarNo = paramsDto.getCarDto().getOrderCarNo();
            Long orderCarId = paramsDto.getCarDto().getOrderCarId();
            Long carrierId = paramsDto.getCarrierId();

            //是否分配司机任务标识
            boolean isAllotDriver = false;
            /**验证运单信息*/
            //【验证】承运商是否可以运营
            Long driverId = null;
            String driverName = null;
            if (paramsDto.getCarrierType() == WaybillCarrierTypeEnum.TRUNK_ENTERPRISE.code || paramsDto.getCarrierType() == WaybillCarrierTypeEnum.TRUNK_INDIVIDUAL.code) {
                Carrier carrier = carrierDao.selectById(carrierId);
                if (carrier.getBusinessState() == null || carrier.getBusinessState() != 0) {
                    return BaseResultUtil.fail("运单,编号为{1}的车辆，所选承运商停运中", orderCarNo);
                }
                //验证司机信息
                if (carrier.getDriverNum() <= 1) {
                    Driver driver = driverDao.findTopByCarrierId(carrierId);
                    if (driver == null) {
                        return BaseResultUtil.fail("运单,编号为{1}的车辆，所选承运商没有运营中的司机", orderCarNo);
                    }
                    isAllotDriver = true;
                    driverId = driver.getId();
                    driverName = driver.getName();
                }
            } else if (paramsDto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_ADMIN.code) {
                Admin admin = adminDao.selectById(carrierId);
                if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
                    return BaseResultUtil.fail("运单,编号为{1}的车辆，所选业务员已离职", orderCarNo);
                }
                isAllotDriver = true;
                driverId = admin.getId();
                driverName = admin.getName();
            } else if (paramsDto.getCarrierType() == WaybillCarrierTypeEnum.SELF.code) {
                isAllotDriver = true;
                driverId = paramsDto.getCarDto().getLoadLinkUserId();
                driverName = paramsDto.getCarDto().getLoadLinkName();
            }

            /**验证运单车辆信息*/
            //加锁
            String lockKey = RedisKeys.getDispatchLock(orderCarNo);
            if (!redisLock.lock(lockKey)) {
                return BaseResultUtil.fail("运单，编号为{1}的车辆，其他人正在调度", orderCarNo);
            }
            lockSet.add(lockKey);

            //【验证】订单车辆状态
            OrderCar orderCar = orderCarDao.selectById(orderCarId);
            if (orderCar == null) {
                return BaseResultUtil.fail("运单,编号为{1}的车辆，车辆所属订单车辆不存在", orderCarNo);
            }
            if (orderCar.getState() == null
                    || (paramsDto.getType() == WaybillTypeEnum.PICK.code && orderCar.getPickState() <= OrderCarLocalStateEnum.WAIT_DISPATCH.code)
                    || (paramsDto.getType() == WaybillTypeEnum.BACK.code && orderCar.getBackState() <= OrderCarLocalStateEnum.WAIT_DISPATCH.code)) {
                return BaseResultUtil.fail("运单,编号为{1}的车辆，当前车辆状态不能提车/配送调度", orderCarNo);
            }
            //【验证】订单状态
            Order order = orderDao.selectById(orderCar.getId());
            if (order == null) {
                return BaseResultUtil.fail("运单,编号为{1}的车辆，当前车辆不存在", orderCarNo);
            }
            if (order.getState() == null
                    || order.getState() < OrderStateEnum.CHECKED.code
                    || order.getState() > OrderStateEnum.FINISHED.code) {
                return BaseResultUtil.fail("运单,编号为{1}的车辆，车辆所属订单状态不能提车/配送调度", orderCarNo);
            }

            /**1、添加运单信息*/
            Waybill waybill = waybillDao.selectById(paramsDto.getId());
            if (waybill.getState() > WaybillStateEnum.TRANSPORTING.code) {
                return BaseResultUtil.fail("运单已经在运输中不能修改");
            }
            waybill.setSource(paramsDto.getUserId().equals(carrierId) ? WaybillSourceEnum.SELF.code : WaybillSourceEnum.MANUAL.code);
            waybill.setCarrierId(carrierId);
            waybill.setCarrierType(paramsDto.getCarrierType());
            //提送车费用逻辑，调度时不允许修改提送车费用，需要到订单中修改提送车费用，多则返还，少则后补
            waybill.setFreightFee(orderCar.getPickFee());
            waybill.setRemark(paramsDto.getRemark());
            waybillDao.updateById(waybill);

            /**2、添加运单车辆信息*/
            WaybillCar waybillCar = waybillCarDao.selectById(paramsDto.getCarDto().getId());
            //初始copy赋值
            BeanUtils.copyProperties(paramsDto, waybillCar);
            waybillCar.setWaybillId(waybill.getId());
            waybillCar.setWaybillNo(waybill.getNo());
            waybillCar.setFreightFee(paramsDto.getType() == WaybillTypeEnum.PICK.code ? orderCar.getPickFee() : orderCar.getPickFee());
            //地址赋值
            FullCity startFullCity = csCityService.findFullCity(paramsDto.getCarDto().getStartAreaCode(), CityLevelEnum.PROVINCE);
            copyWaybillCarStartCity(startFullCity, waybillCar);
            FullCity endFullCity = csCityService.findFullCity(paramsDto.getCarDto().getEndAreaCode(), CityLevelEnum.PROVINCE);
            copyWaybillCarEndCity(endFullCity, waybillCar);
            //运单车辆状态
            waybillCar.setState(WaybillCarStateEnum.WAIT_ALLOT.code);
            if (isAllotDriver) {
                waybillCar.setState(WaybillCarStateEnum.ALLOTED.code);
            }
            waybillCar.setExpectStartTime(paramsDto.getCarDto().getExpectStartTime());
            //TODO 计算预计到达时间，计算线路是否存在
            waybillCarDao.updateById(waybillCar);

            /**3、添加任务信息*/

            Task task = taskDao.findTopByWaybillId(waybill.getId());
            if (task != null) {
                if (isAllotDriver) {
                    task.setDriverId(driverId);
                    task.setDriverName(driverName);
                    //添加运力信息
                    if (paramsDto.getCarrierType() == WaybillCarrierTypeEnum.TRUNK_INDIVIDUAL.code) {
                        VehicleRunning vehicleRunning = vehicleRunningDao.findByDriverId(driverId);
                        if (vehicleRunning != null) {
                            task.setVehicleRunningId(vehicleRunning.getId());
                            task.setVehiclePlateNo(vehicleRunning.getPlateNo());
                        }
                    }
                    taskDao.updateById(task);
                } else {
                    taskDao.deleteByWaybillId(waybill.getId());
                }
            }

        } finally {
            //redisUtil.del(new String[]{lockSet.});
        }
        return BaseResultUtil.success();
    }

    /**
     * 修改干线运单
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/9 8:59
     */
    @Override
    public ResultVo updateTrunk(UpdateTrunkWaybillDto paramsDto) {
        Long currentMillisTime = System.currentTimeMillis();
        Long userId = paramsDto.getUserId();
        Set<String> lockSet = new HashSet<>();
        //【验证参数】操作人
        Admin admin = adminDao.findByUserId(userId);
        if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
            return BaseResultUtil.fail("当前业务员，不在职");
        }
        //【验证参数】业务中心ID
        //Store store = csStoreService.getById(paramsDto.getStoreId(), true);
        //List<String> areaList = csStoreService.findAreaBizScope(store.getId());
        // TODO 验证用户角色
        try {
            Waybill waybill = waybillDao.selectById(paramsDto.getId());
            if (waybill == null) {
                return BaseResultUtil.fail("运单不存在");
            }
            if (waybill.getState() > WaybillStateEnum.TRANSPORTING.code) {
                return BaseResultUtil.fail("运单不存在");
            }

            //加锁
            String lockWaybillKey = RedisKeys.getDispatchLock(waybill.getNo());
            if (!redisLock.lock(lockWaybillKey, 20000, 100, 300L)) {
                return BaseResultUtil.fail("运单{0}，其他人正在修改", waybill.getNo());
            }
            lockSet.add(lockWaybillKey);

            List<UpdateTrunkWaybillCarDto> list = paramsDto.getList();

            Long carrierId = paramsDto.getCarrierId();
            //是否重新分配任务
            boolean isReAllotDriver = false;
            Carrier carrier = null;
            Driver driver = null;
            if (!carrierId.equals(waybill.getCarrierId())) {
                //【验证】承运商和司机信息
                carrier = carrierDao.selectById(carrierId);
                if (carrier.getBusinessState() == null || carrier.getBusinessState() != 0) {
                    return BaseResultUtil.fail("运单{0}，所选承运商，停运中", waybill.getNo());
                }
                driver = new Driver();
                if (carrier.getDriverNum() <= 1) {
                    driver = driverDao.findTopByCarrierId(carrierId);
                    if (driver == null) {
                        return BaseResultUtil.fail("运单{0}，所选承运商没有运营中的司机", waybill.getNo());
                    }
                    isReAllotDriver = true;
                }
            }

            /**1、组装运单信息*/
            waybill.setType(WaybillTypeEnum.TRUNK.code);
            waybill.setGuideLine(paramsDto.getGuideLine());
            if (isReAllotDriver) {
                waybill.setCarrierId(carrier.getId());
                waybill.setCarrierType(carrier.getType());
            }
            waybill.setCarNum(list.size());
            waybill.setState(WaybillStateEnum.WAIT_ALLOT_CONFIRM.code);
            //提送车费用逻辑，调度时不允许修改提送车费用，需要到订单中修改提送车费用，多则返还，少则后补
            waybill.setFreightFee(paramsDto.getFreightFee());
            waybill.setFixedFreightFee(paramsDto.getFixedFreightFee());
            waybill.setRemark(paramsDto.getRemark());
            waybillDao.updateById(waybill);

            //承运商有且仅有一个司机
            Task task = null;
            if (isReAllotDriver) {
                //删除任务
                taskDao.deleteByWaybillId(waybill.getId());
                /**1+、写入任务表*/
                task = new Task();
                task.setNo(sendNoService.getNo(SendNoTypeEnum.TASK));
                task.setWaybillId(waybill.getId());
                task.setCarNum(waybill.getCarNum());
                task.setState(TaskStateEnum.WAIT_ALLOT_CONFIRM.code);
                task.setDriverId(driver.getId());
                task.setDriverName(driver.getName());
                //查询运力
                VehicleRunning vehicleRunning = vehicleRunningDao.getVehiRunByDriverId(driver.getId());
                if (vehicleRunning != null) {
                    task.setVehicleRunningId(vehicleRunning.getId());
                    task.setVehiclePlateNo(vehicleRunning.getPlateNo());
                }
                task.setCreateTime(currentMillisTime);
                task.setCreateUser(admin.getName());
                task.setCreateUserId(admin.getCreateUserId());
                taskDao.insert(task);
            }

            /**2、运单，车辆循环*/
            Set<Long> unDeleteWaybillCarId = new HashSet<>();
            for (UpdateTrunkWaybillCarDto carDto : list) {
                if (carDto == null) {
                    continue;
                }
                WaybillCar waybillCar = waybillCarDao.selectById(carDto.getId());
                String orderCarNo = waybillCar.getOrderCarNo();
                Long orderCarId = waybillCar.getOrderCarId();

                //加锁
                String lockKey = RedisKeys.getDispatchLock(orderCarNo);
                if (!redisLock.lock(lockKey, 20000, 100, 300L)) {
                    throw new ParameterException("运单{0}，编号为{1}的车辆，其他人正在调度", waybill.getNo(), orderCarNo);
                }
                //包板线路不能为空
                if(waybill.getFixedFreightFee() && waybillCar.getLineId() == null){
                    throw new ParameterException("运单{0}，编号为{1}的车辆，线路不能为空", waybill.getNo(), orderCarNo);
                }
                lockSet.add(lockKey);
                //验证目的地是否发生变更

                BeanUtils.copyProperties(carDto, waybillCar);
                waybillCar.setWaybillId(waybill.getId());
                //地址赋值
                FullCity sVo = csCityService.findFullCity(carDto.getStartAreaCode(), CityLevelEnum.PROVINCE);
                FullCity eVo = csCityService.findFullCity(carDto.getEndAreaCode(), CityLevelEnum.PROVINCE);
                copyWaybillCarStartCity(sVo, waybillCar);
                copyWaybillCarEndCity(eVo, waybillCar);
                waybillCar.setState(WaybillCarStateEnum.WAIT_ALLOT.code);
                if (isReAllotDriver) {
                    waybillCar.setState(WaybillCarStateEnum.ALLOTED.code);
                }
                waybillCar.setTakeType(2);
                waybillCar.setCreateTime(currentMillisTime);
                waybillCarDao.updateById(waybillCar);

                //承运商有且仅有一个司机
                if (isReAllotDriver) {
                    /**2+、组装任务车辆表数据*/
                    //新建任务
                    TaskCar taskCar = new TaskCar();
                    taskCar.setTaskId(task.getId());
                    taskCar.setWaybillCarId(waybillCar.getId());
                    taskCarDao.insert(taskCar);
                }
            }
            //删除
            if(waybill.getId() == null || CollectionUtils.isEmpty(unDeleteWaybillCarId)){
                throw new ServerException("运单和车辆不能为空");
            }
            int num = waybillCarDao.deleteWaybillCar(waybill.getId(), unDeleteWaybillCarId);
        } finally {
            for (String key : lockSet) {
                redisLock.releaseLock(key);
            }
        }
        return BaseResultUtil.success();
    }

    /**
     * 干线运单中途完结
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/9 8:59
     */
    @Override
    public ResultVo updateTrunkMidwayFinish(UpdateTrunkMidwayFinishDto paramsDto) {
        Long waybillId = paramsDto.getWaybillId();
        Waybill waybill = waybillDao.selectById(waybillId);
        if (waybill == null) {
            return BaseResultUtil.fail("运单不存在");
        }
        if (waybill.getState() <= WaybillStateEnum.WAIT_ALLOT_CONFIRM.code || waybill.getState() >= WaybillStateEnum.FINISHED.code) {
            return BaseResultUtil.fail("运单未开始或已完结");
        }

        List<WaybillCar> carlist = waybillCarDao.findListByWaybillId(waybill.getId());
        if (CollectionUtils.isEmpty(carlist)) {
            return BaseResultUtil.fail("运单车辆不存在");
        }

        //查询完结城市
        FullCity fullCity = csCityService.findFullCity(paramsDto.getEndAreaCode(), CityLevelEnum.PROVINCE);
        //创建运单
        Waybill newWaybill = null;
        if (paramsDto.getCreateWaybillFlag()) {
            //创建新运单
            newWaybill = createMidWayWaybill(waybill, carlist, paramsDto, fullCity);
        }

        //修改运单
        waybill.setState(WaybillStateEnum.FINISHED.code);
        waybill.setFreightFee(paramsDto.getFreightFee());
        waybill.setRemark(newWaybill == null ? WaybillRemarkEnum.MIDWAY_FINISH_NONE.getMsg() : MessageFormat.format(WaybillRemarkEnum.MIDWAY_FINISH_NEW.getMsg(), newWaybill.getNo()));
        for (WaybillCar waybillCar : carlist) {
            //waybillCar.setFreightFee();
            copyWaybillCarEndCity(fullCity, waybillCar);
            waybillCar.setEndAddress(paramsDto.getEndAddress());
            if (paramsDto.getEndStoreId() == null) {
                //算起始地业务中心
                Store store = csStoreService.findOneBelongByAreaCode(waybillCar.getStartAreaCode());
                if (store != null) {
                    waybillCar.setEndStoreName(store.getName());
                    waybillCar.setEndStoreId(store.getId());
                }
            } else {
                waybillCar.setEndStoreName(paramsDto.getEndStoreName());
                waybillCar.setEndStoreId(paramsDto.getEndStoreId());
            }
            //车辆运输到中途卸车算调度单业务中心
            waybillCar.setEndBelongStoreId(waybillCar.getStartBelongStoreId());
            //交接状态如何变更
            waybillCar.setState(WaybillCarStateEnum.UNLOADED.code);
            waybillCarDao.updateById(waybillCar);
        }
        waybillDao.updateById(waybill);

        return BaseResultUtil.success();
    }

    /**
     * 中途强制卸车
     *
     * @param paramsDto
     * @author JPG
     * @since 2019/11/13 10:00
     */
    @Override
    public ResultVo trunkMidwayUnload(TrunkMidwayUnload paramsDto) {
        List<Long> carIdList = paramsDto.getWaybillCarIdList();
        if (CollectionUtils.isEmpty(carIdList)) {
            return BaseResultUtil.fail("车辆不能为空");
        }
        //查询完结城市
        FullCity fullCity = csCityService.findFullCity(paramsDto.getEndAreaCode(), CityLevelEnum.PROVINCE);
        if (fullCity == null) {
            return BaseResultUtil.fail("请填写卸车城市");
        }
        Set<Long> waybillIds = new HashSet<>();
        for (Long waybillCarId : carIdList) {
            if (waybillCarId == null) {
                continue;
            }
            WaybillCar waybillCar = waybillCarDao.selectById(waybillCarId);
            if (waybillCar == null) {
                throw new ParameterException("ID为{}的车辆不存在", waybillCarId);
            }
            if (waybillCar.getState() > WaybillCarStateEnum.CONFIRMED.code) {
                throw new ParameterException("已完成", waybillCarId);
            }

            //已装车的完成并结算
            if (waybillCar.getState() >= WaybillCarStateEnum.LOADED.code) {
                //TODO 结算
                copyWaybillCarEndCity(fullCity, waybillCar);
                waybillCar.setEndAddress(paramsDto.getEndAddress());

                Long endStoreId = paramsDto.getEndStoreId();
                String endStoreName = paramsDto.getEndStoreName();
                if (paramsDto.getEndStoreId() == null) {
                    //算起始地业务中心
                    Store store = csStoreService.findOneBelongByAreaCode(waybillCar.getStartAreaCode());
                    if (store != null) {
                        endStoreId = store.getId();
                        endStoreName = store.getName();
                    }
                }
                waybillCar.setEndStoreId(endStoreId);
                waybillCar.setEndStoreName(endStoreName);
                //车辆运输到中途卸车算调度单业务中心
                waybillCar.setEndBelongStoreId(waybillCar.getStartBelongStoreId());
                //车辆运输到中途卸车算调度单业务中心
                waybillCar.setEndBelongStoreId(waybillCar.getStartBelongStoreId());
                //交接状态如何变更
                waybillCar.setState(WaybillCarStateEnum.WAIT_CONNECT.code);
                waybillCarDao.updateById(waybillCar);
            }
            if (waybillCar.getState() < WaybillCarStateEnum.LOADED.code) {
                //删除车辆
                waybillCarDao.deleteById(waybillCarId);
                //删除任务车辆
                taskDao.deleteByWaybillCarId(waybillCarId);
            }
            waybillIds.add(waybillCarId);
        }
        //验证运单是否已经全部完成
        for (Long waybillId : waybillIds) {
            int num = waybillCarDao.countUnAllFinish(waybillId);
            if (num == 0) {
                //修改运单状态
                waybillDao.updateStateById(WaybillStateEnum.FINISHED.code, waybillId);
            }
        }
        return BaseResultUtil.success();
    }

    /**
     * @param waybill
     * @param list
     * @param paramsDto
     * @param fullCity
     * @author JPG
     * @since 2019/11/9 11:30
     */
    private Waybill createMidWayWaybill(Waybill waybill, List<WaybillCar> list, UpdateTrunkMidwayFinishDto
            paramsDto, FullCity fullCity) {
        long currentTimeMillis = System.currentTimeMillis();
        Waybill newWaybill = new Waybill();
        BeanUtils.copyProperties(waybill, newWaybill);

        newWaybill.setId(null);
        newWaybill.setNo(sendNoService.getNo(SendNoTypeEnum.WAYBILL));
        newWaybill.setSource(WaybillSourceEnum.MIDWAY_FINISH.code);
        //newWaybill.setGuideLine("");
        //newWaybill.setRecommendLine("");
        newWaybill.setCarrierId(null);
        newWaybill.setCarrierType(null);
        newWaybill.setCarrierName(null);
        newWaybill.setState(WaybillStateEnum.WAIT_ALLOT.code);
        //运费
        BigDecimal subtract = waybill.getFreightFee().subtract(paramsDto.getFreightFee());
        newWaybill.setFreightFee(subtract.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : subtract);
        newWaybill.setFixedFreightFee(false);
        newWaybill.setRemark(MessageFormat.format(WaybillRemarkEnum.MIDWAY_FINISH_CREATED.getMsg(), waybill.getNo()));
        newWaybill.setCreateTime(currentTimeMillis);
        newWaybill.setCreateUser(paramsDto.getUserName());
        newWaybill.setCreateUserId(paramsDto.getUserId());
        waybillDao.insert(newWaybill);

        for (WaybillCar waybillCar : list) {
            WaybillCar newWaybillCar = new WaybillCar();
            newWaybillCar.setId(null);
            newWaybillCar.setWaybillId(newWaybill.getId());
            newWaybillCar.setWaybillNo(newWaybill.getNo());
            //newWaybillCar.setFreightFee();
            copyWaybillCarStartCity(fullCity, newWaybillCar);
            newWaybillCar.setStartAddress(paramsDto.getEndAddress());
            if (paramsDto.getEndStoreId() == null) {
                Store store = csStoreService.findOneBelongByAreaCode(paramsDto.getEndAreaCode());
                if (store != null) {
                    newWaybillCar.setStartStoreName(store.getName());
                    newWaybillCar.setStartStoreId(store.getId());
                }
                //车辆运输到中途卸车算那个业务中心的
                waybillCar.setEndBelongStoreId(waybillCar.getStartBelongStoreId());
            } else {
                newWaybillCar.setStartStoreName(paramsDto.getEndStoreName());
                newWaybillCar.setStartStoreId(paramsDto.getEndStoreId());
            }
            newWaybillCar.setState(WaybillCarStateEnum.WAIT_ALLOT.code);
            newWaybillCar.setExpectStartTime(LocalDateTimeUtil.getMillisByLDT(LocalDateTimeUtil.getDayStartByLong(paramsDto.getUnloadTime())));
            newWaybillCar.setExpectEndTime(null);
            newWaybillCar.setTakeType(WaybillTakeTypeEnum.PICK.code);
            newWaybillCar.setLoadLinkName(paramsDto.getUserName());
            newWaybillCar.setLoadLinkUserId(paramsDto.getUserId());
            newWaybillCar.setLoadLinkPhone(paramsDto.getUserPhone());
            newWaybillCar.setLoadTurnType(WaybillCarTurnType.MIDWAY.code);
            newWaybillCar.setUnloadLinkName(waybillCar.getUnloadLinkName());
            newWaybillCar.setUnloadLinkPhone(waybillCar.getUnloadLinkPhone());
            newWaybillCar.setUnloadLinkUserId(waybillCar.getUnloadLinkUserId());
            newWaybillCar.setCreateTime(currentTimeMillis);
            waybillCarDao.insert(newWaybillCar);
        }
        return newWaybill;
    }

    /**
     * 修改订单车辆调度状态
     *
     * @param waybill
     * @param orderCar
     * @author JPG
     * @since 2019/10/29 9:57
     */
    private void updateOrderCarDispatchState(Waybill waybill, OrderCar orderCar) {
        //查询订单是否已经完结
        Order order = orderDao.selectById(orderCar.getOrderId());
        if (order == null || order.getState() >= OrderStateEnum.FINISHED.code) {
            return;
        }
        //修改车辆状态
        if (waybill.getType() == WaybillTypeEnum.PICK.code) {
            //订单车辆状态
            if (orderCar.getState() == OrderCarStateEnum.WAIT_PICK.code) {
                orderCar.setState(OrderCarStateEnum.WAIT_PICK_DISPATCH.code);
            }
            //订单车辆提车状态
            orderCar.setPickState(OrderCarLocalStateEnum.WAIT_DISPATCH.code);
        }
        if (waybill.getType() == WaybillTypeEnum.BACK.code) {
            //订单车辆状态
            if (orderCar.getState() == OrderCarStateEnum.WAIT_BACK.code) {
                orderCar.setState(OrderCarStateEnum.WAIT_BACK_DISPATCH.code);
            }
            //订单车辆配送状态
            orderCar.setBackState(OrderCarLocalStateEnum.WAIT_DISPATCH.code);
        }
        if (waybill.getType() == WaybillTypeEnum.BACK.code) {
            int trunkState = OrderCarLocalStateEnum.WAIT_DISPATCH.code;
            //根据订单车辆所有运单状态判断订单车辆干线状态
            List<Integer> waybillStateList = waybillDao.findTrunkStateListByOrderCarId(orderCar.getId());
            if (waybillStateList != null || !waybillStateList.isEmpty()) {
                if (waybillStateList.contains(WaybillStateEnum.FINISHED.code)) {
                    trunkState = OrderCarTrunkStateEnum.WAIT_NEXT_DISPATCH.code;
                    if (orderCar.getNowStoreId().equals(order.getEndStoreId())
                            || orderCar.getNowAreaCode().equals(order.getEndAreaCode())) {
                        trunkState = OrderCarTrunkStateEnum.DISPATCHED.code;
                    }
                }
                if (waybillStateList.contains(WaybillStateEnum.WAIT_ALLOT_CONFIRM.code)
                        || waybillStateList.contains(WaybillStateEnum.ALLOT_CONFIRM.code)) {
                    trunkState = OrderCarTrunkStateEnum.WAIT_NEXT_DISPATCH.code;
                }
                if (waybillStateList.contains(WaybillStateEnum.TRANSPORTING.code)) {
                    trunkState = OrderCarTrunkStateEnum.WAIT_NEXT_DISPATCH.code;
                }
            }

            //订单车辆状态
            if (orderCar.getState() == OrderCarStateEnum.WAIT_TRUNK.code) {
                orderCar.setState(OrderCarStateEnum.WAIT_TRUNK_DISPATCH.code);
            }
            //订单车辆干线状态
            orderCar.setTrunkState(trunkState);

        }
        orderCarDao.updateById(orderCar);
    }

    private void copyWaybillCarStartCity(FullCity fullCity, WaybillCar waybillCar) {
        if (fullCity == null) {
            return;
        }
        waybillCar.setStartProvince(fullCity.getProvince());
        waybillCar.setStartProvinceCode(fullCity.getProvinceCode());
        waybillCar.setStartCity(fullCity.getCity());
        waybillCar.setStartCityCode(fullCity.getCityCode());
        waybillCar.setStartArea(fullCity.getArea());
        waybillCar.setStartAreaCode(fullCity.getAreaCode());
    }

    private void copyWaybillCarEndCity(FullCity fullCity, WaybillCar waybillCar) {
        if (fullCity == null) {
            return;
        }
        waybillCar.setEndProvince(fullCity.getProvince());
        waybillCar.setEndProvinceCode(fullCity.getProvinceCode());
        waybillCar.setEndCity(fullCity.getCity());
        waybillCar.setEndCityCode(fullCity.getCityCode());
        waybillCar.setEndArea(fullCity.getArea());
        waybillCar.setEndAreaCode(fullCity.getAreaCode());
    }
}

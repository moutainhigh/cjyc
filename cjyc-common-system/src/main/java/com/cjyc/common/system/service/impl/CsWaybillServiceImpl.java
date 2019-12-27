package com.cjyc.common.system.service.impl;

import com.cjkj.common.redis.lock.RedisDistributedLock;
import com.cjkj.common.redis.template.StringRedisUtil;
import com.cjyc.common.model.dao.*;
import com.cjyc.common.model.dto.web.waybill.*;
import com.cjyc.common.model.entity.*;
import com.cjyc.common.model.entity.defined.FullCity;
import com.cjyc.common.model.entity.defined.FullWaybill;
import com.cjyc.common.model.enums.AdminStateEnum;
import com.cjyc.common.model.enums.CommonStateEnum;
import com.cjyc.common.model.enums.SendNoTypeEnum;
import com.cjyc.common.model.enums.city.CityLevelEnum;
import com.cjyc.common.model.enums.order.*;
import com.cjyc.common.model.enums.task.TaskStateEnum;
import com.cjyc.common.model.enums.waybill.*;
import com.cjyc.common.model.exception.ParameterException;
import com.cjyc.common.model.exception.ServerException;
import com.cjyc.common.model.keys.RedisKeys;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.util.LocalDateTimeUtil;
import com.cjyc.common.model.util.MoneyUtil;
import com.cjyc.common.model.util.TimeStampUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.system.service.*;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 运单业务类
 *
 * @author JPG
 */
@Slf4j
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
    private ICsTaskService csTaskService;
    @Resource
    private ICsLineService csLineService;
    @Resource
    private ICsAdminService csAdminService;

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
            for (SaveLocalWaybillDto dto : list) {
                if (dto == null) {
                    continue;
                }
                String orderCarNo = dto.getOrderCarNo();
                Long orderCarId = dto.getOrderCarId();
                Long carrierId = dto.getCarrierId();

                /**验证运单信息*/
                //是否分配司机任务标识
                boolean isOneDriver = false;
                Long driverId = null;
                String driverName = null;
                String driverPhone = null;
                if (dto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_CONSIGN.code || dto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_PILOT.code) {
                    Carrier carrier = carrierDao.selectById(carrierId);
                    if (carrier.getState() == null || carrier.getState() != CommonStateEnum.CHECKED.code || carrier.getBusinessState() == null || carrier.getBusinessState() != 0) {
                        throw new ParameterException("编号为{0}的车辆，所选承运商停运中", orderCarNo);
                    }
                    //验证司机信息
                    if (carrier.getType() == 1) {
                        Driver driver = driverDao.findTopByCarrierId(carrierId);
                        if (driver == null) {
                            throw new ParameterException("编号为{0}的车辆，所选承运商没有运营中的司机", orderCarNo);
                        }
                        isOneDriver = true;
                        driverId = driver.getId();
                        driverName = driver.getName();
                        driverPhone = driver.getPhone();
                    }
                } else if (dto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_ADMIN.code) {
                    Admin admin = adminDao.selectById(carrierId);
                    if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
                        throw new ParameterException("编号为{0}的车辆，所选业务员已离职", orderCarNo);
                    }
                    isOneDriver = true;
                    driverId = admin.getId();
                    driverName = admin.getName();
                    driverPhone = admin.getPhone();
                } else if (dto.getCarrierType() == WaybillCarrierTypeEnum.SELF.code) {
                    isOneDriver = true;
                    if (WaybillTypeEnum.PICK.code == paramsDto.getType()) {
                        driverId = dto.getLoadLinkUserId();
                        driverName = dto.getLoadLinkName();
                        driverPhone = dto.getLoadLinkPhone();
                    } else {
                        driverId = dto.getUnloadLinkUserId();
                        driverName = dto.getUnloadLinkName();
                        driverPhone = dto.getUnloadLinkPhone();
                    }
                }
                /**验证运单车辆信息*/
                //加锁
                String lockKey = RedisKeys.getDispatchLock(orderCarNo);
                if (!redisLock.lock(lockKey)) {
                    throw new ParameterException("编号为{0}的车辆，其他人正在调度", orderCarNo);
                }
                lockSet.add(lockKey);

                //【验证】订单车辆状态
                OrderCar orderCar = orderCarDao.selectById(orderCarId);
                if (orderCar == null) {
                    throw new ParameterException("编号为{0}的车辆，车辆所属订单车辆不存在", orderCarNo);
                }

                if ((paramsDto.getType() == WaybillTypeEnum.PICK.code && orderCar.getPickState() > OrderCarLocalStateEnum.WAIT_DISPATCH.code)
                        || (paramsDto.getType() == WaybillTypeEnum.BACK.code && orderCar.getBackState() > OrderCarLocalStateEnum.WAIT_DISPATCH.code)) {
                    throw new ParameterException("编号为{0}的车辆，当前车辆状态不能提车/配送调度", orderCarNo);
                }
                //【验证】订单状态
                Order order = orderDao.selectById(orderCar.getOrderId());
                if (order == null) {
                    throw new ParameterException("编号为{0}的车辆，当前车辆不存在", orderCarNo);
                }
                if (order.getState() == null
                        || order.getState() < OrderStateEnum.CHECKED.code
                        || order.getState() > OrderStateEnum.FINISHED.code) {
                    throw new ParameterException("编号为{0}的车辆，车辆所属订单未确认或已结束不能提车/配送调度", orderCarNo);
                }
                //验证提车联系人
                if (paramsDto.getType() == WaybillTypeEnum.PICK.code) {
                    if (!order.getPickContactPhone().equals(dto.getLoadLinkPhone())) {
                        throw new ParameterException("编号为{0}的车辆，提车人信息与订单不一致", orderCarNo);
                    }
                } else {
                    if (!order.getBackContactPhone().equals(dto.getUnloadLinkPhone())) {
                        throw new ParameterException("编号为{0}的车辆，收车人信息与订单不一致", orderCarNo);
                    }
                }
                //【验证】是否调度过，提送车只能有效执行一次
                if (paramsDto.getType() == WaybillTypeEnum.PICK.code) {
                    int n = waybillDao.countWaybill(orderCarId, WaybillTypeEnum.PICK.code);
                    if (n > 0) {
                        throw new ParameterException("编号为{0}的车辆，已经调度过提车运单", orderCarNo);
                    }
                } else {
                    int n = waybillDao.countWaybill(orderCarId, WaybillTypeEnum.BACK.code);
                    if (n > 0) {
                        throw new ParameterException("编号为{0}的车辆，已经调度过送车运单", orderCarNo);
                    }
                }
                //【验证】提车调度，是否已经调度干线
                /*if(paramsDto.getType() == WaybillTypeEnum.PICK.code){
                    WaybillCar waybillCar = waybillCarDao.findFirstTrunkWaybillCar(orderCarId);
                    if(waybillCar != null && !waybillCar.getStartAddress().equals(dto.getEndAddress())){
                        throw new ParameterException("编号为{0}的车辆，提车运单交车地址与第一段干线地址不一致", orderCarNo);
                    }
                }*/
                //【验证】配送调度，需验证干线调度是否完成
                if (paramsDto.getType() == WaybillTypeEnum.BACK.code) {
                    WaybillCar waybillCar = waybillCarDao.findLastTrunkWaybillCar(order.getEndCityCode(), orderCarId);
                    if (!validateIsArriveEndCity(order, waybillCar)) {
                        throw new ParameterException("编号为{0}的车辆，尚未到达目的地所属业务中心或目的地城市范围内", orderCarNo);
                    }
                }
                //TODO 验证提车和送车人是否与订单一致

                /**1、添加运单信息*/
                Waybill waybill = new Waybill();
                waybill.setNo(sendNoService.getNo(SendNoTypeEnum.WAYBILL));
                waybill.setType(paramsDto.getType());
                //承运商类型
                waybill.setSource(WaybillSourceEnum.MANUAL.code);
                waybill.setCarrierType(dto.getCarrierType());
                waybill.setCarrierId(dto.getCarrierId());
                if(dto.getCarrierType() == WaybillCarrierTypeEnum.SELF.code){
                    waybill.setCarrierName(paramsDto.getType() == WaybillTypeEnum.PICK.code ? order.getPickContactName() : order.getBackContactName());
                }else{
                    waybill.setCarrierName(dto.getCarrierName());
                }
                waybill.setCarNum(1);
                waybill.setState(dto.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? WaybillStateEnum.TRANSPORTING.code : WaybillStateEnum.ALLOT_CONFIRM.code);
                //提送车费用逻辑，调度时不允许修改提送车费用，需要到订单中修改提送车费用，多则返还，少则后补
                waybill.setFreightFee(paramsDto.getType() == WaybillTypeEnum.PICK.code ? orderCar.getPickFee() : orderCar.getPickFee());
                waybill.setRemark(dto.getRemark());
                waybill.setCreateTime(currentMillisTime);
                waybill.setCreateUser(paramsDto.getLoginName());
                waybill.setCreateUserId(paramsDto.getLoginId());
                waybill.setFixedFreightFee(false);
                waybill.setInputStoreId(paramsDto.getType() == WaybillTypeEnum.PICK.code ? order.getStartBelongStoreId() : order.getEndBelongStoreId());
                waybill.setFreightFee(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? BigDecimal.ZERO : (paramsDto.getType() == WaybillTypeEnum.PICK.code ? orderCar.getPickFee() : orderCar.getPickFee()));
                //waybill.setGuideLine(dto.);
                waybillDao.insert(waybill);

                /**2、添加运单车辆信息*/
                WaybillCar waybillCar = new WaybillCar();
                BeanUtils.copyProperties(dto, waybillCar);
                waybillCar.setWaybillId(waybill.getId());
                waybillCar.setWaybillNo(waybill.getNo());
                waybillCar.setFreightFee(waybill.getFreightFee());
                //城市信息赋值
                fillWaybillCarCityInfo(waybillCar);
                //业务中心信息赋值
                fillWaybillcarStoreInfo(waybillCar);
                //提送车业务员
                fillWaybillCarHandleAdmin(waybillCar, waybill.getType());

                //计算预计到达时间
                fillWaybillCarExpectEndTime(waybillCar);
                waybillCar.setReceiptFlag(waybillCar.getUnloadLinkPhone().equals(order.getBackContactPhone()));
                //运单车辆状态
                waybillCar.setState(isOneDriver ?
                        (waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ?
                        (waybill.getType() == WaybillTypeEnum.PICK.code ? WaybillCarStateEnum.WAIT_UNLOAD_CONFIRM.code : WaybillCarStateEnum.WAIT_LOAD_CONFIRM.code)
                        : WaybillCarStateEnum.WAIT_LOAD.code)
                        : WaybillCarStateEnum.WAIT_ALLOT.code);

                if (waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code) {
                    waybillCar.setLoadTime(currentMillisTime);
                }
                waybillCar.setCreateTime(currentMillisTime);
                waybillCarDao.insert(waybillCar);

                /**3、添加任务信息*/
                if (isOneDriver) {
                    //只有一个司机添加任务信息
                    Task task = new Task();
                    //获取编号
                    String taskNo = csTaskService.getTaskNo(waybill.getNo());
                    lockSet.add(RedisKeys.getNewTaskNoKey(waybill.getNo()));
                    if (taskNo == null) {
                        throw new ServerException("获取任务编号失败");
                    }
                    task.setNo(taskNo);
                    task.setWaybillId(waybill.getId());
                    task.setWaybillNo(waybill.getNo());
                    task.setGuideLine(waybill.getGuideLine());
                    task.setCarNum(1);
                    task.setState(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? TaskStateEnum.TRANSPORTING.code : TaskStateEnum.WAIT_LOAD.code);
                    task.setDriverId(driverId);
                    task.setDriverName(driverName);
                    task.setDriverPhone(driverPhone);
                    //添加运力信息
                    if (dto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_CONSIGN.code) {
                        VehicleRunning vehicleRunning = vehicleRunningDao.findByDriverId(driverId);
                        if (vehicleRunning != null) {
                            task.setVehicleRunningId(vehicleRunning.getId());
                            task.setVehiclePlateNo(vehicleRunning.getPlateNo());
                        }
                    }
                    task.setCreateTime(currentMillisTime);
                    task.setCreateUser(paramsDto.getLoginName());
                    task.setCreateUserId(paramsDto.getLoginId());
                    taskDao.insert(task);

                    /**4、插入任务车辆关联表*/
                    TaskCar taskCar = new TaskCar();
                    taskCar.setTaskId(task.getId());
                    taskCar.setWaybillCarId(waybillCar.getId());
                    taskCarDao.insert(taskCar);
                }

                /**5、更新订单车辆状态*/
                if (paramsDto.getType() == WaybillTypeEnum.PICK.code) {
                    OrderCar noc = new OrderCar();
                    noc.setId(orderCarId);
                    noc.setState(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? OrderCarStateEnum.PICKING.code : OrderCarStateEnum.WAIT_PICK.code);
                    noc.setPickType(getLocalCarryType(waybill.getCarrierType()));
                    noc.setPickState(OrderCarLocalStateEnum.DISPATCHED.code);
                    orderCarDao.updateById(noc);
                } else {
                    OrderCar noc = new OrderCar();
                    noc.setId(orderCarId);
                    //车辆实际运输状态
                    if (orderCar.getState() == OrderCarStateEnum.WAIT_BACK.code) {
                        noc.setState(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? OrderCarStateEnum.BACKING.code : OrderCarStateEnum.WAIT_BACK.code);
                    }
                    noc.setBackType(getLocalCarryType(waybill.getCarrierType()));
                    noc.setBackState(OrderCarLocalStateEnum.DISPATCHED.code);
                    orderCarDao.updateById(noc);
                }

            }
        } finally {
            if (!CollectionUtils.isEmpty(lockSet)) {
                redisUtil.del(lockSet.toArray(new String[0]));
            }
        }
        return BaseResultUtil.success();
    }

    private boolean validateIsArriveEndCity(Order order, WaybillCar waybillCar) {
        if (waybillCar == null) {
            return false;
        }
        //先验证是否到达所属业务中心
        if (order.getEndStoreId() != null) {
            List<Store> storeList = csStoreService.getBelongByAreaCode(waybillCar.getEndAreaCode());
            if (!CollectionUtils.isEmpty(storeList) && storeList.stream().map(Store::getId).collect(Collectors.toList()).contains(order.getEndStoreId())) {
                return true;
            }
        }
        //其次验证城市
        if (order.getEndCityCode().equals(waybillCar.getEndCityCode())) {
            return true;
        }
        return false;

    }

    private WaybillCar fillWaybillCarHandleAdmin(WaybillCar waybillCar, Integer type) {
        if (WaybillTypeEnum.BACK.code == type) {
            if (StringUtils.isBlank(waybillCar.getLoadLinkPhone())) {
                Admin admin = csAdminService.findLoop(waybillCar.getStartStoreId());
                if (admin == null) {
                    throw new ParameterException("业务中心无人员");
                }
                waybillCar.setLoadLinkUserId(admin.getId());
                waybillCar.setLoadLinkName(admin.getName());
                waybillCar.setLoadLinkPhone(admin.getPhone());
            }
        } else {

            if (StringUtils.isBlank(waybillCar.getUnloadLinkPhone())) {
                Admin admin = csAdminService.findLoop(waybillCar.getEndStoreId());
                if (admin == null) {
                    throw new ParameterException("业务中心无人员");
                }
                waybillCar.setUnloadLinkUserId(admin.getId());
                waybillCar.setUnloadLinkName(admin.getName());
                waybillCar.setUnloadLinkPhone(admin.getPhone());
            }
        }
        return waybillCar;
    }

    private Integer getLocalCarryType(Integer carrierType) {
        switch (carrierType) {
            case 3:
                return OrderPickTypeEnum.PILOT.code;
            case 4:
                return OrderPickTypeEnum.PILOT.code;
            case 5:
                return OrderPickTypeEnum.CONSIGN.code;
            case 6:
                return OrderPickTypeEnum.SELF.code;
            default:
                throw new ParameterException("无法识别的提车类型");
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
        //历史数据
        FullWaybill oldFw = waybillDao.findFullWaybillById(paramsDto.getCarDto().getWaybillId());
        System.out.println(oldFw);
        long currentTimeMillis = System.currentTimeMillis();
        Set<String> lockSet = new HashSet<>();
        try {
            String orderCarNo = paramsDto.getCarDto().getOrderCarNo();
            Long orderCarId = paramsDto.getCarDto().getOrderCarId();
            Long carrierId = paramsDto.getCarrierId();

            //是否分配司机任务标识
            boolean isReAllotCarrier = false;
            boolean isOneDriver = false;
            /**验证运单信息*/
            //【验证】承运商是否可以运营
            Long driverId = null;
            String driverName = null;
            String driverPhone = null;
            if (paramsDto.getCarrierType() == WaybillCarrierTypeEnum.TRUNK_ENTERPRISE.code || paramsDto.getCarrierType() == WaybillCarrierTypeEnum.TRUNK_INDIVIDUAL.code) {
                Carrier carrier = carrierDao.selectById(carrierId);
                if (carrier.getState() == null || carrier.getState() != CommonStateEnum.CHECKED.code || carrier.getBusinessState() == null || carrier.getBusinessState() != 0) {
                    throw new ParameterException("编号为{0}的车辆，所选承运商停运中", orderCarNo);
                }
                //验证司机信息
                if (carrier.getType() == 1) {
                    Driver driver = driverDao.findTopByCarrierId(carrierId);
                    if (driver == null) {
                        return BaseResultUtil.fail("运单,编号为{0}的车辆，所选承运商没有运营中的司机", orderCarNo);
                    }
                    isOneDriver = true;
                    driverId = driver.getId();
                    driverName = driver.getName();
                    driverPhone = driver.getPhone();
                }
            } else if (paramsDto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_ADMIN.code) {
                Admin admin = adminDao.selectById(carrierId);
                if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
                    return BaseResultUtil.fail("运单,编号为{0}的车辆，所选业务员已离职", orderCarNo);
                }
                isOneDriver = true;
                driverId = admin.getId();
                driverName = admin.getName();
                driverPhone = admin.getPhone();
            } else if (paramsDto.getCarrierType() == WaybillCarrierTypeEnum.SELF.code) {
                isOneDriver = true;
                if (WaybillTypeEnum.PICK.code == paramsDto.getType()) {
                    driverId = paramsDto.getCarDto().getLoadLinkUserId();
                    driverName = paramsDto.getCarDto().getLoadLinkName();
                    driverPhone = paramsDto.getCarDto().getLoadLinkPhone();
                } else {
                    driverId = paramsDto.getCarDto().getUnloadLinkUserId();
                    driverName = paramsDto.getCarDto().getUnloadLinkName();
                    driverPhone = paramsDto.getCarDto().getUnloadLinkPhone();
                }
            }

            /**验证运单车辆信息*/
            //加锁
            String lockKey = RedisKeys.getDispatchLock(orderCarNo);
            if (!redisLock.lock(lockKey)) {
                return BaseResultUtil.fail("运单，编号为{0}的车辆，其他人正在调度", orderCarNo);
            }
            lockSet.add(lockKey);

            //【验证】订单车辆状态
            OrderCar orderCar = orderCarDao.selectById(orderCarId);
            if (orderCar == null) {
                return BaseResultUtil.fail("运单,编号为{0}的车辆，车辆所属订单车辆不存在", orderCarNo);
            }
            //【验证】订单状态
            Order order = orderDao.selectById(orderCar.getOrderId());
            if (order == null) {
                return BaseResultUtil.fail("运单,编号为{0}的车辆，当前车辆不存在", orderCarNo);
            }

            /**1、添加运单信息*/
            Waybill waybill = waybillDao.selectById(paramsDto.getId());

            if (waybill == null) {
                return BaseResultUtil.fail("运单不存在");
            }

            isReAllotCarrier = validateReAllotCarrier(carrierId, waybill.getCarrierId());

            if (waybill.getState() > WaybillStateEnum.TRANSPORTING.code) {
                return BaseResultUtil.fail("运单已经在运输中不能修改");
            }
            waybill.setState(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? WaybillStateEnum.TRANSPORTING.code : WaybillStateEnum.ALLOT_CONFIRM.code);
            waybill.setSource(WaybillSourceEnum.MANUAL.code);
            waybill.setCarrierType(paramsDto.getCarrierType());
            waybill.setCarrierId(paramsDto.getCarrierId());
            if(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code){
                waybill.setCarrierName(waybill.getType() == WaybillTypeEnum.PICK.code ? order.getPickContactName() : order.getBackContactName());
            }else{
                waybill.setCarrierName(paramsDto.getCarrierName());
            }
            waybill.setFreightFee(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? BigDecimal.ZERO : (paramsDto.getType() == WaybillTypeEnum.PICK.code ? orderCar.getPickFee() : orderCar.getPickFee()));
            waybill.setFixedFreightFee(false);
            waybill.setRemark(paramsDto.getRemark());
            waybillDao.updateByIdForNull(waybill);

            /**2、添加运单车辆信息*/
            WaybillCar waybillCar = waybillCarDao.selectById(paramsDto.getCarDto().getId());
            //初始copy赋值
            BeanUtils.copyProperties(paramsDto.getCarDto(), waybillCar);
            waybillCar.setWaybillId(waybill.getId());
            waybillCar.setWaybillNo(waybill.getNo());
            waybillCar.setFreightFee(waybill.getFreightFee());
            //城市信息赋值
            fillWaybillCarCityInfo(waybillCar);
            //业务中心信息赋值
            fillWaybillcarStoreInfo(waybillCar);
            //提送车业务员
            fillWaybillCarHandleAdmin(waybillCar, waybill.getType());
            //计算预计到达时间
            fillWaybillCarExpectEndTime(waybillCar);
            waybillCar.setReceiptFlag(waybillCar.getUnloadLinkPhone().equals(order.getBackContactPhone()));
            //运单车辆状态
            waybillCar.setState(isOneDriver ?
                    (waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? WaybillCarStateEnum.WAIT_UNLOAD_CONFIRM.code : WaybillCarStateEnum.WAIT_LOAD.code)
                    : WaybillCarStateEnum.WAIT_ALLOT.code);
            if (waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code) {
                waybillCar.setLoadTime(currentTimeMillis);
            }
            waybillCar.setLoadLinkUserId(waybillCar.getLoadLinkUserId());
            waybillCar.setUnloadLinkUserId(waybillCar.getUnloadLinkUserId());
            waybillCar.setCreateTime(currentTimeMillis);
            //TODO 计算预计到达时间，计算线路是否存在
            waybillCarDao.updateByIdForNull(waybillCar);

            /**3、添加任务信息*/
            if (isReAllotCarrier) {
                //取消之前司机任务
                taskDao.cancelBywaybillId(waybill.getId());
                //创建新司机任务
                if (isOneDriver) {
                    Task task = new Task();
                    task.setNo(csTaskService.getTaskNo(waybill.getNo()));
                    task.setDriverId(driverId);
                    task.setDriverName(driverName);
                    task.setWaybillId(waybill.getId());
                    task.setWaybillNo(waybill.getNo());
                    task.setGuideLine(waybill.getGuideLine());
                    task.setCarNum(waybill.getCarNum());
                    task.setState(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? TaskStateEnum.TRANSPORTING.code : TaskStateEnum.WAIT_LOAD.code);
                    task.setDriverName(driverName);
                    task.setDriverPhone(driverPhone);
                    task.setDriverId(driverId);
                    //添加运力信息
                    if (paramsDto.getCarrierType() == WaybillCarrierTypeEnum.LOCAL_CONSIGN.code) {
                        VehicleRunning vehicleRunning = vehicleRunningDao.findByDriverId(driverId);
                        if (vehicleRunning != null) {
                            task.setVehicleRunningId(vehicleRunning.getId());
                            task.setVehiclePlateNo(vehicleRunning.getPlateNo());
                        }
                    }
                    task.setRemark(paramsDto.getRemark());
                    task.setCreateUser(paramsDto.getLoginName());
                    task.setCreateUserId(paramsDto.getLoginId());
                    task.setCreateTime(currentTimeMillis);
                    taskDao.insert(task);
                }
            }

            //更新订单信息
            if (paramsDto.getType() == WaybillTypeEnum.PICK.code) {
                OrderCar oc = new OrderCar();
                oc.setId(orderCarId);
                oc.setState(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? OrderCarStateEnum.PICKING.code : OrderCarStateEnum.WAIT_PICK.code);
                oc.setPickType(getLocalCarryType(waybill.getCarrierType()));
                orderCarDao.updateById(oc);
            } else {
                OrderCar oc = new OrderCar();
                oc.setId(orderCarId);
                oc.setState(waybill.getCarrierType() == WaybillCarrierTypeEnum.SELF.code ? OrderCarStateEnum.BACKING.code : OrderCarStateEnum.WAIT_BACK.code);
                oc.setBackType(getLocalCarryType(waybill.getCarrierType()));
                orderCarDao.updateById(oc);
            }
        } finally {
            if (!CollectionUtils.isEmpty(lockSet)) {
                for (String key : lockSet) {
                    redisLock.releaseLock(key);
                }
                //redisUtil.del(lockSet.toArray(new String[0]));
            }
        }
        return BaseResultUtil.success();
    }

    private boolean validateReAllotCarrier(Long oldCarrierId, Long newCarrierId) {
        oldCarrierId = oldCarrierId == null || oldCarrierId <= 0 ? 0L : oldCarrierId;
        newCarrierId = newCarrierId == null || newCarrierId <= 0 ? 0L : newCarrierId;
        if(oldCarrierId.equals(newCarrierId)){
            return false;
        }
        return true;
    }

    /**
     * 计算到达时间
     **/
    private WaybillCar fillWaybillCarExpectEndTime(WaybillCar waybillCar) {
        if (waybillCar.getExpectStartTime() == null) {
            throw new ParameterException("请填写预估提车日期");
        }
        Line line = csLineService.getLineByCity(waybillCar.getStartCityCode(), waybillCar.getEndCityCode(), true);
        if (line != null && line.getDays() != null) {
            waybillCar.setExpectEndTime(TimeStampUtil.addDays(waybillCar.getExpectStartTime(), line.getDays().intValue()));
        }
        return waybillCar;
    }

    /**
     * 计算城市信息
     **/
    private WaybillCar fillWaybillCarCityInfo(WaybillCar wc) {
        FullCity startFullCity = csCityService.findFullCity(wc.getStartAreaCode(), CityLevelEnum.PROVINCE);
        copyWaybillCarStartCity(startFullCity, wc);
        FullCity endFullCity = csCityService.findFullCity(wc.getEndAreaCode(), CityLevelEnum.PROVINCE);
        copyWaybillCarEndCity(endFullCity, wc);
        return wc;
    }

    /**
     * 计算业务中心
     **/
    private WaybillCar fillWaybillcarStoreInfo(WaybillCar wc) {
        Long startBelongStoreId = csStoreService.getBelongStoreId(wc.getStartStoreId(), wc.getStartCityCode());
        if (startBelongStoreId != null) {
            wc.setStartBelongStoreId(startBelongStoreId);
        }
        Long endBelongStoreId = csStoreService.getBelongStoreId(wc.getEndStoreId(), wc.getEndCityCode());
        if (endBelongStoreId != null) {
            wc.setEndBelongStoreId(endBelongStoreId);
        }
        return wc;
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
        Long loginId = paramsDto.getLoginId();
        Set<String> lockSet = new HashSet<>();
        //【验证参数】操作人
        Admin admin = adminDao.selectById(loginId);
        if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
            return BaseResultUtil.fail("当前业务员，不在职");
        }
        try {
            List<SaveTrunkWaybillCarDto> carList = paramsDto.getList();
            if (carList == null || carList.isEmpty()) {
                return BaseResultUtil.fail("车辆列表不能为空");
            }
            Long carrierId = paramsDto.getCarrierId();

            //是否分配任务
            boolean isOneDriver = false;
            //【验证】承运商和司机信息
            Carrier carrier = carrierDao.selectById(carrierId);
            if (carrier.getState() == null || carrier.getState() != CommonStateEnum.CHECKED.code || carrier.getBusinessState() == null || carrier.getBusinessState() != 0) {
                return BaseResultUtil.fail("运单，所选承运商，停运中");
            }
            Driver driver = new Driver();
            if (carrier.getType() == 1) {
                driver = driverDao.findTopByCarrierId(carrierId);
                if (driver == null) {
                    return BaseResultUtil.fail("运单，所选承运商没有运营中的司机");
                }
                isOneDriver = true;
            }

            /**1、组装运单信息*/
            Waybill waybill = new Waybill();
            waybill.setNo(sendNoService.getNo(SendNoTypeEnum.WAYBILL));
            waybill.setType(WaybillTypeEnum.TRUNK.code);
            waybill.setSource(loginId.equals(carrierId) ? WaybillSourceEnum.SELF.code : WaybillSourceEnum.MANUAL.code);
            waybill.setGuideLine(paramsDto.getGuideLine());
            waybill.setCarrierId(carrierId);
            waybill.setCarrierName(carrier.getName());
            waybill.setCarrierType(carrier.getType());
            waybill.setCarNum(carList.size());
            waybill.setState(WaybillStateEnum.ALLOT_CONFIRM.code);
            waybill.setFreightFee(MoneyUtil.convertYuanToFen(paramsDto.getFreightFee()));
            waybill.setFixedFreightFee(paramsDto.getFixedFreightFee());
            waybill.setRemark(paramsDto.getRemark());
            waybill.setCreateTime(currentMillisTime);
            waybill.setCreateUser(admin.getName());
            waybill.setCreateUserId(admin.getId());
            //TODO 干线运单所属业务中心
            //waybill.setInputStoreId(paramsDto.);
            waybillDao.insert(waybill);

            /**2、运单，车辆循环*/
            Set<Long> waybillCarIdSet = new HashSet<>();
            Set<String> orderCarNoSet = Sets.newHashSet();
            for (SaveTrunkWaybillCarDto dto : carList) {
                if (dto == null) {
                    continue;
                }
                String orderCarNo = dto.getOrderCarNo();
                Long orderCarId = dto.getOrderCarId();

                //加锁
                String lockKey = RedisKeys.getDispatchLock(orderCarNo);
                if (!redisLock.lock(lockKey, 20000, 100, 300L)) {
                    throw new ParameterException("运单，编号为{0}的车辆，其他人正在调度", orderCarNo);
                }
                lockSet.add(lockKey);

                //验证订单车辆状态
                OrderCar orderCar = orderCarDao.selectById(orderCarId);
                if (orderCar == null) {
                    throw new ParameterException("运单，编号为{0}的车辆，不存在", orderCarNo);
                }
                if (orderCar.getState() == null) {
                    throw new ParameterException("运单，编号为{0}的车辆，无法提车调度", orderCarNo);
                }
                //验证订单状态
                Order order = orderDao.selectById(orderCar.getOrderId());
                if (order == null) {
                    throw new ParameterException("运单，编号为{0}的车辆，所属订单车辆不存在", orderCarNo);
                }

                if (order.getState() == null
                        || order.getState() < OrderStateEnum.CHECKED.code
                        || order.getState() > OrderStateEnum.FINISHED.code) {
                    throw new ParameterException("运单，编号为{0}的车辆，所属订单状态无法干线调度", orderCarNo);
                }

                //验证是否已经调度过,已经调度的为
/*                int n = waybillCarDao.countForValidateRepeatTrunkDisPatch(areaList);
                if (n > 0) {
                    throw new ParameterException("运单，编号为{0}的车辆，已经调度过", orderCarNos);
                }*/

                //验证出发地与上一次调度目的地是否一致
                WaybillCar prevWc = waybillCarDao.findLastByOderCarId(orderCarId);
                if (prevWc != null) {
                    if (!prevWc.getEndAddress().equals(dto.getStartAddress())) {
                        throw new ServerException("车辆本次调度出发地址与上次调度结束地址不一致");
                    }
                }

                //同一订单车辆不能重复
                if (orderCarNoSet.contains(dto.getOrderCarNo())) {
                    throw new ServerException("车辆{0}，重复", dto.getOrderCarNo());
                }

                WaybillCar waybillCar = new WaybillCar();
                BeanUtils.copyProperties(dto, waybillCar);
                waybillCar.setWaybillId(waybill.getId());
                waybillCar.setFreightFee(MoneyUtil.convertYuanToFen(dto.getFreightFee()));
                waybillCar.setWaybillNo(waybill.getNo());
                waybillCar.setOrderCarId(orderCar.getId());
                //城市信息赋值
                fillWaybillCarCityInfo(waybillCar);
                //业务中心信息赋值
                fillWaybillcarStoreInfo(waybillCar);
                //填充接待员
                if (StringUtils.isBlank(waybillCar.getUnloadLinkPhone())) {
                    if (waybillCar.getEndStoreId() != null) {
                        fillWaybillCarHandleAdmin(waybillCar, waybill.getType());
                    } else {
                        throw new ParameterException("请填写收车人");
                    }
                }
                //计算预计到达时间
                fillWaybillCarExpectEndTime(waybillCar);
                waybillCar.setReceiptFlag(waybillCar.getUnloadLinkPhone().equals(order.getBackContactPhone()));
                waybillCar.setState(isOneDriver ? WaybillCarStateEnum.WAIT_LOAD.code : WaybillCarStateEnum.WAIT_ALLOT.code);
                waybillCar.setCreateTime(currentMillisTime);
                waybillCarDao.insert(waybillCar);

                waybillCarIdSet.add(waybillCar.getId());
                //更新订单车辆状态
                OrderCar noc = new OrderCar();
                noc.setId(orderCar.getId());
                noc.setTrunkState(OrderCarTrunkStateEnum.WAIT_NEXT_DISPATCH.code);
                int n = waybillCarDao.countPrevTrunk(waybillCar.getId());
                if (n == 0) {
                    //提干
                    if (order.getStartAddress().equals(waybillCar.getStartAddress())) {
                        noc.setPickType(OrderPickTypeEnum.WL.code);
                        noc.setPickState(OrderCarLocalStateEnum.F_WL.code);
                        noc.setState(OrderCarStateEnum.WAIT_TRUNK.code);
                    }
                }
                //干线最后一段
                if (order.getEndCityCode().equals(waybillCar.getEndCityCode())) {
                    noc.setTrunkState(OrderCarTrunkStateEnum.DISPATCHED.code);
                }
                //干送
                if (order.getEndAddress().equals(waybillCar.getEndAddress())) {
                    noc.setBackType(OrderPickTypeEnum.WL.code);
                    noc.setBackState(OrderCarLocalStateEnum.F_WL.code);
                }
                orderCarDao.updateById(noc);
                orderCarNoSet.add(waybillCar.getOrderCarNo());

            }

            //承运商有且仅有一个司机
            Task task = null;
            if (isOneDriver) {
                /**1+、写入任务表*/
                task = new Task();
                //获取编号
                String taskNo = csTaskService.getTaskNo(waybill.getNo());
                lockSet.add(RedisKeys.getNewTaskNoKey(waybill.getNo()));
                if (taskNo == null) {
                    throw new ServerException("获取任务编号失败");
                }
                task.setNo(taskNo);

                task.setWaybillId(waybill.getId());
                task.setWaybillNo(waybill.getNo());
                task.setGuideLine(waybill.getGuideLine());
                task.setCarNum(carList.size());
                task.setState(TaskStateEnum.WAIT_LOAD.code);
                task.setDriverId(driver.getId());
                task.setDriverName(driver.getName());
                task.setDriverPhone(driver.getPhone());
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

                //添加任务车辆信息
                taskCarDao.saveBatchByTaskIdAndWaybillCarIds(task.getId(), waybillCarIdSet);
            }
        } finally {
            if (!CollectionUtils.isEmpty(lockSet)) {
                redisUtil.del(lockSet.toArray(new String[0]));
            }
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
        Set<String> lockSet = new HashSet<>();
        //【验证参数】操作人
        Admin admin = adminDao.selectById(paramsDto.getLoginId());
        if (admin == null || admin.getState() != AdminStateEnum.CHECKED.code) {
            return BaseResultUtil.fail("当前业务员，不在职");
        }
        //TODO 验证用户角色
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
            boolean isOneDriver = false;

            //【验证】承运商和司机信息
            Carrier carrier = carrierDao.selectById(carrierId);
            if (carrier == null || carrier.getState() == null || carrier.getState() != CommonStateEnum.CHECKED.code || carrier.getBusinessState() == null || carrier.getBusinessState() != 0) {
                return BaseResultUtil.fail("运单，所选承运商，停运中");
            }
            Driver driver = null;
            if (carrier.getDriverNum() <= 1) {
                driver = driverDao.findTopByCarrierId(carrierId);
                if (driver == null) {
                    return BaseResultUtil.fail("运单{0}，所选承运商没有运营中的司机", waybill.getNo());
                }
                isOneDriver = true;
                if (!carrierId.equals(waybill.getCarrierId())) {
                    isReAllotDriver = true;
                }
            }
            if (waybill.getState() >= WaybillStateEnum.TRANSPORTING.code && isReAllotDriver) {
                return BaseResultUtil.fail("运输中运单不允许变更司机，请使用卸载车辆功能");
            }

            /**1、组装运单信息*/
            waybill.setGuideLine(paramsDto.getGuideLine());
            if (isReAllotDriver) {
                waybill.setCarrierId(carrier.getId());
                waybill.setCarrierType(carrier.getType());
            }
            waybill.setCarNum(list.size());
            waybill.setCarrierId(paramsDto.getCarrierId());
            waybill.setCarrierId(carrierId);
            waybill.setCarrierType(carrier.getType());
            waybill.setCarrierName(carrier.getName());
            waybill.setFreightFee(MoneyUtil.convertYuanToFen(paramsDto.getFreightFee()));
            waybill.setFixedFreightFee(paramsDto.getFixedFreightFee());
            waybill.setRemark(paramsDto.getRemark());
            waybillDao.updateByIdForNull(waybill);
            /**2、运单，车辆循环*/
            boolean hasNewWaybillCar = false;
            Set<Long> unDeleteWaybillCarIds = new HashSet<>();
            for (UpdateTrunkWaybillCarDto dto : list) {
                if (dto == null) {
                    continue;
                }
                boolean isNewWaybillCar = false;
                WaybillCar waybillCar = null;
                if (dto.getId() != null) {
                    //修改的车辆
                    waybillCar = waybillCarDao.selectById(dto.getId());
                }
                if (waybillCar == null) {
                    isNewWaybillCar = true;
                    hasNewWaybillCar = true;
                    //新增的车辆
                    waybillCar = new WaybillCar();
                }
                BeanUtils.copyProperties(dto, waybillCar);
                String orderCarNo = dto.getOrderCarNo();
                Long orderCarId = dto.getOrderCarId();

                //验证订单车辆状态
                OrderCar orderCar = orderCarDao.selectById(orderCarId);
                if (orderCar == null) {
                    throw new ParameterException("运单，编号为{0}的车辆，不存在", orderCarNo);
                }
                if (orderCar.getState() == null) {
                    throw new ParameterException("运单，编号为{0}的车辆，无法提车调度", orderCarNo);
                }
                //验证订单状态
                Order order = orderDao.selectById(orderCar.getOrderId());
                if (order == null) {
                    throw new ParameterException("运单，编号为{0}的车辆，所属订单车辆不存在", orderCarNo);
                }

                if (order.getState() == null
                        || order.getState() < OrderStateEnum.CHECKED.code
                        || order.getState() > OrderStateEnum.FINISHED.code) {
                    throw new ParameterException("运单，编号为{0}的车辆，所属订单状态无法干线调度", orderCarNo);
                }

                //加锁
                String lockKey = RedisKeys.getDispatchLock(orderCarNo);
                if (!redisLock.lock(lockKey, 20000, 100, 300L)) {
                    throw new ParameterException("运单{0}，编号为{0}的车辆，其他人正在调度", waybill.getNo(), orderCarNo);
                }
                //包板线路不能为空
                if (waybill.getFixedFreightFee() && waybillCar.getLineId() == null) {
                    throw new ParameterException("运单{0}，编号为{0}的车辆，线路不能为空", waybill.getNo(), orderCarNo);
                }
                lockSet.add(lockKey);
                //验证出发地与上一次调度目的地是否一致
                WaybillCar prevWc;
                if (waybillCar.getId() == null) {
                    prevWc = waybillCarDao.findLastByOderCarId(orderCarId);
                } else {
                    prevWc = waybillCarDao.findLastByOderCarIdAndId(waybillCar.getId(), orderCarId);
                }
                if (prevWc != null) {
                    if (!prevWc.getEndAddress().equals(dto.getStartAddress())) {
                        throw new ServerException("本次调度出发地址与上次调度结束地址不一致");
                    }
                }

                //车辆数据
                waybillCar.setWaybillId(waybill.getId());
                waybillCar.setWaybillNo(waybill.getNo());
                waybillCar.setReceiptFlag(order.getBackContactPhone().equals(waybillCar.getUnloadLinkPhone()));
                //城市信息赋值
                fillWaybillCarCityInfo(waybillCar);
                //业务中心信息赋值
                fillWaybillcarStoreInfo(waybillCar);
                //计算预计到达时间
                fillWaybillCarExpectEndTime(waybillCar);
                //新车添加车辆状态
                waybillCar.setState(isOneDriver ? WaybillCarStateEnum.WAIT_LOAD.code : WaybillCarStateEnum.WAIT_ALLOT.code);
                waybillCar.setCreateTime(currentMillisTime);
                if (isNewWaybillCar) {
                    waybillCarDao.insert(waybillCar);
                } else {
                    waybillCarDao.updateByIdForNull(waybillCar);
                }

                //更新车辆信息
                int n = waybillCarDao.countPrevTrunk(waybillCar.getId());
                OrderCar noc = new OrderCar();
                noc.setId(orderCar.getId());
                noc.setTrunkState(OrderCarTrunkStateEnum.WAIT_NEXT_DISPATCH.code);
                if (n == 0) {
                    //提干
                    if (order.getStartAddress().equals(waybillCar.getStartAddress())) {
                        noc.setPickType(OrderPickTypeEnum.WL.code);
                        noc.setPickState(OrderCarLocalStateEnum.F_WL.code);
                    }
                }
                //干线最后一段
                if (order.getEndCityCode().equals(waybillCar.getEndCityCode())) {
                    noc.setTrunkState(OrderCarTrunkStateEnum.DISPATCHED.code);
                }
                //干送
                if (order.getEndAddress().equals(waybillCar.getEndAddress())) {
                    noc.setBackType(OrderPickTypeEnum.WL.code);
                    noc.setBackState(OrderCarLocalStateEnum.F_WL.code);
                }
                orderCarDao.updateById(noc);
                unDeleteWaybillCarIds.add(waybillCar.getId());
            }

            //承运商有且仅有一个司机
            Task task = null;
            //重新分配司机或者新车辆
            if (isReAllotDriver || (isOneDriver && hasNewWaybillCar)) {
                //删除任务
                taskDao.deleteByWaybillId(waybill.getId());
                /**1+、写入任务表*/
                task = new Task();
                String taskNo = csTaskService.getTaskNo(waybill.getNo());
                lockSet.add(RedisKeys.getNewTaskNoKey(waybill.getNo()));
                if (taskNo == null) {
                    throw new ServerException("获取任务编号失败");
                }
                task.setNo(taskNo);
                task.setWaybillId(waybill.getId());
                task.setWaybillNo(waybill.getNo());
                task.setGuideLine(waybill.getGuideLine());
                task.setCarNum(waybill.getCarNum());
                task.setState(TaskStateEnum.WAIT_LOAD.code);
                task.setDriverId(driver.getId());
                task.setDriverName(driver.getName());
                task.setDriverPhone(driver.getPhone());
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
                /**2+、组装任务车辆表数据*/
                taskCarDao.saveBatchByTaskIdAndWaybillCarIds(task.getId(), unDeleteWaybillCarIds);
            }

            //查询删除的车辆
            List<WaybillCar> waybillCars = waybillCarDao.findWaitCancelListByUnCancelIds(unDeleteWaybillCarIds, waybill.getId());
            if (!CollectionUtils.isEmpty(waybillCars)) {
                //取消运单车辆
                waybillCars.forEach(waybillCar -> {
                    cancelWaybillCar(waybill.getType(), waybillCar);
                });
            }

            waybillDao.updateFreightFee(waybill.getId());

        } finally {
            if (!CollectionUtils.isEmpty(lockSet)) {
                redisUtil.del(lockSet.toArray(new String[0]));
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
    public ResultVo cancel(CancelWaybillDto paramsDto) {
        //取消运单
        List<Waybill> waybillList = waybillDao.findListByIds(paramsDto.getWaybillIdList());
        for (Waybill waybill : waybillList) {
            if (waybill == null) {
                continue;
            }
            //取消运单
            cancelWaybill(waybill);
        }
        return BaseResultUtil.success();
    }

    private void cancelWaybill(Waybill waybill) {

        //状态不大于待承接
        if (waybill.getState() > WaybillStateEnum.TRANSPORTING.code) {
            throw new ServerException("运单{0},[{1}]状态不能取消", waybill.getNo(), WaybillStateEnum.valueOf(waybill.getState()).name);
        }
        //修改运单主单状态
        waybillDao.updateStateById(WaybillStateEnum.F_CANCEL.code, waybill.getId());
        //修改任务主单状态
        taskDao.cancelBywaybillId(waybill.getId());
        //修改车辆状态和调度状态
        List<WaybillCar> list = waybillCarDao.findListByWaybillId(waybill.getId());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(waybillCar -> cancelWaybillCar(waybill.getType(), waybillCar));

    }

    /**
     * 取消运单车辆
     *
     * @author JPG
     * @since 2019/10/29 9:57
     */
    private void cancelWaybillCar(int waybillType, WaybillCar waybillCar) {
        if (waybillCar.getState() >= WaybillCarStateEnum.LOADED.code) {
            throw new ParameterException("车辆{0}运输中，不允许取消", waybillCar.getOrderCarNo());
        }
        OrderCar oc = orderCarDao.selectById(waybillCar.getOrderCarId());
        if (oc == null) {
            throw new ParameterException("车辆{0}, 订单信息错误", waybillCar.getOrderCarNo());
        }
        Order order = orderDao.selectById(oc.getOrderId());
        if (order == null) {
            throw new ParameterException("车辆{0}, 车辆信息错误", waybillCar.getOrderCarNo());
        }

        OrderCar noc = new OrderCar();
        noc.setId(oc.getId());
        //修改车辆状态
        if (WaybillTypeEnum.PICK.code == waybillType) {
            noc.setPickType(order.getPickType());
            //订单车辆状态
            if (OrderCarStateEnum.WAIT_PICK.code == oc.getState()) {
                noc.setState(OrderCarStateEnum.WAIT_PICK_DISPATCH.code);
            }
            //订单车辆提车状态
            noc.setPickState(OrderCarLocalStateEnum.WAIT_DISPATCH.code);
            //取消运单车辆
            waybillCarDao.updateStateById(waybillCar.getId(), WaybillCarStateEnum.F_CANCEL.code);
        } else if (WaybillTypeEnum.BACK.code == waybillType) {
            noc.setBackType(order.getBackType());
            //订单车辆状态
            if (OrderCarStateEnum.WAIT_BACK.code == oc.getState()) {
                noc.setState(OrderCarStateEnum.WAIT_BACK_DISPATCH.code);
            }
            //订单车辆配送状态
            noc.setBackState(OrderCarLocalStateEnum.WAIT_DISPATCH.code);
            //取消运单车辆
            waybillCarDao.updateStateById(waybillCar.getId(), WaybillCarStateEnum.F_CANCEL.code);
        } else if (WaybillTypeEnum.TRUNK.code == waybillType) {
            //取消该车辆所有后续调度
            waybillCarDao.cancelAfterWaybillCar(waybillCar.getId(), waybillCar.getOrderCarNo());
            //查询是否还有干线调度
            int num = waybillCarDao.countTrunkWaybillCar(waybillCar.getOrderCarNo());
            if (num <= 0) {
                noc.setTrunkState(OrderCarTrunkStateEnum.WAIT_DISPATCH.code);
            } else {
                //订单车辆干线状态
                noc.setTrunkState(OrderCarTrunkStateEnum.WAIT_NEXT_DISPATCH.code);
            }
            //订单车辆状态
            if (OrderCarStateEnum.WAIT_TRUNK.code == oc.getState()) {
                noc.setState(OrderCarStateEnum.WAIT_TRUNK_DISPATCH.code);
            }

            //提干
            if (order.getStartAddress().equals(waybillCar.getStartAddress())) {
                noc.setPickType(order.getPickType());
                noc.setPickState(OrderCarLocalStateEnum.WAIT_DISPATCH.code);
            }
            //干线最后一段
            if (order.getEndCityCode().equals(waybillCar.getEndCityCode())) {
                noc.setTrunkState(OrderCarTrunkStateEnum.WAIT_NEXT_DISPATCH.code);
                if (num <= 0) {
                    noc.setTrunkState(OrderCarTrunkStateEnum.WAIT_DISPATCH.code);
                }
            }
            //干送
            if (order.getEndAddress().equals(waybillCar.getEndAddress())) {
                noc.setBackType(order.getBackType());
                noc.setBackState(OrderCarLocalStateEnum.WAIT_DISPATCH.code);
            }

        } else {
            throw new ParameterException("运单类型错误");
        }
        //更新任务车辆数
        Task task = taskDao.findByWaybillCarId(waybillCar.getId());
        if (task != null) {
            taskDao.updateNum(task.getId());
            validateAndFinishTask(waybillCar.getId());
        }
        //更新运单车辆数
        waybillDao.updateNum(waybillCar.getWaybillId());
        validateAndFinishWaybill(waybillCar.getWaybillId());
        orderCarDao.updateById(noc);
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
        if (waybill.getState() <= WaybillStateEnum.WAIT_ALLOT.code || waybill.getState() >= WaybillStateEnum.FINISHED.code) {
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
                Store store = csStoreService.getOneBelongByAreaCode(waybillCar.getStartAreaCode());
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
        //查询运单
        Waybill waybill = waybillDao.selectById(paramsDto.getWaybillId());
        if (waybill == null) {
            return BaseResultUtil.fail("运单信息错误");
        }
        BigDecimal oldTotalFee = BigDecimal.ZERO;
        Set<WaybillCar> waybillCars = Sets.newHashSet();
        for (Long waybillCarId : carIdList) {
            if (waybillCarId == null) {
                continue;
            }
            WaybillCar waybillCar = waybillCarDao.selectById(waybillCarId);
            if (waybillCar == null) {
                throw new ParameterException("ID为{0}的车辆不存在", waybillCarId);
            }
            if (waybillCar.getState() > WaybillCarStateEnum.UNLOADED.code) {
                throw new ParameterException("车辆{0}已完结", waybillCar.getOrderCarNo());
            }
            if (!waybillCar.getWaybillId().equals(waybill.getId())) {
                throw new ParameterException("车辆{0}不属于运单{1}", waybillCar.getOrderCarNo(), waybill.getNo());
            }

            //已装车的完成
            if (waybillCar.getState() >= WaybillCarStateEnum.LOADED.code) {
                copyWaybillCarEndCity(fullCity, waybillCar);
                waybillCar.setEndAddress(paramsDto.getEndAddress());

                Long endStoreId = paramsDto.getEndStoreId();
                String endStoreName = paramsDto.getEndStoreName();
                if (paramsDto.getEndStoreId() == null) {
                    //算起始地业务中心
                    Store store = csStoreService.getOneBelongByAreaCode(waybillCar.getStartAreaCode());
                    if (store != null) {
                        endStoreId = store.getId();
                        endStoreName = store.getName();
                    }
                }


                waybillCar.setEndStoreId(endStoreId);
                waybillCar.setEndStoreName(endStoreName);
                waybillCar.setReceiptFlag(false);
                waybillCar.setUnloadLinkName(paramsDto.getUnloadLinkName());
                waybillCar.setUnloadLinkPhone(paramsDto.getUnloadLinkPhone());
                waybillCar.setUnloadLinkUserId(paramsDto.getUnloadLinkUserId());
                waybillCar.setUnloadTime(paramsDto.getUnloadTime());
                //车辆运输到中途卸车算调度单业务中心
                waybillCar.setEndBelongStoreId(waybillCar.getStartBelongStoreId());
                //交接状态如何变更
                waybillCar.setState(WaybillCarStateEnum.UNLOADED.code);
                //取消后续调度
                waybillCarDao.cancelAfterWaybillCar(waybillCar.getId(), waybillCar.getOrderCarNo());
                //
                waybillCarDao.updateByIdForNull(waybillCar);
                waybillCars.add(waybillCar);
                oldTotalFee = oldTotalFee.add(waybillCar.getFreightFee());
                //验证并完成任务
                validateAndFinishTask(waybillCar.getId());


            } else {
                //未装车的取消
                cancelWaybillCar(waybill.getType(), waybillCar);
            }
        }

        //按比例均摊运费
        shareWaybillCarFreightFee(waybillCars, oldTotalFee, paramsDto.getFreightFee());
        //更新运单费用
        waybillDao.updateFreightFee(waybill.getId());
        //验证并完成运单
        validateAndFinishWaybill(waybill.getId());
        return BaseResultUtil.success();
    }

    private void validateAndFinishTask(Long waybillCarId) {
        Task task = taskDao.findByWaybillCarId(waybillCarId);
        if(task == null){
            return;
        }
        int count = taskCarDao.countUnFinishByTaskId(task.getId());
        if(count > 0){
            return;
        }
        taskDao.updateForFinish(task.getId());
    }

    private void validateAndFinishWaybill(Long waybillId) {
        int count = waybillCarDao.countUnFinishByWaybillId(waybillId);
        if(count > 0){
            return;
        }
        waybillDao.updateForFinish(waybillId);
    }

    private void shareWaybillCarFreightFee(Set<WaybillCar> waybillCars, BigDecimal oldTotalFee, BigDecimal newTotalFee) {
        if(newTotalFee.compareTo(BigDecimal.ZERO) == 0){
            waybillCars.forEach(waybillCar -> {
                waybillCar.setFreightFee(BigDecimal.ZERO);
                waybillCarDao.updateById(waybillCar);
            });
        }else if(oldTotalFee.compareTo(BigDecimal.ZERO) == 0){

            BigDecimal[] bigDecimals = newTotalFee.divideAndRemainder(new BigDecimal(waybillCars.size()));
            BigDecimal rAvg = bigDecimals[0];
            BigDecimal rRemainder = bigDecimals[1];
            for (WaybillCar waybillCar : waybillCars) {
                //运费
                if (rRemainder.compareTo(BigDecimal.ZERO) > 0) {
                    waybillCar.setFreightFee(rAvg.add(BigDecimal.ONE));
                    rRemainder = rRemainder.subtract(BigDecimal.ONE);
                } else {
                    waybillCar.setFreightFee(rAvg);
                }
                waybillCarDao.updateById(waybillCar);
            }
        }else{
            BigDecimal freightFee = MoneyUtil.convertYuanToFen(newTotalFee);
            BigDecimal avg = freightFee.divide(oldTotalFee, 0, RoundingMode.FLOOR);
            BigDecimal remainder = freightFee.subtract(avg.multiply(oldTotalFee));

            BigDecimal[] bigDecimals = remainder.divideAndRemainder(new BigDecimal(waybillCars.size()));
            BigDecimal rAvg = bigDecimals[0];
            BigDecimal rRemainder = bigDecimals[1];
            for (WaybillCar waybillCar : waybillCars) {
                //运费
                if (rRemainder.compareTo(BigDecimal.ZERO) > 0) {
                    waybillCar.setFreightFee(waybillCar.getFreightFee().multiply(avg).add(rAvg).add(BigDecimal.ONE));
                    rRemainder = rRemainder.subtract(BigDecimal.ONE);
                } else {
                    waybillCar.setFreightFee(waybillCar.getFreightFee().multiply(avg).add(rAvg));
                }
                waybillCarDao.updateById(waybillCar);
            }
        }

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
        newWaybill.setCreateUser(paramsDto.getLoginName());
        newWaybill.setCreateUserId(paramsDto.getLoginId());
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
                Store store = csStoreService.getOneBelongByAreaCode(paramsDto.getEndAreaCode());
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
            newWaybillCar.setLoadLinkName(paramsDto.getLoginName());
            newWaybillCar.setLoadLinkUserId(paramsDto.getLoginId());
            newWaybillCar.setLoadLinkPhone(paramsDto.getUserPhone());
            newWaybillCar.setUnloadLinkName(waybillCar.getUnloadLinkName());
            newWaybillCar.setUnloadLinkPhone(waybillCar.getUnloadLinkPhone());
            newWaybillCar.setUnloadLinkUserId(waybillCar.getUnloadLinkUserId());
            newWaybillCar.setCreateTime(currentTimeMillis);
            waybillCarDao.insert(newWaybillCar);
        }
        return newWaybill;
    }

    private WaybillCar copyWaybillCarStartCity(FullCity fullCity, WaybillCar waybillCar) {
        if (fullCity == null) {
            return waybillCar;
        }
        waybillCar.setStartProvince(fullCity.getProvince());
        waybillCar.setStartProvinceCode(fullCity.getProvinceCode());
        waybillCar.setStartCity(fullCity.getCity());
        waybillCar.setStartCityCode(fullCity.getCityCode());
        waybillCar.setStartArea(fullCity.getArea());
        waybillCar.setStartAreaCode(fullCity.getAreaCode());
        return waybillCar;
    }

    private WaybillCar copyWaybillCarEndCity(FullCity fullCity, WaybillCar waybillCar) {
        if (fullCity == null) {
            return waybillCar;
        }
        waybillCar.setEndProvince(fullCity.getProvince());
        waybillCar.setEndProvinceCode(fullCity.getProvinceCode());
        waybillCar.setEndCity(fullCity.getCity());
        waybillCar.setEndCityCode(fullCity.getCityCode());
        waybillCar.setEndArea(fullCity.getArea());
        waybillCar.setEndAreaCode(fullCity.getAreaCode());
        return waybillCar;
    }

}

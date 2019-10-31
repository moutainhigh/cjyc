package com.cjyc.web.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjkj.common.redis.lock.RedisDistributedLock;
import com.cjkj.common.redis.template.StringRedisUtil;
import com.cjyc.common.model.dao.*;
import com.cjyc.common.model.dto.web.task.*;
import com.cjyc.common.model.entity.*;
import com.cjyc.common.model.enums.SendNoTypeEnum;
import com.cjyc.common.model.enums.order.OrderCarStateEnum;
import com.cjyc.common.model.enums.task.TaskStateEnum;
import com.cjyc.common.model.enums.waybill.WaybillCarStateEnum;
import com.cjyc.common.model.enums.waybill.WaybillStateEnum;
import com.cjyc.common.model.keys.RedisKeys;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.OrderCarVo;
import com.cjyc.web.api.exception.ServerException;
import com.cjyc.web.api.service.ISendNoService;
import com.cjyc.web.api.service.ITaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;

/**
 * <p>
 * 任务表(子运单) 服务实现类
 * </p>
 *
 * @author JPG
 * @since 2019-10-26
 */
@Service
public class TaskServiceImpl extends ServiceImpl<ITaskDao, Task> implements ITaskService {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private IOrderCarDao orderCarDao;
    @Resource
    private IWaybillDao waybillDao;
    @Resource
    private IWaybillCarDao waybillCarDao;
    @Resource
    private IDriverDao driverDao;
    @Resource
    private RedisDistributedLock redisLock;
    @Resource
    private ISendNoService sendNoService;
    @Resource
    private ITaskDao taskDao;
    @Resource
    private StringRedisUtil redisUtil;
    @Resource
    private ITaskCarDao taskCarDao;


    @Override
    public ResultVo allot(AllotTaskDto paramsDto) {
        Long waybillId = paramsDto.getWaybillId();
        Long driverId = paramsDto.getDriverId();
        List<Long> waybillCarIdList = paramsDto.getWaybillCarIdList();
        Set<String> lockKeySet = new HashSet<>();
        try {
            //验证运单状态
            Waybill waybill = waybillDao.selectById(waybillId);
            if (waybill == null
                    || waybill.getState() < WaybillStateEnum.WAIT_ALLOT.code
                    || waybill.getState() > WaybillStateEnum.FINISHED.code) {

                return BaseResultUtil.fail("当前运单的状态，无法分配任务");
            }
            List<WaybillCar> list = waybillCarDao.findVoByIds(waybillCarIdList);
            if(list == null || list.isEmpty()){
                return BaseResultUtil.fail("内容不能为空");
            }
            //验证司机信息
            Driver driver = driverDao.selectById(driverId);
            if(driver == null || driver.getState() != 1 ){
                return BaseResultUtil.fail("司机不在运营中");
            }

            Task task = new Task();
            task.setNo(sendNoService.getNo(SendNoTypeEnum.TASK));
            task.setWaybillId(waybill.getId());
            task.setWaybillNo(waybill.getNo());
            task.setCarNum(paramsDto.getWaybillCarIdList().size());
            task.setState(TaskStateEnum.WAIT_ALLOT_CONFIRM.code);
            task.setDriverId(driver.getId());
            task.setDriverPhone(driver.getPhone());
            task.setDriverName(driver.getName());
            task.setCreateTime(System.currentTimeMillis());
            task.setCreateUser(paramsDto.getUserName());
            task.setCreateUserId(paramsDto.getUserId());
            taskDao.insert(task);

            List<TaskCar> saveTaskCarList = new ArrayList<>();
            int noCount = 0;
            for (WaybillCar waybillCar : list) {
                if(waybillCar == null){
                    continue;
                }
                //加锁
                String lockKey = RedisKeys.getAllotTaskKey(waybillCar.getOrderCarNo());
                if(!redisLock.lock(lockKey, 20000, 100, 300)){
                    throw new ServerException("当前运单的状态，无法分配任务");
                }
                lockKeySet.add(lockKey);

                //验证运单车辆状态
                if(waybillCar.getState() > WaybillCarStateEnum.WAIT_ALLOT.code){
                    throw new ServerException("当前运单车辆的状态，无法分配任务");
                }

                TaskCar taskCar = new TaskCar();
                taskCar.setTaskId(task.getId());
                taskCar.setWaybillCarId(waybillCar.getId());
                taskCarDao.insert(taskCar);
                noCount++;
            }

            if(noCount != task.getCarNum()){
                task.setCarNum(noCount);
                taskDao.updateById(task);
            }

        } finally {
            for (String key : lockKeySet) {
                redisLock.releaseLock(key);
            }
        }
        return null;
    }

    @Override
    public ResultVo load(LoadTaskDto paramsDto) {
        Long taskCarId = paramsDto.getTaskCarId();
        WaybillCar waybillCar = waybillCarDao.findByTaskCarId(taskCarId);
        if(waybillCar == null){
            return BaseResultUtil.fail("运单车辆不存在");
        }
        if(waybillCar.getState() > WaybillCarStateEnum.LOADED.code){
            return BaseResultUtil.fail("车辆已经装过车");
        }
        waybillCar.setState(WaybillCarStateEnum.LOADED.code);
        waybillCar.setLoadPhotoImg(paramsDto.getLoadPhotoImg());
        waybillCar.setLoadTime(System.currentTimeMillis());
        waybillCarDao.updateById(waybillCar);

        //TODO 给客户发送消息
        //TODO 写物流轨迹
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo unload(UnLoadTaskDto paramsDto) {
        Map<String, Object> failCarNoMap = new HashMap<>();

        List<Long> taskCarIdList = paramsDto.getTaskCarIdList();
        if (taskCarIdList == null || taskCarIdList.isEmpty()){
            return BaseResultUtil.fail("车辆不能为空");
        }
        for (Long taskCarId : taskCarIdList) {
            if(taskCarId == null){
                continue;
            }
            WaybillCar waybillCar = waybillCarDao.findByTaskCarId(taskCarId);
            if(waybillCar == null){
                failCarNoMap.put(waybillCar.getOrderCarNo(), "运单车辆不存在");
                continue;
            }
            if(waybillCar.getState() > WaybillCarStateEnum.UNLOADED.code){
                failCarNoMap.put(waybillCar.getOrderCarNo(), "车辆已经卸过车");
                continue;
            }
            waybillCar.setState(WaybillCarStateEnum.UNLOADED.code);
            waybillCar.setUnloadTime(System.currentTimeMillis());
            waybillCarDao.updateById(waybillCar);
        }

        return BaseResultUtil.success();
    }

    @Override
    public ResultVo inStore(InStoreTaskDto paramsDto) {
        Map<String, Object> failCarNoMap = new HashMap<>();
        long currentTimeMillis = System.currentTimeMillis();

        List<Long> taskCarIdList = paramsDto.getTaskCarIdList();
        if (taskCarIdList == null || taskCarIdList.isEmpty()){
            return BaseResultUtil.fail("车辆不能为空");
        }
        for (Long taskCarId : taskCarIdList) {
            if(taskCarId == null){
                continue;
            }
            WaybillCar waybillCar = waybillCarDao.findByTaskCarId(taskCarId);
            if(waybillCar == null){
                failCarNoMap.put(waybillCar.getOrderCarNo(), "运单车辆不存在");
                continue;
            }
            if(waybillCar.getState() > WaybillCarStateEnum.UNLOADED.code){
                failCarNoMap.put(waybillCar.getOrderCarNo(), "车辆已经卸过车");
                continue;
            }
            String endAddress = (waybillCar.getEndProvince() == null? "" : waybillCar.getEndProvince())
                    + (waybillCar.getEndCity() == null ? "" : waybillCar.getEndCity())
                    + (waybillCar.getEndArea() == null ? "" : waybillCar.getEndArea())
                    + (waybillCar.getEndAddress() == null ? "" : waybillCar.getEndAddress());
            //验证卸车目的地
            if(waybillCar.getEndStoreId() == null){
                failCarNoMap.put(waybillCar.getOrderCarNo(), MessageFormat.format("车辆只能卸在:{0}", endAddress));
                continue;
            }
            if(!waybillCar.getEndStoreId().equals(paramsDto.getStoreId())){
                failCarNoMap.put(waybillCar.getOrderCarNo(), MessageFormat.format("车辆只能卸在:{0}业务员中心, 地址：{}", waybillCar.getEndStoreName(), endAddress));
                continue;
            }




            //验证车辆是否到达目的地
            Long orderCarId = waybillCar.getOrderCarId();
            OrderCarVo orderCarVo = orderCarDao.findExtraById(orderCarId);
            if(orderCarVo == null){
                failCarNoMap.put(waybillCar.getOrderCarNo(), "车辆不存在");
                continue;
            }
            //int state = getOrderCarState(orderCarVo);
            //orderCarDao.updateStateById(OrderCarStateEnum., orderCarId)

            waybillCar.setState(WaybillCarStateEnum.CONFIRMED.code);
            waybillCar.setUnloadTime(currentTimeMillis);
            waybillCarDao.updateById(waybillCar);

        }

        return BaseResultUtil.success(failCarNoMap);
    }

    /**
     * 计算车辆状态
     * @author JPG
     * @since 2019/10/31 13:19
     * @param orderCarVo
     */
    private Integer getOrderCarState(OrderCarVo orderCarVo, Long nowStoreId) {
        if(orderCarVo == null || nowStoreId == null){
            return null;
        }
        //计算是否到达目的地业务中心
        if(orderCarVo.getEndStoreId() != null){
            if(orderCarVo.getEndStoreId().equals(nowStoreId)){
                //return OrderCarStateEnum.wai
            }
        }
        return null;
    }

    @Override
    public ResultVo outStore(OutStoreTaskDto reqDto) {
        return null;
    }

    @Override
    public ResultVo sign(SignTaskDto reqDto) {
        return null;
    }

}

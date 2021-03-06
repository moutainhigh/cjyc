package com.cjyc.common.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cjkj.common.model.ResultData;
import com.cjkj.common.model.ReturnMsg;
import com.cjkj.usercenter.dto.common.UpdateDeptReq;
import com.cjkj.usercenter.dto.common.UpdateUserReq;
import com.cjkj.usercenter.dto.common.UserResp;
import com.cjkj.usercenter.dto.yc.AddDeptAndUserReq;
import com.cjkj.usercenter.dto.yc.AddDeptAndUserResp;
import com.cjkj.usercenter.dto.yc.UpdateDeptManagerReq;
import com.cjyc.common.model.dao.ICarrierDao;
import com.cjyc.common.model.dao.ICarrierDriverConDao;
import com.cjyc.common.model.dao.IDriverDao;
import com.cjyc.common.model.dao.ITaskDao;
import com.cjyc.common.model.dto.web.carrier.CarrierDto;
import com.cjyc.common.model.dto.web.carrier.DispatchCarrierDto;
import com.cjyc.common.model.dto.web.carrier.TrailCarrierDto;
import com.cjyc.common.model.entity.Carrier;
import com.cjyc.common.model.entity.CarrierDriverCon;
import com.cjyc.common.model.entity.Driver;
import com.cjyc.common.model.entity.Task;
import com.cjyc.common.model.enums.CommonStateEnum;
import com.cjyc.common.model.enums.task.TaskStateEnum;
import com.cjyc.common.model.enums.transport.BusinessStateEnum;
import com.cjyc.common.model.enums.transport.DriverRoleEnum;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.util.JsonUtils;
import com.cjyc.common.model.util.YmlProperty;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.carrier.DispatchCarrierVo;
import com.cjyc.common.model.vo.web.carrier.TrailCarrierVo;
import com.cjyc.common.system.config.CarrierProperty;
import com.cjyc.common.system.feign.ISysDeptService;
import com.cjyc.common.system.feign.ISysUserService;
import com.cjyc.common.system.service.ICsCarrierService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CsCarrierServiceImpl implements ICsCarrierService {
    @Resource
    private ICarrierDao carrierDao;
    @Resource
    private IDriverDao driverDao;
    @Resource
    private ITaskDao taskDao;
    @Resource
    private ICarrierDriverConDao carrierDriverConDao;
    @Resource
    private ISysUserService sysUserService;
    @Resource
    private ISysDeptService sysDeptService;


    @Override
    public ResultData<AddDeptAndUserResp> saveCarrierToPlatform(Carrier carrier) {

        List<Long> menuIds = CarrierProperty.carriesMenuIds;
        AddDeptAndUserReq deptReq = new AddDeptAndUserReq();
        deptReq.setName(carrier.getName());
        deptReq.setLegalPerson(carrier.getLegalName());
        deptReq.setDeptPerson(carrier.getLinkman());
        deptReq.setTelephone(carrier.getLinkmanPhone());
        deptReq.setPassword(YmlProperty.get("cjkj.driver.password"));
        if (menuIds != null && menuIds.size() > 0) {
            deptReq.setMenuIdList(menuIds);
        }
        List<Long> mgrMenuIds = CarrierProperty.carriesMgrMenuIds;
        if (!CollectionUtils.isEmpty(mgrMenuIds)) {
            deptReq.setMgrMenuIdList(mgrMenuIds);
        }
        ResultData<AddDeptAndUserResp> rd = sysDeptService.saveDeptAndUser(deptReq);
        return rd;
    }

    @Override
    public List<Carrier> getBelongListByDriver(Long driverId) {
        return carrierDao.findBelongListByDriverId(driverId);
    }

    @Override
    public List<Long> getBelongIdsListByDriver(Long driverId) {
        return carrierDao.getBelongIdsListByDriver(driverId);
    }
    @Override
    public ResultData<Long> updateCarrierToPlatform(Carrier carrier, CarrierDto dto) {
        //1.判断 carrier不为空 且 deptId 不为空（物流平台存在此机构)
        Long userId = null;
        if (null != carrier) {
            if (carrier.getDeptId() != null && carrier.getDeptId() > 0L) {
                if (!carrier.getLinkmanPhone().equals(dto.getLinkmanPhone())) {
                    //需要变更联系人
                    //1.新联系人是否在物流平台
                    ResultData<UserResp> accountRd = sysUserService.getByAccount(dto.getLinkmanPhone());
                    if (!ReturnMsg.SUCCESS.getCode().equals(accountRd.getCode())) {
                        return ResultData.failed("查询用户信息失败，原因：" + accountRd.getMsg());
                    }
                    CarrierDriverCon origCdCon = carrierDriverConDao.selectOne(new QueryWrapper<CarrierDriverCon>()
                            .eq("carrier_id", carrier.getId())
                            .eq("role", DriverRoleEnum.SUPERADMIN.code));
                    if (null == origCdCon) {
                        return ResultData.failed("数据异常，此承运商下无机构管理员");
                    }
                    if (accountRd.getData() != null) {
                        //2.如果存在：需要做用户角色关系管理及新用户校验
                        //2.1 新用户必须是此承运商司机
                        Driver driver = driverDao.selectOne(new QueryWrapper<Driver>()
                                .eq("user_id", accountRd.getData().getUserId()));
                        if (null == driver) {
                            return ResultData.failed("司机数据查询错误，根据user_id: " + accountRd.getData().getUserId() +
                                    "未查询到司机信息");
                        }
                        CarrierDriverCon cdCon = carrierDriverConDao.selectOne(new QueryWrapper<CarrierDriverCon>()
                                .eq("carrier_id", carrier.getId())
                                .eq("driver_id", driver.getId()));
                        if (null == cdCon) {
                            return ResultData.failed("新用户不属于此承运商");
                        }
                        //2.2 更新承运商信息及更换机构管理员
                        UpdateDeptManagerReq managerReq = new UpdateDeptManagerReq();
                        managerReq.setDeptId(carrier.getDeptId());
                        managerReq.setInitUserId(origCdCon.getDriverId());
                        managerReq.setNewUserId(accountRd.getData().getUserId());
                        ResultData rd = sysDeptService.updateDeptManager(managerReq);
                        if (!ReturnMsg.SUCCESS.getCode().equals(rd.getCode())) {
                            return ResultData.failed("变更机构管理员失败，原因：" + rd.getMsg());
                        }else {
                            //将原司机-承运商关系设置为普通角色
                            CarrierDriverCon carrierDriverCon = carrierDriverConDao.selectOne(new QueryWrapper<CarrierDriverCon>()
                                    .eq("carrier_id", dto.getCarrierId())
                                    .eq("role", DriverRoleEnum.SUPERADMIN.code));
                            if (carrierDriverCon != null) {
                                carrierDriverCon.setRole(DriverRoleEnum.SUB_DRIVER.code);
                                carrierDriverConDao.updateById(carrierDriverCon);
                            }
                            //新司机-承运商关系设置为管理员
                            cdCon.setMode(dto.getMode());
                            cdCon.setState(CommonStateEnum.WAIT_CHECK.code);
                            cdCon.setRole(DriverRoleEnum.SUPERADMIN.code);
                            carrierDriverConDao.updateById(cdCon);
                        }
                        userId = accountRd.getData().getUserId();
                    }else {
                        //3.如果不存在，直接将联系人手机号及姓名更新即可
                        UpdateUserReq userReq = new UpdateUserReq();
                        userReq.setUserId(origCdCon.getDriverId());
                        userReq.setAccount(dto.getLinkmanPhone());
                        userReq.setMobile(dto.getLinkmanPhone());
                        userReq.setName(dto.getLinkman());
                        ResultData updateRd = sysUserService.update(userReq);
                        if (!ReturnMsg.SUCCESS.getCode().equals(updateRd.getCode())) {
                            return ResultData.failed("更新管理员信息错误，原因：" + updateRd.getMsg());
                        }else {
                            CarrierDriverCon carrierDriverCon = carrierDriverConDao.selectOne(new QueryWrapper<CarrierDriverCon>()
                                    .eq("carrier_id", dto.getCarrierId())
                                    .eq("role", DriverRoleEnum.SUPERADMIN.code));
                            Driver driver = driverDao.selectById(carrierDriverCon.getDriverId());
                            driver.setName(dto.getLinkman());
                            driver.setRealName(dto.getLinkman());
                            driver.setPhone(dto.getLinkmanPhone());
                            driverDao.updateById(driver);
                            userId = driver.getUserId();
                        }
                    }
                }

                //将企业名称、法人姓名信息更新即可
                if (!carrier.getName().equals(dto.getName())
                        || !carrier.getLegalName().equals(dto.getLegalName())) {
                    UpdateDeptReq deptReq = new UpdateDeptReq();
                    deptReq.setDeptId(carrier.getDeptId());
                    deptReq.setName(dto.getName());
                    deptReq.setLegalPerson(carrier.getLegalName());
                    return sysDeptService.update(deptReq);
                }
                return ResultData.ok(userId, "成功");
            }
            return ResultData.ok("数据未同步到物流平台，不需要变更");
        }
        return ResultData.ok("无变更：数据不存在");
    }

    @Override
    public ResultVo<PageVo<TrailCarrierVo>> trailDriver(TrailCarrierDto dto) {
        PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        List<TrailCarrierVo> carrierVos = carrierDao.findTrailDriver(dto);
        PageInfo<TrailCarrierVo> pageInfo =  new PageInfo<>(carrierVos);
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public ResultVo<PageVo<DispatchCarrierVo>> dispatchCarrier(DispatchCarrierDto dto) {
        PageHelper.startPage(dto.getCurrentPage(),dto.getPageSize());
        List<DispatchCarrierVo> carrierVos = carrierDao.findDispatchCarrier(dto);
        PageInfo<DispatchCarrierVo> pageInfo = new PageInfo<>(carrierVos);
        return BaseResultUtil.success(pageInfo);
    }



    /*********************************韵车集成改版 st*****************************/
    @Override
    public ResultVo<PageVo<TrailCarrierVo>> trailDriverNew(TrailCarrierDto dto) {
        log.info("调度中心中提车/送车调度中代驾和拖车列表请求json数据 :: "+ JsonUtils.objectToJson(dto));
        PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        List<TrailCarrierVo> carrierVos = carrierDao.findTrailDriverNew(dto);
        if(!CollectionUtils.isEmpty(carrierVos)){
            for(TrailCarrierVo vo : carrierVos){
                //处理该司机当前营运状态
                Integer taskCount = taskDao.selectCount(new QueryWrapper<Task>().lambda()
                        .eq(Task::getDriverId, vo.getDriverId())
                        .lt(Task::getState, TaskStateEnum.FINISHED.code));
                if(taskCount > 0){
                    vo.setBusinessState(BusinessStateEnum.OUTAGE.code);
                }else{
                    vo.setBusinessState(BusinessStateEnum.BUSINESS.code);
                }
            }
        }
        PageInfo<TrailCarrierVo> pageInfo =  new PageInfo<>(carrierVos);
        return BaseResultUtil.success(pageInfo);
    }


    @Override
    public ResultVo<PageVo<TrailCarrierVo>> trailAppDriverNew(TrailCarrierDto dto) {
        log.info("调度中心中提车/送车调度中代驾和拖车列表请求json数据 :: "+ JsonUtils.objectToJson(dto));
        PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        dto.setKeyword(dto.getPhone());
        List<TrailCarrierVo> carrierVos = carrierDao.findAppTrailDriverNew(dto);
        PageInfo<TrailCarrierVo> pageInfo =  new PageInfo<>(carrierVos);
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public ResultVo<PageVo<DispatchCarrierVo>> dispatchAppCarrier(DispatchCarrierDto dto) {
        log.info("调度中心中干线调度承运商信息请求json数据 :: "+ JsonUtils.objectToJson(dto));
        PageHelper.startPage(dto.getCurrentPage(),dto.getPageSize());
        dto.setKeyword(dto.getLinkmanPhone());
        List<DispatchCarrierVo> carrierVos = carrierDao.findAppDispatchCarrier(dto);
        PageInfo<DispatchCarrierVo> pageInfo = new PageInfo<>(carrierVos);
        return BaseResultUtil.success(pageInfo);
    }

}

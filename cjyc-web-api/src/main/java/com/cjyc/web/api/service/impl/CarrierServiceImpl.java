package com.cjyc.web.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjkj.common.model.ResultData;
import com.cjkj.common.model.ReturnMsg;
import com.cjkj.usercenter.dto.yc.AddDeptAndUserReq;
import com.cjkj.usercenter.dto.yc.AddDeptAndUserResp;
import com.cjyc.common.model.dao.*;
import com.cjyc.common.model.dto.web.OperateDto;
import com.cjyc.common.model.dto.web.carrier.CarrierDto;
import com.cjyc.common.model.dto.web.carrier.SeleCarrierDto;
import com.cjyc.common.model.dto.web.carrier.SeleVehicleDriverDto;
import com.cjyc.common.model.entity.*;
import com.cjyc.common.model.enums.*;
import com.cjyc.common.model.enums.transport.*;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.util.LocalDateTimeUtil;
import com.cjyc.common.model.util.YmlProperty;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.carrier.BaseCarrierVo;
import com.cjyc.common.model.vo.web.carrier.BaseDriverVo;
import com.cjyc.common.model.vo.web.carrier.BaseVehicleVo;
import com.cjyc.common.model.vo.web.carrier.CarrierVo;
import com.cjyc.web.api.exception.CommonException;
import com.cjyc.common.system.feign.ISysDeptService;
import com.cjyc.common.system.feign.ISysUserService;
import com.cjyc.web.api.service.ICarrierCityConService;
import com.cjyc.web.api.service.ICarrierDriverConService;
import com.cjyc.web.api.service.ICarrierService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
public class CarrierServiceImpl extends ServiceImpl<ICarrierDao, Carrier> implements ICarrierService {

    @Resource
    private IAdminDao adminDao;

    @Resource
    private ICarrierDao carrierDao;

    @Resource
    private IDriverDao driverDao;

    @Resource
    private IVehicleDao vehicleDao;

    @Resource
    private ICarrierDriverConDao carrierDriverConDao;

    @Resource
    private IBankCardBindDao bankCardBindDao;

    @Resource
    private ICarrierCarCountDao carrierCarCountDao;

    @Resource
    private ICarrierCityConDao carrierCityConDao;

    @Resource
    private ICarrierCityConService carrierCityConService;

    @Resource
    private ICarrierDriverConService carrierDriverConService;

    @Autowired
    private ISysDeptService sysDeptService;

    @Autowired
    private ISysUserService sysUserService;

    @Value("${cjkj.carries_menu_ids}")
    private Long[] menuIds;

    private static final Long NOW = LocalDateTimeUtil.getMillisByLDT(LocalDateTime.now());

    @Override
    public boolean saveCarrier(CarrierDto dto) {
        //添加承运商
        Carrier carrier = new Carrier();
        BeanUtils.copyProperties(dto,carrier);
        carrier.setState(CommonStateEnum.WAIT_CHECK.code);
        carrier.setType(CarrierTypeEnum.ENTERPRISE.code);
        carrier.setBusinessState(BusinessStateEnum.BUSINESS.code);
        carrier.setCreateUserId(dto.getUserId());
        carrier.setCreateTime(NOW);
        carrier.setOperateUserId(dto.getUserId());
        carrier.setOperateTime(NOW);
        Admin admin = adminDao.selectOne(new QueryWrapper<Admin>().lambda().eq(Admin::getUserId, dto.getUserId()).select(Admin::getName));
        if(admin != null){
            carrier.setOperateName(admin.getName());
        }
        super.save(carrier);

        //添加承运商司机管理员
        Driver driver = new Driver();
        driver.setName(dto.getName());
        driver.setPhone(dto.getLinkmanPhone());
        driver.setIdentity(DriverIdentityEnum.ADMIN.code);
        driver.setState(CommonStateEnum.CHECKED.code);
        driver.setBusinessState(BusinessStateEnum.BUSINESS.code);
        driver.setSource(DriverSourceEnum.SALEMAN_WEB.code);
        driver.setIdCard(dto.getLegalIdCard());
        driver.setRealName(dto.getLinkman());
        driver.setCreateUserId(dto.getUserId());
        driver.setCreateTime(NOW);
        if(admin != null){
            driver.setOperateName(admin.getName());
        }
        driver.setOperateTime(NOW);
        driver.setOperateUserId(dto.getUserId());
        driverDao.insert(driver);

        //承运商与司机绑定关系
        CarrierDriverCon cdc = new CarrierDriverCon();
        cdc.setDriverId(driver.getId());
        cdc.setCarrierId(carrier.getId());
        cdc.setRole(DriverIdentityEnum.SUPERADMIN.code);
        carrierDriverConDao.insert(cdc);

        //保存银行卡信息
        BankCardBind bcb = new BankCardBind();
        BeanUtils.copyProperties(dto,bcb);
        bcb.setUserId(driver.getUserId());
        bcb.setUserType(UserTypeEnum.DRIVER.code);
        bcb.setIdCard(dto.getLegalIdCard());
        bcb.setState(UseStateEnum.USABLE.code);
        bcb.setCreateTime(NOW);
        bcb.setCardPhone(dto.getLinkmanPhone());
        bankCardBindDao.insert(bcb);
        //添加承运商业务范围
        return carrierCityConService.batchSave(carrier.getId(),dto.getCodes());
    }

    @Override
    public boolean modifyCarrier(CarrierDto dto) {
        //更新承运商
        Carrier carrier = new Carrier();
        BeanUtils.copyProperties(dto,carrier);
        Admin admin = adminDao.selectOne(new QueryWrapper<Admin>().lambda().eq(Admin::getUserId, dto.getUserId()).select(Admin::getName));
        if(admin != null){
            carrier.setOperateName(admin.getName());
        }
        carrier.setOperateTime(NOW);
        carrier.setOperateUserId(dto.getUserId());
        super.updateById(carrier);
        //更新承运商司机管理员
        Driver driver = driverDao.getDriverByDriverId(dto.getId());
        if(driver != null){
            driver.setPhone(dto.getLinkmanPhone());
            driver.setName(dto.getName());
            driver.setRealName(dto.getLinkman());
            driver.setIdCard(dto.getLegalIdCard());
            driverDao.updateById(driver);
        }
        //更新银行卡信息
        BankCardBind bcb = bankCardBindDao.getBankCardBindByUserId(driver.getUserId());
        if(bcb != null){
            bcb.setCardType(dto.getCardType());
            bcb.setCardNo(dto.getCardNo());
            bcb.setBankName(dto.getBankName());
            bcb.setIdCard(dto.getLegalIdCard());
            bcb.setCardPhone(dto.getLinkmanPhone());
            bankCardBindDao.updateById(bcb);
        }
        //承运商业务范围,先批量删除，再添加
        carrierCityConService.batchDelete(carrier.getId());
        return carrierCityConService.batchSave(carrier.getId(),dto.getCodes());
    }

    @Override
    public ResultVo findCarrier(SeleCarrierDto dto) {
        PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        List<CarrierVo> carrierVos = carrierDao.getCarrierByTerm(dto);
        if(!CollectionUtils.isEmpty(carrierVos)){
            for(CarrierVo vo : carrierVos){
                CarrierCarCount count = carrierCarCountDao.count(vo.getId());
                if(count != null){
                    vo.setCarNum(count.getCarNum() == null ? 0:count.getCarNum());
                    vo.setTotalIncome(count.getIncome() == null ? BigDecimal.ZERO:count.getIncome().divide(new BigDecimal(100)));
                }
            }
        }
        PageInfo<CarrierVo> pageInfo =  new PageInfo<>(carrierVos);
        return BaseResultUtil.success(pageInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean verifyCarrier(OperateDto dto) {
        //审核状态 1:审核通过 2:审核拒绝 3：冻结 4:解除
        Carrier carrier = carrierDao.selectById(dto.getId());
        //审核通过
        if(FlagEnum.AUDIT_PASS.code == dto.getFlag()){
            //审核通过将承运商信息同步到物流平台
            ResultData<AddDeptAndUserResp> rd = saveCarrierToPlatform(carrier);
            if (!ReturnMsg.SUCCESS.getCode().equals(rd.getCode())) {
                throw new CommonException("承运商机构添加失败");
            }
            //更新司机userID信息
            updateDriver(rd.getData().getUserId(), carrier.getId());
            //更新绑定银行卡信息
            updateBank(rd.getData().getUserId(),carrier.getLegalIdCard());
            carrier.setDeptId(rd.getData().getDeptId());
            carrier.setState(CommonStateEnum.CHECKED.code);
        }else if(FlagEnum.AUDIT_REJECT.code == dto.getFlag()){
            //审核拒绝
            carrier.setState(CommonStateEnum.REJECT.code);
        }else if(FlagEnum.FROZEN.code == dto.getFlag()){
            //冻结
            carrier.setState(CommonStateEnum.FROZEN.code);
        }else if(FlagEnum.THAW.code == dto.getFlag()){
            //解冻
            carrier.setState(CommonStateEnum.CHECKED.code);
        }
        carrier.setOperateTime(NOW);
        carrier.setOperateUserId(dto.getUserId());
        Admin admin = adminDao.selectOne(new QueryWrapper<Admin>().lambda().eq(Admin::getUserId, dto.getUserId()).select(Admin::getName));
        if(admin != null){
            carrier.setOperateName(admin.getName());
        }
        return super.updateById(carrier);
    }

    @Override
    public ResultVo showBaseCarrier(Long carrierId) {
        BaseCarrierVo vo = carrierDao.showCarrierById(carrierId);
        if(vo != null){
            vo.setMapCodes(carrierCityConService.getMapCodes(carrierId));
        }
        return BaseResultUtil.success(vo);
    }

    @Override
    public PageInfo<BaseVehicleVo> getBaseVehicleByTerm(SeleVehicleDriverDto dto) {
        PageInfo<BaseVehicleVo> pageInfo = null;
        try{
            List<BaseVehicleVo> baseVehicleVos = vehicleDao.getBaseVehicleByTerm(dto);
            PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
            pageInfo = new PageInfo<>(baseVehicleVos);
        }catch (Exception e){
            log.info("根据条件查看车辆信息出现异常");
        }
        return pageInfo;
    }

    @Override
    public ResultVo getBaseDriverByTerm(SeleVehicleDriverDto dto) {
        PageInfo<BaseDriverVo> pageInfo = null;
        try{
            List<Long> idsList =  carrierDriverConService.getDriverIds(dto.getId());
            if(!idsList.isEmpty() && !CollectionUtils.isEmpty(idsList)){
                List<BaseDriverVo> driverVos = driverDao.getDriversByIds(idsList);
                if(!driverVos.isEmpty() && CollectionUtils.isEmpty(driverVos)){
                    PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
                    pageInfo = new PageInfo<>(driverVos);
                    return BaseResultUtil.getPageVo(ResultEnum.SUCCESS.getCode(),ResultEnum.SUCCESS.getMsg(),pageInfo);
                }
            }
        }catch (Exception e){
            log.info("根据承运商id查看司机信息出现异常");
            throw new CommonException(e.getMessage());
        }
        return BaseResultUtil.getPageVo(ResultEnum.FAIL.getCode(),ResultEnum.FAIL.getMsg(),pageInfo);
    }

    @Override
    public ResultVo resetPwd(Long id) {
        CarrierDriverCon driverCon = carrierDriverConDao.selectOne(new QueryWrapper<CarrierDriverCon>()
                .eq("carrier_id", id)
                .eq("role", DriverIdentityEnum.SUPERADMIN.code));
        if (driverCon != null) {
            Driver driver = driverDao.selectById(driverCon.getDriverId());
            if (driver != null) {
                ResultData rd =
                        sysUserService.resetPwd(driver.getUserId(), YmlProperty.get("cjkj.salesman.password"));
                if (ReturnMsg.SUCCESS.getCode().equals(rd.getCode())) {
                    return BaseResultUtil.success();
                }else {
                    return BaseResultUtil.fail(rd.getMsg());
                }
            }
        }
        return BaseResultUtil.fail("用户信息有误，请检查");
    }

    /**
     * 将承运商相关信息保存到物流平台
     * @param carrier 承运商信息
     * @return
     */
    private ResultData<AddDeptAndUserResp> saveCarrierToPlatform(Carrier carrier) {
        if (null == carrier) {
            return ResultData.failed("承运商信息错误，请检查");
        }
        AddDeptAndUserReq deptReq = new AddDeptAndUserReq();
        deptReq.setName(carrier.getName());
        deptReq.setLegalPerson(carrier.getLegalName());
        deptReq.setDeptPerson(carrier.getLinkman());
        deptReq.setTelephone(carrier.getLinkmanPhone());
        deptReq.setPassword(YmlProperty.get("cjkj.salesman.password"));
        if (menuIds != null && menuIds.length > 0) {
            deptReq.setMenuIdList(Arrays.asList(menuIds));
        }
        ResultData<AddDeptAndUserResp> rd = sysDeptService.saveDeptAndUser(deptReq);
        return rd;
    }

    /**
     * 保存司机及承运商-司机关联关系
     * @param userId
     * @param carrierId
     */
    private void updateDriver(Long userId, Long carrierId) {
        List<CarrierDriverCon> conList = carrierDriverConDao.selectList(new QueryWrapper<CarrierDriverCon>()
                .eq("carrier_id", carrierId)
                .eq("role", DriverIdentityEnum.SUPERADMIN.code));
        Long driverId = null;
        if (!CollectionUtils.isEmpty(conList)) {
            driverId = conList.get(0).getDriverId();
        }
        if (driverId != null) {
            Driver driver = driverDao.selectById(driverId);
            if (driver != null) {
                driver.setUserId(userId);
                driverDao.updateById(driver);
            }
        }
    }

    /**
     * 根据承运商身份证号更新userId
     * @param userId
     * @param idCard
     */
    private void updateBank(Long userId, String idCard){
        List<BankCardBind> bankList = bankCardBindDao.selectList(new QueryWrapper<BankCardBind>()
                .eq("id_card", idCard));
        Long bankId = null;
        if (!CollectionUtils.isEmpty(bankList)) {
            bankId = bankList.get(0).getId();
        }
        if (bankId != null) {
            BankCardBind bcb = bankCardBindDao.selectById(bankId);
            if (bcb != null) {
                bcb.setUserId(userId);
                bankCardBindDao.updateById(bcb);
            }
        }
    }

}
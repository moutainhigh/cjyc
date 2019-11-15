package com.cjyc.web.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjkj.common.model.ResultData;
import com.cjkj.common.model.ReturnMsg;
import com.cjkj.usercenter.dto.common.AddUserReq;
import com.cjkj.usercenter.dto.common.AddUserResp;
import com.cjkj.usercenter.dto.common.UpdateUserReq;
import com.cjyc.common.model.constant.TimePatternConstant;
import com.cjyc.common.model.dao.*;
import com.cjyc.common.model.dto.web.OperateDto;
import com.cjyc.common.model.dto.web.customer.*;
import com.cjyc.common.model.entity.*;
import com.cjyc.common.model.enums.*;
import com.cjyc.common.model.enums.coupon.CouponLifeTypeEnum;
import com.cjyc.common.model.enums.customer.CustomerPayEnum;
import com.cjyc.common.model.enums.customer.CustomerSourceEnum;
import com.cjyc.common.model.enums.customer.CustomerStateEnum;
import com.cjyc.common.model.enums.customer.CustomerTypeEnum;
import com.cjyc.common.model.exception.ServerException;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.util.LocalDateTimeUtil;
import com.cjyc.common.model.util.YmlProperty;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.customer.*;
import com.cjyc.common.model.vo.web.coupon.CustomerCouponSendVo;
import com.cjyc.web.api.exception.CommonException;
import com.cjyc.common.system.feign.ISysUserService;
import com.cjyc.web.api.service.ICustomerContractService;
import com.cjyc.web.api.service.ICustomerService;
import com.cjyc.web.api.service.ISendNoService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 *  @author: zj
 *  @Date: 2019/9/29 15:20
 *  @Description: 移动用户
 */
@Service
@Slf4j
public class CustomerServiceImpl extends ServiceImpl<ICustomerDao,Customer> implements ICustomerService{

    @Resource
    private ICustomerDao customerDao;

    @Resource
    private ICustomerContractDao customerContractDao;

    @Resource
    private ICustomerContractService customerContractService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private ICustomerPartnerDao customerPartnerDao;

    @Resource
    private IBankCardBindDao bankCardBindDao;

    @Resource
    private ICouponDao couponDao;

    @Resource
    private ICouponSendDao couponSendDao;

    @Resource
    private IAdminDao adminDao;

    @Resource
    private IDriverDao driverDao;

    @Resource
    private ICustomerCountDao customerCountDao;

    @Resource
    private ISendNoService sendNoService;

    private static final Long NOW = LocalDateTimeUtil.getMillisByLDT(LocalDateTime.now());

    @Override
    public ResultVo existCustomer(ExistCustomreDto dto) {
        Customer c = customerDao.selectOne(new QueryWrapper<Customer>().lambda()
                        .eq(Customer::getContactPhone, dto.getPhone())
                        .ne((dto.getCustomerId() != null && dto.getCustomerId() != 0),Customer::getId,dto.getCustomerId()));
        if(c != null){
            return BaseResultUtil.fail("该客户已存在，请检查");
        }
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo saveCustomer(CustomerDto dto) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto,customer);
        customer.setCustomerNo(sendNoService.getNo(SendNoTypeEnum.CUSTOMER));
        customer.setAlias(dto.getName());
        customer.setIsDelete(DeleteStateEnum.NO_DELETE.code);
        customer.setContactMan(dto.getName());
        customer.setType(CustomerTypeEnum.INDIVIDUAL.code);
        customer.setState(CustomerStateEnum.CHECKED.code);
        customer.setPayMode(CustomerPayEnum.TIME_PAY.code);
        customer.setSource(CustomerSourceEnum.WEB.code);
        customer.setRegisterTime(NOW);
        customer.setCreateUserId(dto.getLoginId());
        customer.setCreateTime(NOW);
        //新增个人用户信息到物流平台
        ResultData<Long> rd = addCustomerToPlatform(customer);
        if (!ReturnMsg.SUCCESS.getCode().equals(rd.getCode())) {
            return BaseResultUtil.fail(rd.getMsg());
        }
        customer.setUserId(rd.getData());
        super.save(customer);
        return BaseResultUtil.success();
    }


    @Override
    public ResultVo modifyCustomer(CustomerDto dto) {
        Customer customer = customerDao.selectById(dto.getCustomerId());
        if(null != customer){
            ResultData<Boolean> updateRd = updateCustomerToPlatform(customer, dto.getContactPhone());
            if (!ReturnMsg.SUCCESS.getCode().equals(updateRd.getCode())) {
                log.error("修改用户信息失败，原因：" + updateRd.getMsg());
                return BaseResultUtil.fail("修改用户信息失败，原因：" + updateRd.getMsg());
            }
            if (updateRd.getData()) {
                //需要同步手机号信息
                syncPhone(customer.getContactPhone(), dto.getContactPhone());
            }
            customer.setName(dto.getName());
            customer.setAlias(dto.getName());
            customer.setContactMan(dto.getName());
            customer.setContactPhone(dto.getContactPhone());
            customer.setIdCard(dto.getIdCard());
            customer.setIdCardFrontImg(dto.getIdCardFrontImg());
            customer.setIdCardBackImg(dto.getIdCardBackImg());
        }
        super.updateById(customer);
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo findCustomer(SelectCustomerDto dto) {
        PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        List<CustomerVo> customerVos = customerDao.findCustomer(dto);
        if(!CollectionUtils.isEmpty(customerVos)){
            for(CustomerVo vo : customerVos){
                CustomerCountVo count = customerCountDao.count(vo.getId());
                Admin admin = adminDao.selectById(vo.getCreateUserId());
                if(admin != null){
                    vo.setCreateUserName(StringUtils.isNotBlank(admin.getName()) == true ? admin.getName():"");
                }
                if(count != null){
                    vo.setTotalOrder(count.getTotalOrder() == null ? 0:count.getTotalOrder());
                    vo.setTotalCar(count.getTotalCar() == null ? 0:count.getTotalCar());
                    vo.setTotalAmount(count.getTotalAmount() == null ? BigDecimal.ZERO:count.getTotalAmount().divide(new BigDecimal(100)));
                }
                if(StringUtils.isNotBlank(vo.getCreateTime())){
                    Long createTime = Long.parseLong(vo.getCreateTime());
                    vo.setCreateTime(LocalDateTimeUtil.formatLDT(LocalDateTimeUtil.convertLongToLDT(createTime),TimePatternConstant.COMPLEX_TIME_FORMAT));
                }
            }
        }
        PageInfo<CustomerVo> pageInfo =  new PageInfo<>(customerVos);
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public ResultVo saveKeyCustomer(KeyCustomerDto dto) {
        //新增大客户
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto,customer);
        customer.setCustomerNo(sendNoService.getNo(SendNoTypeEnum.CUSTOMER));
        customer.setAlias(dto.getName());
        customer.setIsDelete(DeleteStateEnum.NO_DELETE.code);
        customer.setType(CustomerTypeEnum.ENTERPRISE.code);
        customer.setState(CustomerStateEnum.WAIT_LOGIN.code);
        customer.setSource(CustomerSourceEnum.WEB.code);
        customer.setRegisterTime(NOW);
        customer.setCreateTime(NOW);
        customer.setCreateUserId(dto.getLoginId());

        //保存大客户信息到物流平台
        ResultData<Long> rd = addCustomerToPlatform(customer);
        if (!ReturnMsg.SUCCESS.getCode().equals(rd.getCode())) {
            log.error("保存大客户信息失败，原因：" + rd.getMsg());
            return BaseResultUtil.fail("保存大客户信息失败，原因：" + rd.getMsg());
        }
        customer.setUserId(rd.getData());
        super.save(customer);
        //合同集合
        List<CustomerContractDto> customerConList = dto.getCustContraVos();
        List<CustomerContract> list = encapCustomerContract(customer.getId(),customerConList);
        customerContractService.saveBatch(list);
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo verifyCustomer(OperateDto dto) {
        Customer customer = customerDao.selectById(dto.getId());
        if(customer != null){
            Long now = LocalDateTimeUtil.getMillisByLDT(LocalDateTime.now());
            if(FlagEnum.DELETE.code == dto.getFlag()){
               //假删除
                customer.setIsDelete(DeleteStateEnum.YES_DELETE.code);
            }else if(FlagEnum.AUDIT_PASS.code == dto.getFlag()){
                //审核通过
                customer.setState(CustomerStateEnum.CHECKED.code);
                customer.setCheckTime(now);
                customer.setCheckUserId(dto.getLoginId());
            }else if(FlagEnum.AUDIT_REJECT.code == dto.getFlag()){
                //审核拒绝
                customer.setState(CustomerStateEnum.REJECT.code);
                customer.setCheckTime(now);
                customer.setCheckUserId(dto.getLoginId());
            }
        }
        super.updateById(customer);
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo<ShowKeyCustomerVo> showKeyCustomer(Long customerId) {
        ShowKeyCustomerVo sKeyCustomerDto = new ShowKeyCustomerVo();
        //根据主键id查询大客户
        Customer customer = customerDao.selectById(customerId);
        if(customer != null){
            BeanUtils.copyProperties(customer,sKeyCustomerDto);
        }
        //根据customer_user_id查询大客户的合同
        List<CustomerContractVo> contractVos = customerContractDao.getCustContractByCustId(customer.getId());
        if(!CollectionUtils.isEmpty(contractVos)){
            for(CustomerContractVo vo : contractVos){
                if(StringUtils.isNotBlank(vo.getContractLife())){
                    vo.setContractLife(LocalDateTimeUtil.formatLDT(LocalDateTimeUtil.convertLongToLDT(Long.parseLong(vo.getContractLife())),TimePatternConstant.SIMPLE_DATE_FORMAT));
                }
                if(StringUtils.isNotBlank(vo.getProjectEstabTime())){
                    vo.setProjectEstabTime(LocalDateTimeUtil.formatLDT(LocalDateTimeUtil.convertLongToLDT(Long.parseLong(vo.getProjectEstabTime())),TimePatternConstant.SIMPLE_DATE_FORMAT));
                }
                vo.setProTraVolume(vo.getProTraVolume() == null ? BigDecimal.ZERO:vo.getProTraVolume());
                vo.setAvgMthTraVolume(vo.getAvgMthTraVolume() == null ? BigDecimal.ZERO:vo.getAvgMthTraVolume());
            }
            sKeyCustomerDto.setCustContraVos(contractVos);
        }
        return BaseResultUtil.success(sKeyCustomerDto);
    }

    @Override
    public ResultVo modifyKeyCustomer(KeyCustomerDto dto) {
        Customer customer = customerDao.selectById(dto.getCustomerId());
        if(null != customer){
            //判断手机号是否存在
            ResultData<Boolean> updateRd = updateCustomerToPlatform(customer, dto.getContactPhone());
            if (!ReturnMsg.SUCCESS.getCode().equals(updateRd.getCode())) {
                log.error("修改用户信息失败，原因：" + updateRd.getMsg());
                return BaseResultUtil.fail("修改用户信息失败，原因：" + updateRd.getMsg());
            }
            if (updateRd.getData()) {
                //需要同步手机号信息
                syncPhone(customer.getContactPhone(), dto.getContactPhone());
            }
            customer.setName(dto.getName());
            customer.setAlias(dto.getName());
            customer.setContactMan(dto.getContactMan());
            customer.setSocialCreditCode(dto.getSocialCreditCode());
            customer.setContactPhone(dto.getContactPhone());
            customer.setContactAddress(dto.getContactAddress());
            customer.setCustomerNature(dto.getCustomerNature());
            super.updateById(customer);

            List<CustomerContractDto> contractDtos = dto.getCustContraVos();
            List<CustomerContract> list = null;
            if(!CollectionUtils.isEmpty(contractDtos)){
                //批量删除
                customerContractDao.removeKeyContract(dto.getCustomerId());
                list = encapCustomerContract(customer.getId(),contractDtos);
                customerContractService.saveBatch(list);
            }
        }
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo<PageVo<ListKeyCustomerVo>> findKeyCustomer(SelectKeyCustomerDto dto) {
        PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        List<ListKeyCustomerVo> keyCustomerList = customerDao.findKeyCustomter(dto);
        if(!CollectionUtils.isEmpty(keyCustomerList)){
            for(ListKeyCustomerVo vo : keyCustomerList){
                CustomerCountVo count = customerCountDao.count(vo.getId());
                if(count != null){
                    vo.setTotalOrder(count.getTotalOrder() == null ? 0:count.getTotalOrder());
                    vo.setTotalCar(count.getTotalCar() == null ? 0:count.getTotalCar());
                    vo.setTotalAmount(count.getTotalAmount() == null ? BigDecimal.ZERO:count.getTotalAmount().divide(new BigDecimal(100)));
                }
                if(StringUtils.isNotBlank(vo.getCreateTime())){
                    vo.setCreateTime(LocalDateTimeUtil.formatLDT(LocalDateTimeUtil.convertLongToLDT(Long.valueOf(vo.getCreateTime())),TimePatternConstant.COMPLEX_TIME_FORMAT));
                }
            }
        }
        PageInfo<ListKeyCustomerVo> pageInfo = new PageInfo<>(keyCustomerList);
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public boolean save(Customer customer) {
        //添加架构组数据
        AddUserReq addUserReq = new AddUserReq();
        addUserReq.setAccount(customer.getContactPhone());
        addUserReq.setPassword(YmlProperty.get("cjkj.web.password"));
        addUserReq.setDeptId(Long.valueOf(YmlProperty.get("cjkj.dept_customer_id")));
        addUserReq.setMobile(customer.getContactPhone());
        addUserReq.setName(customer.getName());
        ResultData<AddUserResp> resultData = sysUserService.save(addUserReq);

        if(resultData == null || resultData.getData() == null || resultData.getData().getUserId() == null){
            throw new ServerException("添加用户失败");
        }
        customer.setUserId(resultData.getData().getUserId());
        return super.save(customer);
    }

    @Override
    public Customer selectById(Long customerId) {
        return customerDao.selectById(customerId);
    }

    @Override
    public ResultVo savePartner(PartnerDto dto) {
        //新增c_customer
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto,customer);
        customer.setAlias(dto.getName());
        customer.setCustomerNo(sendNoService.getNo(SendNoTypeEnum.CUSTOMER));
        customer.setIsDelete(DeleteStateEnum.NO_DELETE.code);
        customer.setSource(CustomerSourceEnum.WEB.code);
        customer.setType(CustomerTypeEnum.COOPERATOR.code);
        customer.setState(CommonStateEnum.WAIT_CHECK.code);
        customer.setRegisterTime(NOW);
        customer.setCreateTime(NOW);
        customer.setCreateUserId(dto.getLoginId());

        //新增用户信息到物流平台
        ResultData<Long> rd = addCustomerToPlatform(customer);
        if (!ReturnMsg.SUCCESS.getCode().equals(rd.getCode())) {
            return BaseResultUtil.fail(rd.getMsg());
        }
        customer.setUserId(rd.getData());

        super.save(customer);
        encapPartner(dto,customer,NOW);
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo modifyPartner(PartnerDto dto) {
        Long now = LocalDateTimeUtil.getMillisByLDT(LocalDateTime.now());
        Customer customer = customerDao.selectById(dto.getCustomerId());
        if(customer != null){
            //判断手机号是否存在
            ResultData<Boolean> updateRd = updateCustomerToPlatform(customer, dto.getContactPhone());
            if (!ReturnMsg.SUCCESS.getCode().equals(updateRd.getCode())) {
                log.error("修改用户信息失败，原因：" + updateRd.getMsg());
                return BaseResultUtil.fail("修改用户信息失败，原因：" + updateRd.getMsg());
            }
            if (updateRd.getData()) {
                //需要同步手机号信息
                syncPhone(customer.getContactPhone(), dto.getContactPhone());
            }
           BeanUtils.copyProperties(dto,customer);
           customer.setAlias(dto.getName());
           super.updateById(customer);
            //删除合伙人附加信息
            customerPartnerDao.removeByCustomerId(customer.getId());
            //删除合伙人银行卡信息
            bankCardBindDao.removeBandCarBind(customer.getId());
            encapPartner(dto,customer,now);
        }
        return BaseResultUtil.success();
    }

    @Override
    public ResultVo<PageVo<CustomerPartnerVo>> findPartner(CustomerPartnerDto dto) {
        PageHelper.startPage(dto.getCurrentPage(),dto.getPageSize());
        List<CustomerPartnerVo> partnerVos = customerDao.getPartnerByTerm(dto);
        if(!CollectionUtils.isEmpty(partnerVos)){
            for(CustomerPartnerVo partnerVo : partnerVos){
                CustomerCountVo count = customerCountDao.count(partnerVo.getUserId());
                Admin admin = adminDao.findByUserId(partnerVo.getCreateUserId());
                if(admin != null){
                    partnerVo.setCreateUserName(StringUtils.isNotBlank(admin.getName()) == true ? admin.getName():"");
                }
                if(count != null){
                    partnerVo.setTotalOrder(count.getTotalOrder() == null ? 0:count.getTotalOrder());
                    partnerVo.setTotalCar(count.getTotalCar() == null ? 0:count.getTotalCar());
                    partnerVo.setTotalAmount(count.getTotalAmount() == null ? BigDecimal.ZERO:count.getTotalAmount().divide(new BigDecimal(100)));
                }
            }
        }
        PageInfo<CustomerPartnerVo> pageInfo = new PageInfo<>(partnerVos);
        return BaseResultUtil.success(pageInfo);
    }

    @Override
    public Customer selectByPhone(String customerPhone) {
        return customerDao.findByPhone(customerPhone);
    }

    @Override
    public boolean updateById(Customer customer) {
        return super.updateById(customer);
    }

    @Override
    public ResultVo getAllCustomerByKey(String keyword) {
        List<Map<String,Object>> customerList = customerDao.getAllCustomerByKey(keyword);
        return BaseResultUtil.success(customerList);
    }

    @Override
    public ResultVo getContractByCustomerId(Long customerId) {
        //获取当前时间戳
        Long now = LocalDateTimeUtil.getMillisByLDT(LocalDateTime.now());
        List<Map<String,Object>> contractList = customerDao.getContractByCustomerId(customerId,now);
        return BaseResultUtil.success(contractList);
    }

    @Override
    public ResultVo customerCoupon(CustomerCouponDto dto) {
        PageInfo<CustomerCouponVo> pageInfo = null;
        PageHelper.startPage(dto.getCurrentPage(), dto.getPageSize());
        List<CustomerCouponVo> voList = couponDao.getCustomerCouponByTerm(dto);
        List<CustomerCouponVo> disCouponVos = new ArrayList<>(10);
        List<CustomerCouponVo> couponVos = new ArrayList<>(10);
        Map<String, Object> count = new HashMap<>();
        if (!CollectionUtils.isEmpty(voList)) {
            //不可用的优惠券(使用过的或者是过期的)
            for (CustomerCouponVo couponVo : voList) {
                if (couponVo.getIsUse() == 1 || (couponVo.getIsForever() == 0 && couponVo.getEndPeriodDate() > NOW)) {
                    disCouponVos.add(couponVo);
                } else {
                    couponVos.add(couponVo);
                }
            }
            count.put("isUse_0", couponVos.size());
            count.put("isUse_1", disCouponVos.size());
        }
        if(UseStateEnum.DISABLED.code == dto.getIsUsable()){
            //不可用
            pageInfo = new PageInfo<>(disCouponVos);
        }else{
            pageInfo = new PageInfo<>(couponVos);
        }
        return BaseResultUtil.success(pageInfo,count);
    }
    @Override
    public ResultVo<List<CustomerCouponSendVo>> getCouponByCustomerId(Long customerId) {
        List<CustomerCouponSendVo> sendVoList = null;
        //根据客户id查看发放的优惠券
        List<CustomerCouponSendVo> couponVos = couponSendDao.getCustomerCoupon(customerId);
        if(!CollectionUtils.isEmpty(couponVos)){
            sendVoList = new ArrayList<>();
            for(CustomerCouponSendVo sendVo : couponVos){
                if(sendVo.getEndPeriodDate() != null){
                    if(sendVo.getEndPeriodDate() > NOW){
                        sendVoList.add(sendVo);
                    }
                }else{
                    sendVoList.add(sendVo);
                }
            }
        }
        return BaseResultUtil.success(sendVoList);
    }

    /**
     * 封装合伙人附加信息&银行卡信息
     * @param dto
     * @param customer
     * @param now
     * @return
     */
    private void encapPartner(PartnerDto dto,Customer customer,Long now){
        //新增合伙人附加信息c_customer_partner
        CustomerPartner cp = new CustomerPartner();
        BeanUtils.copyProperties(dto,cp);
        cp.setCustomerId(customer.getId());
        customerPartnerDao.insert(cp);
        //新增绑定银行卡信息s_bank_card_bind
        BankCardBind bcb = new BankCardBind();
        BeanUtils.copyProperties(dto,bcb);
        bcb.setUserId(customer.getUserId());
        bcb.setCardPhone(customer.getContactPhone());
        bcb.setUserType(UserTypeEnum.CUSTOMER.code);
        bcb.setState(UseStateEnum.USABLE.code);
        bcb.setCreateTime(now);
        bankCardBindDao.insert(bcb);
    }

    /**
     * 封装大客户合同
     * @param customerId
     * @param customerConList
     * @return
     */
    private List<CustomerContract> encapCustomerContract(Long customerId,List<CustomerContractDto> customerConList){
        List<CustomerContract> list = null;
        if(!CollectionUtils.isEmpty(customerConList)){
            list = new ArrayList<>(10);
            for(CustomerContractDto dto : customerConList){
                CustomerContract custCont = new CustomerContract();
                BeanUtils.copyProperties(dto,custCont);
                custCont.setCustomerId(customerId);
                if(StringUtils.isNotBlank(dto.getContractLife())){
                    custCont.setContractLife(LocalDateTimeUtil.convertToLong(dto.getContractLife(),TimePatternConstant.SIMPLE_DATE_FORMAT));
                }
                if(StringUtils.isNotBlank(dto.getProjectEstabTime())){
                    custCont.setProjectEstabTime(LocalDateTimeUtil.convertToLong(dto.getProjectEstabTime(),TimePatternConstant.SIMPLE_DATE_FORMAT));
                }
                list.add(custCont);
            }
        }
        return list;
    }

    /**
     * 将C端客户保存到物流平台
     * @param customer
     * @return
     */
    private ResultData<Long> addCustomerToPlatform(Customer customer) {
        ResultData<AddUserResp> accountRd =
                sysUserService.getByAccount(customer.getContactPhone());
        if (!ReturnMsg.SUCCESS.getCode().equals(accountRd.getCode())) {
            return ResultData.failed("获取用户信息失败，原因：" + accountRd.getMsg());
        }
        if (accountRd.getData() != null) {
            //存在，则直接返回已有用户userId信息
            return ResultData.ok(accountRd.getData().getUserId());
        }
        //不存在，需要重新添加
        AddUserReq user = new AddUserReq();
        user.setName(customer.getName());
        user.setAccount(customer.getContactPhone());
        user.setMobile(customer.getContactPhone());
        user.setDeptId(Long.parseLong(YmlProperty.get("cjkj.dept_customer_id")));
        user.setPassword(YmlProperty.get("cjkj.salesman.password"));
        ResultData<AddUserResp> saveRd = sysUserService.save(user);
        if (!ReturnMsg.SUCCESS.getCode().equals(saveRd.getCode())) {
            return ResultData.failed("保存客户信息失败，原因：" + saveRd.getMsg());
        }
        return ResultData.ok(saveRd.getData().getUserId());
    }

    /**
     * 修改账号信息到物流平台：
     *  修改物流平台账号信息：如果修改账号则将要修改的账号不能存在否则修改失败
     * @param customer
     * @param newPhone
     * @return
     */
    private ResultData<Boolean> updateCustomerToPlatform(Customer customer, String newPhone) {
        String oldPhone = customer.getContactPhone();
        if (!oldPhone.equals(newPhone)) {
            //新旧账号不相同需要替换手机号
            ResultData<AddUserResp> accountRd = sysUserService.getByAccount(newPhone);
            if (!ReturnMsg.SUCCESS.getCode().equals(accountRd.getCode())) {
                return ResultData.failed("用户信息获取失败，原因：" + accountRd.getMsg());
            }
            if (accountRd.getData() != null) {
                return ResultData.failed("用户账号不允许修改，预修改账号：" + newPhone + " 已存在");
            }
            UpdateUserReq user = new UpdateUserReq();
            user.setUserId(customer.getUserId());
            user.setAccount(newPhone);
            user.setMobile(newPhone);
            ResultData rd = sysUserService.updateUser(user);
            if (!ReturnMsg.SUCCESS.getCode().equals(rd.getCode())) {
                return ResultData.failed("用户信息修改失败，原因：" + rd.getMsg());
            }
            return ResultData.ok(true);
        }
        return ResultData.ok(false);
    }

    /**
     * 将手机号同步到系统用户及司机用户
     * @param oldPhone
     * @param newPhone
     */
    private void syncPhone(String oldPhone, String newPhone) {
        Admin admin = adminDao.selectOne(new QueryWrapper<Admin>()
                .eq("phone", oldPhone));
        if (null != admin) {
            admin.setPhone(newPhone);
            adminDao.updateById(admin);
        }
        Driver driver = driverDao.selectOne(new QueryWrapper<Driver>()
                .eq("phone", oldPhone));
        if (null != driver) {
            driver.setPhone(newPhone);
            driverDao.updateById(driver);
        }
    }

    /**
     * 校验：手机号是否在Customer中存在
     * @param phone
     * @return
     */
    /*private boolean phoneExistsInCustomer(String phone) {
        List<Customer> existList = customerDao.selectList(new QueryWrapper<Customer>()
                .eq("contact_phone", phone));
        if (!CollectionUtils.isEmpty(existList)) {
            log.error("手机号已存在，请检查");
            return true;
        }
        return false;
    }*/
}

package com.cjyc.customer.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjyc.common.model.dao.IBankCardBindDao;
import com.cjyc.common.model.dao.ICustomerDao;
import com.cjyc.common.model.dao.ICustomerPartnerDao;
import com.cjyc.common.model.dto.web.customer.PartnerDto;
import com.cjyc.common.model.entity.BankCardBind;
import com.cjyc.common.model.entity.Customer;
import com.cjyc.common.model.entity.CustomerPartner;
import com.cjyc.common.model.enums.CommonStateEnum;
import com.cjyc.common.model.enums.UseStateEnum;
import com.cjyc.common.model.enums.customer.CustomerSourceEnum;
import com.cjyc.common.model.enums.customer.CustomerTypeEnum;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.customer.api.service.IApplyPartnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
public class ApplyPartnerServiceImpl extends ServiceImpl<ICustomerDao, Customer> implements IApplyPartnerService {

    @Resource
    private ICustomerDao customerDao;

    @Resource
    private ICustomerPartnerDao customerPartnerDao;

    @Resource
    private IBankCardBindDao bankCardBindDao;

    @Override
    @Transactional
    public ResultVo applyPartner(PartnerDto dto) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto,customer);
        customer.setType(CustomerTypeEnum.COOPERATOR.code);
        customer.setSource(CustomerSourceEnum.UPGRADE.code);
        customer.setState(CommonStateEnum.WAIT_CHECK.code);
        super.updateById(customer);
        //合伙人附加信息
        CustomerPartner cp = new CustomerPartner();
        BeanUtils.copyProperties(dto,cp);
        cp.setCustomerId(customer.getId());
        customerPartnerDao.insert(cp);
        //创建合伙人银行信息
        BankCardBind bcb = new BankCardBind();
        BeanUtils.copyProperties(dto,bcb);
        bcb.setState(UseStateEnum.USABLE.code);
        bankCardBindDao.insert(bcb);
        return BaseResultUtil.success();
    }
}
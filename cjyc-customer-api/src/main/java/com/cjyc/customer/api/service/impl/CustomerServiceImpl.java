package com.cjyc.customer.api.service.impl;

import com.cjyc.common.model.dto.web.customer.CustomerfuzzyListDto;
import com.cjyc.common.model.entity.Customer;
import com.cjyc.common.model.dao.customerDao;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.customer.CustomerFuzzyListVo;
import com.cjyc.customer.api.service.ICustomerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 客户表（登录用户端APP用户） 服务实现类
 * </p>
 *
 * @author JPG
 * @since 2019-10-18
 */
@Service
public class CustomerServiceImpl extends ServiceImpl<customerDao, Customer> implements ICustomerService {
    @Resource
    private customerDao customerDao;

    @Override
    public ResultVo fuzzyList(CustomerfuzzyListDto paramsDto) {
        List<CustomerFuzzyListVo> fuzzyList = customerDao.findFuzzyList(paramsDto);
        return BaseResultUtil.success(fuzzyList);
    }
}

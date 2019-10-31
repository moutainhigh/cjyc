package com.cjyc.web.api.service;

import com.cjyc.common.model.dto.web.customer.*;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.CustomerVo;
import com.cjyc.common.model.vo.web.ListKeyCustomerVo;
import com.cjyc.common.model.vo.web.ShowKeyCustomerVo;
import com.cjyc.common.model.entity.Customer;
import com.cjyc.common.model.dto.BasePageDto;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 *  @author: zj
 *  @Date: 2019/9/29 15:01
 *  @Description: 用户接口
 */
public interface ICustomerService  {

    /**
     *  新增移动端用户
     * @param customerDto
     * @return
     */
    boolean saveCustomer(CustomerDto customerDto);

    /**
     * 更新移动端用户
     * @param customerDto
     * @return
     */
    boolean updateCustomer(CustomerDto customerDto);

    /**
     * 根据id删除移动端用户
     * @param ids
     * @return
     */
    boolean delCustomerByIds(List<Long> ids);

    /**
     * 根据条件查询移动端用户
     * @param selectCustomerDto
     * @return
     */
    PageInfo<CustomerVo> findCustomerByTerm(SelectCustomerDto selectCustomerDto);

    /**
     * 新增大客户&合同
     * @param keyCustomerDto
     * @return
     */
    boolean saveKeyCustAndContract(KeyCustomerDto keyCustomerDto);

    /**
     * 根据ids删除大客户
     * @param ids
     * @return
     */
    boolean delKeyCustomerByIds(List<Long> ids);

    /**
     * 根据大客户id查看大客户&合同
     * @param id
     * @return
     */
    ShowKeyCustomerVo showKeyCustomerById(Long id);

    /**
     * 更新大客户&合同
     * @param keyCustomerDto
     * @return
     */
    boolean updateKeyCustomer(KeyCustomerDto keyCustomerDto);

    /**
     * 根据条件查询大客户信息
     * @param selectKeyCustomerDto
     * @return
     */
    PageInfo<ListKeyCustomerVo> findKeyCustomer(SelectKeyCustomerDto selectKeyCustomerDto);

    int save(Customer customer);

    Customer selectById(Long customerId);

    /**
     * 新增大客户
     * @param dto
     * @return
     */
    ResultVo addOrUpdatePartner(PartnerDto dto);

    /**
     * 审核/删除
     * @param id
     * @param flag
     * @return
     */
    ResultVo verifyOrDeletePartner(Long id,Integer flag);

    Customer selectByPhone(String customerPhone);

    int updateById(Customer customer);

    /**
     * 根据关键字(手机号/用户名称)模糊查询用户信息
     * @param keyword
     * @return
     */
    ResultVo getAllCustomerByKey(String keyword);

    /**
     * 查看客户优惠券
     * @param dto
     * @return
     */
    ResultVo getCustomerCouponByTerm(CustomerCouponDto dto);
}

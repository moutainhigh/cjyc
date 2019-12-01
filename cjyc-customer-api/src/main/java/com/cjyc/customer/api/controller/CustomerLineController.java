package com.cjyc.customer.api.controller;

import com.cjyc.common.model.dto.CommonDto;
import com.cjyc.common.model.dto.customer.invoice.InvoiceApplyQueryDto;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.customer.customerLine.CustomerLineVo;
import com.cjyc.customer.api.service.ICustomerLineService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 *  @author: zj
 *  @Date: 2019/11/1 17:18
 *  @Description:客户历史线路
 */
@Api(tags = "客户历史线路")
@CrossOrigin
@RestController
@RequestMapping("/customerLine")
public class CustomerLineController {

    @Resource
    private ICustomerLineService customerLineService;

    @ApiOperation(value = "查看用户历史线路")
    @PostMapping(value = "/queryLinePage")
    public ResultVo<PageVo<CustomerLineVo>> queryLinePage(@Validated @RequestBody CommonDto dto){
        return customerLineService.queryLinePage(dto);
    }
}
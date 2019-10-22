package com.cjyc.customer.api.controller;

import com.cjyc.common.model.dto.customer.UpdateCustomerDto;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.customer.api.service.ICustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 客户
 * @author JPG
 */
@Api(tags = "客户")
@Slf4j
@RestController
@RequestMapping(value = "/cust",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CustomerController {

    @Resource
    private ICustomerService customerService;


    @ApiOperation(value = "修改个人信息")
    @PostMapping("/update")
    public ResultVo update(@RequestBody UpdateCustomerDto updateCustomerDto) {

        return null;
    }


}
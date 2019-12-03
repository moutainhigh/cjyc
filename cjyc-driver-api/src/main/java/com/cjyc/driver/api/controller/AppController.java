package com.cjyc.driver.api.controller;

import com.cjyc.common.model.dto.AppItemDto;
import com.cjyc.common.model.dto.sys.SysPictureDto;
import com.cjyc.common.model.vo.AppItemVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.system.service.ICsAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@Api(tags = "app",description = "查询首页轮播图")
public class AppController {

    @Autowired
    private ICsAppService csAppService;

    @ApiOperation(value = "查询首页轮播图", notes = "用户端 systemPicture传system_picture_customer" +
            "；司机端systemPicture传system_picture_driver； 业务员端systemPicture传system_picture_sale ", httpMethod = "POST")
    @PostMapping(value = "/getSysPicture")
    public ResultVo<AppItemVo> getSysPicture(@Validated @RequestBody AppItemDto dto){
        return csAppService.getSysPicture(dto);
    }

    @ApiOperation(value = "修改首页轮播图")
    @PostMapping(value = "/updateSysPicture")
    public ResultVo updateSysPicture(@RequestBody @Validated SysPictureDto sysPictureDto){
        return csAppService.updateSysPicture(sysPictureDto);
    }
}
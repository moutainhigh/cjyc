package com.cjyc.customer.api.controller;

import com.cjyc.common.model.dto.LogisticsInformationDto;
import com.cjyc.common.model.vo.LogisticsInformationVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.system.service.ICsLogisticsInformationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @Description 位置信息
 * @Author Liu Xing Xiang
 * @Date 2020/4/9 8:26
 **/
@Api(tags = "位置信息管理")
@CrossOrigin
@RestController
@RequestMapping("/location")
public class LocationInfoController {
    @Resource
    private ICsLogisticsInformationService csLogisticsInformationService;

    /**
     * 功能描述: 查询物流信息
     * @author liuxingxiang
     * @date 2020/4/3
     * @param reqDto
     * @return com.cjyc.common.model.vo.ResultVo<com.cjyc.common.model.vo.LogisticsInformationVo>
     */
    @ApiOperation(value = "查询物流信息")
    @PostMapping(value = "/getLogisticsInfo")
    public ResultVo<LogisticsInformationVo> getLogisticsInformation(@RequestBody @Valid LogisticsInformationDto reqDto) {
        return csLogisticsInformationService.getLogisticsInformation(reqDto);
    }
}

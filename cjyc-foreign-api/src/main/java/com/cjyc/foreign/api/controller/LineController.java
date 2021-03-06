package com.cjyc.foreign.api.controller;

import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.foreign.api.dto.req.LineReqDto;
import com.cjyc.foreign.api.dto.res.LineResDto;
import com.cjyc.foreign.api.service.ILineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 班线管理
 * @Author Liu Xing Xiang
 * @Date 2020/3/10 9:57
 **/
@Api(tags = {"班线管理"})
@RestController
@RequestMapping("/line")
public class LineController {
    @Autowired
    private ILineService lineService;

    /**
     * 功能描述: 根据城市编码查询班线价格
     * @author liuxingxiang
     * @date 2020/3/10
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo<com.cjyc.foreign.api.dto.res.LineResDto>
     */
    @ApiOperation(value = "根据城市编码查询班线价格")
    @PostMapping("/getLinePriceByCityCode")
    public ResultVo<LineResDto> getLinePriceByCityCode(@RequestBody @Validated LineReqDto dto) {
        return lineService.getLinePriceByCityCode(dto);
    }


}

package com.cjyc.salesman.api.controller;

import com.cjyc.common.model.dto.web.waybill.*;
import com.cjyc.common.model.enums.ResultEnum;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.BaseTipVo;
import com.cjyc.common.model.vo.ListVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.system.service.ICsAdminService;
import com.cjyc.common.system.service.ICsWaybillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运单
 *
 * @author JPG
 */
@Api(tags = "运单")
@Slf4j
@RestController
@RequestMapping(value = "/waybill",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class WaybillController {

    @Autowired
    private ICsWaybillService csWaybillService;
    @Autowired
    private ICsAdminService csAdminService;

    /**
     * 提送车调度
     *
     * @author JPG
     * @since 2019/10/15 11:53
     */
    @ApiOperation("提送车调度")
    @PostMapping("/local/save")
    public ResultVo saveLocal(@Validated @RequestBody SaveLocalDto reqDto) {
        ResultVo<SaveLocalDto> resVo = csAdminService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csWaybillService.saveLocal(reqDto);
    }



    /**
     * 修改同城调度
     *
     * @author JPG
     * @since 2019/10/15 11:53
     */
    @ApiOperation(value = "修改同城调度", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PostMapping("/local/update")
    public ResultVo updateLocal(@Validated @RequestBody UpdateLocalDto reqDto) {
        ResultVo<UpdateLocalDto> resVo = csAdminService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csWaybillService.updateLocal(reqDto);
    }


    /**
     * 干线调度
     *
     * @author JPG
     * @since 2019/10/15 11:53
     */
    @ApiOperation("干线调度")
    @PostMapping("/trunk/save")
    public ResultVo saveTrunk(@Validated @RequestBody SaveTrunkWaybillDto reqDto) {
        ResultVo<SaveTrunkWaybillDto> resVo = csAdminService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csWaybillService.saveTrunk(reqDto);
    }

    /**
     * 修改干线运单
     *
     * @author JPG
     * @since 2019/10/15 11:53
     */
    @ApiOperation("修改干线调度")
    @PostMapping("/trunk/update")
    public ResultVo updateTrunk(@Validated @RequestBody UpdateTrunkWaybillDto reqDto) {
        ResultVo<UpdateTrunkWaybillDto> resVo = csAdminService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csWaybillService.updateTrunk(reqDto);
    }


    /**
     * 中途卸载车辆
     *
     * @author JPG
     * @since 2019/10/15 11:53
     */
    @ApiOperation("中途卸载车辆")
    @PostMapping("/trunk/midway/unload")
    public ResultVo trunkMidwayUnload(@Validated @RequestBody TrunkMidwayUnload reqDto) {
        ResultVo<TrunkMidwayUnload> resVo = csAdminService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csWaybillService.trunkMidwayUnload(reqDto);
    }


    /**
     * 取消调度
     *
     * @author JPG
     * @since 2019/10/15 11:53
     */
    @ApiOperation("取消调度")
    @PostMapping("/cancel")
    public ResultVo<ListVo<BaseTipVo>> cancel(@Validated @RequestBody CancelWaybillDto reqDto) {
        //验证用户
        ResultVo<CancelWaybillDto> resVo = csAdminService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csWaybillService.cancel(reqDto);
    }

}

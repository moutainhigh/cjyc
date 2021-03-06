package com.cjyc.driver.api.controller;

import com.cjyc.common.model.dto.driver.task.ReplenishInfoDto;
import com.cjyc.common.model.dto.web.task.AllotTaskDto;
import com.cjyc.common.model.dto.web.task.BaseTaskDto;
import com.cjyc.common.model.enums.ResultEnum;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.ResultReasonVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.system.service.ICsDriverService;
import com.cjyc.common.system.service.ICsTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@Api(tags = "运单")
@RequestMapping(value = "/waybill", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class WaybillController {
    @Resource
    private ICsTaskService csTaskService;
    @Resource
    private ICsDriverService csDriverService;


    /**
     * 提车完善信息
     * @author JPG
     */
    @ApiOperation(value = "提车完善信息")
    @PostMapping(value = "/replenish/info/update")
    public ResultVo replenishInfo(@Valid @RequestBody ReplenishInfoDto reqDto) {
        //验证用户
        ResultVo<ReplenishInfoDto> resVo = csDriverService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csTaskService.replenishInfo(resVo.getData());
    }


    /**
     * 提车装车并完善信息
     * @author JPG
     */
    @ApiOperation(value = "同城装车")
    @PostMapping(value = "/load/for/local")
    public ResultVo<ResultReasonVo> loadForLocal(@RequestBody @Validated ReplenishInfoDto reqDto) {
        //验证用户
        ResultVo<ReplenishInfoDto> resVo = csDriverService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csTaskService.loadForLocal(resVo.getData());
    }
    /**
     * 分配任务
     * @author JPG
     */
    @ApiOperation(value = "分配任务")
    @PostMapping(value = "/allot")
    public ResultVo allot(@RequestBody AllotTaskDto reqDto) {
        //验证用户
        ResultVo<AllotTaskDto> resVo = csDriverService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csTaskService.allot(resVo.getData());
    }

    /**
     * 装车
     * @author JPG
     */
    @ApiOperation(value = "装车")
    @PostMapping(value = "/car/load")
    public ResultVo<ResultReasonVo> load(@Validated @RequestBody BaseTaskDto reqDto) {
        //验证用户
        ResultVo<BaseTaskDto> resVo = csDriverService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csTaskService.load(resVo.getData());
    }

    /**
     * 卸车
     * @author JPG
     */
    @ApiOperation(value = "卸车")
    @PostMapping(value = "/car/unload")
    public ResultVo<ResultReasonVo> unload(@Validated @RequestBody BaseTaskDto reqDto) {
        //验证用户
        ResultVo<BaseTaskDto> resVo = csDriverService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csTaskService.unload(resVo.getData());
    }

    /**
     * 取消卸车
     * @author JPG
     */
    @ApiOperation(value = "取消交车")
    @PostMapping(value = "/car/unload/cancel")
    public ResultVo<ResultReasonVo> cancelUnload(@Validated @RequestBody BaseTaskDto reqDto) {
        //验证用户
        ResultVo<BaseTaskDto> resVo = csDriverService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }
        return csTaskService.cancelUnload(resVo.getData());
    }

    /**
     * 签收-司机
     * @author JPG
     */
    @ApiOperation(value = "签收-无需支付")
    @PostMapping(value = "/car/receipt")
    public ResultVo receipt(@RequestBody BaseTaskDto reqDto) {
        //验证用户
        ResultVo<BaseTaskDto> resVo = csDriverService.validateEnabled(reqDto);
        if(ResultEnum.SUCCESS.getCode() != resVo.getCode()){
            return BaseResultUtil.fail(resVo.getMsg());
        }

        return csTaskService.receipt(resVo.getData());
    }




}

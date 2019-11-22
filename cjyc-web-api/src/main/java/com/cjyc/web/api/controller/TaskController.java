package com.cjyc.web.api.controller;

import com.cjyc.common.model.dto.web.task.*;
import com.cjyc.common.model.entity.Admin;
import com.cjyc.common.model.entity.Driver;
import com.cjyc.common.model.util.BaseResultUtil;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.task.CrTaskVo;
import com.cjyc.common.model.vo.web.task.ListByWaybillTaskVo;
import com.cjyc.common.model.vo.web.task.TaskVo;
import com.cjyc.common.system.service.ICsAdminService;
import com.cjyc.common.system.service.ICsDriverService;
import com.cjyc.web.api.service.ITaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 任务
 * @author JPG
 */
@RestController
@RequestMapping("/task")
@Api(tags = "运单-任务")
public class TaskController {

    @Resource
    private ITaskService taskService;
    @Resource
    private ICsAdminService csAdminService;
    @Resource
    private ICsDriverService csDriverService;


    /**
     * 分配任务
     * @author JPG
     */
    @ApiOperation(value = "分配任务")
    @PostMapping(value = "/allot")
    public ResultVo allot(@RequestBody AllotTaskDto reqDto) {
        //验证用户
        Driver driver = csDriverService.getById(reqDto.getUserId(), true);
        if (driver == null) {
            return BaseResultUtil.fail("当前用户不能登录");
        }
        reqDto.setUserName(driver.getName());
        return taskService.allot(reqDto);
    }



    /**
     * 装车
     * @author JPG
     */
    @ApiOperation(value = "装车")
    @PostMapping(value = "/car/load")
    public ResultVo load(@Validated @RequestBody LoadTaskDto reqDto) {
        //验证用户
        Driver driver = csDriverService.validate(reqDto.getLoginId());
        reqDto.setLoginName(driver.getName());
        return taskService.load(reqDto);
    }


    /**
     * 卸车
     * @author JPG
     */
    @ApiOperation(value = "卸车")
    @PostMapping(value = "/car/unload")
    public ResultVo unload(@RequestBody UnLoadTaskDto reqDto) {
        //验证用户
        Driver driver = csDriverService.validate(reqDto.getLoginId());
        reqDto.setLoginName(driver.getName());
        return taskService.unload(reqDto);
    }


    /**
     * 确认入库
     * @author JPG
     */
    @ApiOperation(value = "确认入库")
    @PostMapping(value = "/car/in/store")
    public ResultVo inStore(@Validated @RequestBody InStoreTaskDto reqDto) {
        //验证用户
        Admin admin = csAdminService.validate(reqDto.getLoginId());
        reqDto.setLoginName(admin.getName());
        return taskService.inStore(reqDto);
    }

    /**
     * 确认出库
     * @author JPG
     */
    @ApiOperation(value = "确认出库")
    @PostMapping(value = "/car/out/store")
    public ResultVo outStore(@RequestBody OutStoreTaskDto reqDto) {
        //验证用户
        Admin admin = csAdminService.validate(reqDto.getLoginId());
        reqDto.setLoginName(admin.getName());
        return taskService.outStore(reqDto);
    }



    /**
     * 交接-司机
     * @author JPG
     */
    @ApiOperation(value = "签收")
    @PostMapping(value = "/car/driver/sign")
    public ResultVo driverSign(@RequestBody SignTaskDto reqDto) {
        //验证用户
        Admin admin = csAdminService.validate(reqDto.getLoginId());
        reqDto.setLoginName(admin.getName());
        return taskService.sign(reqDto);
    }



    /**
     * 查询子运单（任务）列表
     */
    @ApiOperation(value = "查询子运单（任务）列表")
    @PostMapping(value = "/list/{waybillId}")
    public ResultVo<List<ListByWaybillTaskVo>> listByWaybillId(@PathVariable Long waybillId) {
        return taskService.getlistByWaybillId(waybillId);
    }

    /**
     * 查询子运单（任务）列表
     */
    @ApiOperation(value = "查询子运单（任务）列表")
    @PostMapping(value = "/get/{taskId}")
    public ResultVo<TaskVo> get(@PathVariable Long taskId) {
        return taskService.get(taskId);
    }







    /**----承运商模块------------------------------------------------------------------------------------------------------------*/


    /**
     * 我的运单-承运商
     */
    @ApiOperation(value = "我的运单-承运商")
    @PostMapping(value = "/cr/alloted/list")
    public ResultVo<PageVo<CrTaskVo>> crAllottedList(@RequestBody CrTaskDto reqDto) {
        return taskService.crAllottedList(reqDto);
    }



}

package com.cjyc.web.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjyc.common.model.dto.web.task.*;
import com.cjyc.common.model.entity.Task;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultReasonVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.task.CrTaskVo;
import com.cjyc.common.model.vo.web.task.ListByWaybillTaskVo;
import com.cjyc.common.model.vo.web.task.TaskPageVo;
import com.cjyc.common.model.vo.web.task.TaskVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 任务表(子运单) 服务类
 * </p>
 *
 * @author JPG
 * @since 2019-10-26
 */
public interface ITaskService extends IService<Task> {


    ResultVo<List<ListByWaybillTaskVo>> getlistByWaybillId(Long waybillId);

    ResultVo<TaskVo> get(Long taskId);

    ResultVo<PageVo<CrTaskVo>> crTaskList(CrTaskDto reqDto);

    /**
     * 功能描述: 查询我的任务列表
     * @author liuxingxiang
     * @date 2019/12/20
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo
     */
    ResultVo<PageVo<TaskPageVo>> getTaskPage(TaskPageDto dto);




    /************************************韵车集成改版 st***********************************/

    ResultVo<PageVo<CrTaskVo>> crTaskListNew(CrTaskDto reqDto);

    void exportCrAllottedListExcel(HttpServletRequest request, HttpServletResponse response);
}

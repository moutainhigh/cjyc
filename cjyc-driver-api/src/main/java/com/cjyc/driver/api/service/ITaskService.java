package com.cjyc.driver.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjyc.common.model.dto.driver.BaseDriverDto;
import com.cjyc.common.model.dto.driver.DetailQueryDto;
import com.cjyc.common.model.dto.driver.FinishTaskQueryDto;
import com.cjyc.common.model.dto.driver.NoFinishTaskQueryDto;
import com.cjyc.common.model.entity.Task;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.driver.task.TaskDetailVo;
import com.cjyc.common.model.vo.driver.task.WaybillTaskVo;
import com.github.pagehelper.PageInfo;

/**
 * <p>
 * 任务表(子运单) 服务类
 * </p>
 *
 * @author JPG
 * @since 2019-11-19
 */
public interface ITaskService extends IService<Task> {

    /**
     * 功能描述: 分页查询首页已交付任务单列表
     * @author liuxingxiang
     * @date 2019/11/19
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo<com.github.pagehelper.PageInfo<com.cjyc.common.model.vo.driver.task.WaybillTaskVo>>
     */
    ResultVo<PageInfo<WaybillTaskVo>> getFinishTaskPage(FinishTaskQueryDto dto);

    /**
     * 功能描述: 查询任务详情
     * @author liuxingxiang
     * @date 2019/11/20
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo<com.github.pagehelper.PageInfo<com.cjyc.common.model.vo.driver.task.TaskDetailVo>>
     */
    ResultVo<PageInfo<TaskDetailVo>> getDetail(DetailQueryDto dto);

    /**
     * 功能描述: 分页查询待分配任务列表
     * @author liuxingxiang
     * @date 2019/11/20
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo<com.github.pagehelper.PageInfo<com.cjyc.common.model.vo.driver.task.WaybillTaskVo>>
     */
    ResultVo<PageInfo<WaybillTaskVo>> getWaitHandleTaskPage(BaseDriverDto dto);

    /**
     * 功能描述: 分页查询提车，交车任务列表
     * @author liuxingxiang
     * @date 2019/11/20
     * @param dto
     * @return com.cjyc.common.model.vo.ResultVo<com.github.pagehelper.PageInfo<com.cjyc.common.model.vo.driver.task.WaybillTaskVo>>
     */
    ResultVo<PageInfo<WaybillTaskVo>> getNoFinishTaskPage(NoFinishTaskQueryDto dto);
}

package com.cjyc.web.api.service;

import com.cjyc.common.model.dto.web.OperateDto;
import com.cjyc.common.model.dto.web.carrier.*;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.carrier.TrailCarrierVo;

import com.cjyc.common.model.vo.web.carrier.TransportDriverVo;
import com.cjyc.common.model.vo.web.carrier.TransportVehicleVo;

/**
 *  @author: zj
 *  @Date: 2019/10/18 15:22
 *  @Description:承运商管理接口
 */
public interface ICarrierService {

    /**
     * 添加承运商
     * @param dto
     * @return
     */
    ResultVo saveOrModifyCarrier(CarrierDto dto);

    /**
     * 根据条件查询承运商
     * @param dto
     * @return
     */
    ResultVo findCarrier(SeleCarrierDto dto);

    /**
     * 根据承运商id进行审核通过/拒绝/冻结
     * @param dto
     * @return
     */
    ResultVo verifyCarrier(OperateDto dto);

    /**
     * 根据承运商carrierId查看承运商信息
     * @param carrierId
     * @return
     */
    ResultVo showBaseCarrier(Long carrierId);

    /**
     * 重置承运商超级管理员密码
     * @param id
     * @return
     */
    ResultVo resetPwd(Long id);

    /**
     * 获取承运商下司机
     * @param dto
     * @return
     */
    ResultVo<PageVo<TransportDriverVo>> transportDriver(TransportDto dto);

    /**
     * 获取该承运商下的车辆信息
     * @param dto
     * @return
     */
    ResultVo<PageVo<TransportVehicleVo>> transportVehicle(TransportDto dto);

    /**
     * 调度承运商信息
     * @param dto
     * @return
     */
    ResultVo dispatchCarrier(DispatchCarrierDto dto);

    /**
     * 调度中心中提车干线调度中代驾和拖车列表
     * @param dto
     * @return
     */
    ResultVo<PageVo<TrailCarrierVo>> trailDriver(TrailCarrierDto dto);
}

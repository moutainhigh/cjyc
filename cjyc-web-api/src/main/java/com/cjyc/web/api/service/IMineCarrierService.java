package com.cjyc.web.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjyc.common.model.dto.CarrierVehicleDto;
import com.cjyc.common.model.dto.web.OperateDto;
import com.cjyc.common.model.dto.web.mineCarrier.*;
import com.cjyc.common.model.entity.Carrier;
import com.cjyc.common.model.vo.PageVo;
import com.cjyc.common.model.vo.ResultVo;
import com.cjyc.common.model.vo.web.mineCarrier.HandleDto;
import com.cjyc.common.model.vo.web.mineCarrier.MyCarVo;
import com.cjyc.common.model.vo.web.mineCarrier.MyDriverVo;
import com.cjyc.common.model.vo.FreeDriverVo;
import com.cjyc.common.model.vo.web.mineCarrier.MyWaybillVo;

import java.util.List;

public interface IMineCarrierService extends IService<Carrier> {

    /**
     * 查询该承运商下的运单
     * @param dto
     * @return
     */
    ResultVo<PageVo<MyWaybillVo>> findWaybill(MyWaybillDto dto);
    /**
     * 分页查询承运商下司机
     * @param dto
     * @return
     */
    ResultVo<PageVo<MyDriverVo>> findPageDriver(QueryMyDriverDto dto);

    /**
     * 操作承运商下的司机
     * @param dto
     * @return
     */
    ResultVo verifyDriver(OperateDto dto);

    /**
     * 新增承运商下车辆
     * @param dto
     * @return
     */
    ResultVo saveOrModifyVehicle(CarrierVehicleDto dto);

    /**
     * 分页查询该承运商下的车辆
     * @param dto
     * @return
     */
    ResultVo<PageVo<MyCarVo>> findPageCar(QueryMyCarDto dto);
}

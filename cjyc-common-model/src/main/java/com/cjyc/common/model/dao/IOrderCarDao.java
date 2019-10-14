package com.cjyc.common.model.dao;

import com.cjyc.common.model.entity.OrderCar;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 订单明细（车辆表） Mapper 接口
 * </p>
 *
 * @author JPG
 * @since 2019-09-29
 */
public interface IOrderCarDao extends BaseMapper<OrderCar> {

    /**
     * 根据订单编号查询车辆信息
     * @param orderNo
     * @return
     */
    List<OrderCarCenterVo> getOrderCarByNo(@Param("orderNo") String orderNo);

}

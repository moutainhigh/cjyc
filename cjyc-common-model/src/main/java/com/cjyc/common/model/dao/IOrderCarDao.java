package com.cjyc.common.model.dao;

import com.cjyc.common.model.entity.OrderCar;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 订单明细（车辆表） Mapper 接口
 * </p>
 *
 * @author JPG
 * @since 2019-09-29
 */
@Repository
public interface IOrderCarDao extends BaseMapper<OrderCar> {

}

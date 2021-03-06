package com.cjyc.common.model.dao;

import com.cjyc.common.model.entity.LineNode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 线路节点顺序表 Mapper 接口
 * </p>
 *
 * @author JPG
 * @since 2019-09-29
 */
public interface ILineNodeDao extends BaseMapper<LineNode> {

    List<LineNode> findByCitySet(@Param("citySet") Set<String> citySet, @Param("startCity") String startCity);
}

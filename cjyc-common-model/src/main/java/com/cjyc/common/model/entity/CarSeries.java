package com.cjyc.common.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 车系管理
 * </p>
 *
 * @author JPG
 * @since 2019-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("s_car_series")
@ApiModel(value="CarSeries对象", description="车系管理")
public class CarSeries implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "车辆ID")
    private String carCode;

    @ApiModelProperty(value = "品牌")
    private String brand;

    @ApiModelProperty(value = "型号")
    private String model;

    @ApiModelProperty(value = "分类： 1微型车，2小型车，3中型车，4大型车， 5其他车")
    private Integer type;

    @ApiModelProperty(value = "拼音首字母")
    private String pinInitial;

    @ApiModelProperty(value = "拼音首字母缩写")
    private String pinAcronym;

    @ApiModelProperty(value = "品牌logo图片")
    private String logoImg;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "创建人")
    private Long createUserId;

    @ApiModelProperty(value = "最后修改时间")
    private Long updateTime;

    @ApiModelProperty(value = "最后修改人")
    private Long updateUserId;
}

package com.cjyc.common.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 运行运力池表
 * </p>
 *
 * @author JPG
 * @since 2019-11-08
 */
@Data
@Accessors(chain = true)
@TableName("d_vehicle_running")
@ApiModel(value="VehicleRunning对象", description="运行运力池表")
public class VehicleRunning implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "司机ID")
    private Long driverId;

    @ApiModelProperty(value = "运力ID")
    private Long vehicleId;

    @ApiModelProperty(value = "运力车牌")
    private String plateNo;

    @ApiModelProperty(value = "承运数")
    private Integer carryCarNum;

    @ApiModelProperty(value = "非空车位")
    private Integer occupiedCarNum;

    @ApiModelProperty(value = "状态：0无效，1有效")
    private Integer state;

    @ApiModelProperty(value = "运行状态：0空闲，1在途  2繁忙")
    private Integer runningState;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "更新时间")
    private Long updateTime;

    @ApiModelProperty(value = "上报时间")
    private String beatTime;

    @ApiModelProperty(value = "经度")
    private String lng;

    @ApiModelProperty(value = "纬度")
    private String lat;


}

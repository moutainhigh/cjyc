package com.cjyc.common.model.vo.driver.mine;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
@Data
public class DriverInfoVo implements Serializable {
    private static final long serialVersionUID = -3792504247609727556L;

    @ApiModelProperty("承运商id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long carrierId;

    @ApiModelProperty("司机id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long driverId;

    @ApiModelProperty("司机姓名")
    private String realName;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("身份证号")
    private String idCard;

    @ApiModelProperty("身份证正面")
    private String idCardFrontImg;

    @ApiModelProperty("身份证反面")
    private String idCardBackImg;

    @ApiModelProperty("承运方式：2 : 代驾  3 : 干线   4：拖车")
    private Integer mode;

    @ApiModelProperty("营运状态：0营运中(空闲)，1停运中(繁忙)")
    private Integer businessState;

    @ApiModelProperty("车辆id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long vehicleId;

    @ApiModelProperty("车牌号")
    private String plateNo;
}
package com.cjyc.common.model.vo.web.mimeCarrier;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
@Data
public class MyDriverVo implements Serializable {
    private static final long serialVersionUID = -3906374262244268584L;

    @ApiModelProperty("承运商id(carrierId)")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long carrierId;

    @ApiModelProperty("司机id(driverId)")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long driverId;

    @ApiModelProperty("司机姓名")
    private String realName;

    @ApiModelProperty("司机手机号")
    private String phone;

    @ApiModelProperty("承运方式：2 : 代驾  3 : 干线   4：拖车")
    private Integer mode;

    @ApiModelProperty("营运状态：0营运中，1停运中")
    private Integer businessState;

    @ApiModelProperty("状态：0待审核，2已审核，4取消，5冻结  7已驳回，9已停用（CommonStateEnum）")
    private Integer state;

    @ApiModelProperty("司机身份 0：普通司机 1：管理员")
    private Integer identity;

    @ApiModelProperty("总运台")
    private Integer carNum;

    @ApiModelProperty("车牌号")
    private String plateNo;

    @ApiModelProperty("车位")
    private Integer defaultCarryNum;
}
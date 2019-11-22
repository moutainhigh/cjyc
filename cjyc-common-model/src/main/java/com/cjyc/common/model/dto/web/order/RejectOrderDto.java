package com.cjyc.common.model.dto.web.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RejectOrderDto {
    @NotNull
    @ApiModelProperty(value = "loginId", required = true)
    private Long loginId;

    @ApiModelProperty("loginName")
    private String loginName;

    @NotNull
    @ApiModelProperty(value = "订单ID", required = true)
    private Long orderId;

    @NotNull
    @ApiModelProperty(value = "驳回原因", required = true)
    private String reason;
}

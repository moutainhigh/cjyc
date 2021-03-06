package com.cjyc.common.model.dto.customer.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class CarCollectPayDto {

    @ApiModelProperty(value = "用户ID",required = true)
    private Long loginId;
    @ApiModelProperty(value = "用户名称（不用传）")
    private String loginName;
    @ApiModelProperty(value = "订单车辆编号",required = true)
    private List<String> orderCarNos;
    @ApiModelProperty(value = "Ip（不用传）")
    private String ip;
    @ApiModelProperty(value = "支付渠道")
    private String channel;
}

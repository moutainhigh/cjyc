package com.cjyc.common.model.dto.web.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel
public class ChangePriceOrderDto {


    @NotNull
    @ApiModelProperty(value = "用户ID",required = true)
    private Long userId;
    @NotBlank
    @ApiModelProperty(value = "用户名",required = true)
    private String userName;
    @NotNull
    @ApiModelProperty(value = "订单ID",required = true)
    private Long orderId;
    @NotNull
    @ApiModelProperty(value = "实际总收益费用，车辆合计费用（提车费+干线费+送车费+保险费）",required = true)
    private BigDecimal realWlTotalFee;
    @NotNull
    @ApiModelProperty(value = "物流券抵消金额",required = true)
    private BigDecimal couponOffsetFee;
    @NotNull
    @ApiModelProperty(value = "实际总物流费用，客户实际需付(提车费+干线费+送车费+保险费+服务费)",required = true)
    private BigDecimal wlTotalFee;
    @ApiModelProperty(value = "原因")
    private String reason;

    @ApiModelProperty("车辆列表")
    private List<ChangePriceOrderCarDto> orderCarList;

}
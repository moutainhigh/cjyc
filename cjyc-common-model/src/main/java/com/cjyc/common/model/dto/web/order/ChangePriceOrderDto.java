package com.cjyc.common.model.dto.web.order;

import com.cjyc.common.model.constant.ArgsConstant;
import com.cjyc.common.model.dto.web.BaseWebDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel
public class ChangePriceOrderDto extends BaseWebDto {
    @NotNull
    @ApiModelProperty(value = "订单ID",required = true)
    private Long orderId;
    @NotNull
    @ApiModelProperty(value = "物流券抵消金额",required = true)
    @Digits(integer = ArgsConstant.INT_MAX, fraction = ArgsConstant.FRACTION_MAX, message = "金额整数最多8位，小数最多2位")
    @DecimalMin(value = ArgsConstant.DECIMAL_ZERO, message = "金额不能小于0")
    private BigDecimal couponOffsetFee;
    @NotNull
    @ApiModelProperty(value = "实际总物流费用，客户实际需付(提车费+干线费+送车费+保险费+服务费)",required = true)
    @Digits(integer = ArgsConstant.INT_MAX, fraction = ArgsConstant.FRACTION_MAX, message = "金额整数最多8位，小数最多2位")
    @DecimalMin(value = ArgsConstant.DECIMAL_ZERO, message = "金额不能小于0")
    private BigDecimal totalFee;

    @ApiModelProperty(value = "原因")
    private String reason;

    @ApiModelProperty("车辆列表")
    private List<ChangePriceOrderCarDto> orderCarList;

}

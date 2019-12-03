package com.cjyc.common.model.dto.customer.freightBill;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
@Data
public class FindStoreDto implements Serializable {
    private static final long serialVersionUID = -6533219025534413601L;
    @ApiModelProperty(value = "城市编码",required = true)
    @NotBlank(message = "城市编码不能为空")
    private String cityCode;
}
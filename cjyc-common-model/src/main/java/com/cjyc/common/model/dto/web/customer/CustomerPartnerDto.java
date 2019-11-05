package com.cjyc.common.model.dto.web.customer;

import com.cjyc.common.model.dto.BasePageDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
@Data
public class CustomerPartnerDto extends BasePageDto implements Serializable {
    private static final long serialVersionUID = -4471541673400527556L;

    @ApiModelProperty("公司名称")
    private String name;

    @ApiModelProperty("联系人")
    private String contactMan;

    @ApiModelProperty("联系电话")
    private String contactPhone;

    @ApiModelProperty("统一社会信用代码")
    private String socialCreditCode;

    @ApiModelProperty("是否可以开票 0：否 1：是")
    private Integer isInvoice;
}
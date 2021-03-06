package com.cjyc.common.model.dto.web.inquiry;

import com.cjyc.common.model.dto.BasePageDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SelectInquiryDto extends BasePageDto {

    private static final long serialVersionUID = -5463698523193022010L;
    @ApiModelProperty("登陆用户id(loginId)")
    private Long loginId;

    @ApiModelProperty("始发地")
    private String fromCity;

    @ApiModelProperty("目的地")
    private String toCity;

    @ApiModelProperty("处理状态 1：未处理  2：已处理")
    private Integer state;

    @ApiModelProperty("询价开始时间戳")
    private Long startStamp;

    @ApiModelProperty("询价结束时间戳")
    private Long endStamp;

}
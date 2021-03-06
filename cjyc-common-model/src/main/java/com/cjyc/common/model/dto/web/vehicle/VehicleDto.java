package com.cjyc.common.model.dto.web.vehicle;

import com.cjyc.common.model.constant.RegexConstant;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
@Data
public class VehicleDto implements Serializable {

    @ApiModelProperty(value = "当前登陆用户id(loginId)",required = true)
    @NotNull(message = "当前登陆用户id(loginId)不能为空")
    private Long loginId;

    @ApiModelProperty(value = "车牌号",required = true)
    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    @ApiModelProperty(value = "车位数",required = true)
    @NotNull(message = "车位数不能为空")
    @Min(value = 1)
    private Integer defaultCarryNum;

}
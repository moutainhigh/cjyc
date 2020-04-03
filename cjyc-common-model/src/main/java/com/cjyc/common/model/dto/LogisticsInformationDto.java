package com.cjyc.common.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Description 物流信息请求参数
 * @Author Liu Xing Xiang
 * @Date 2020/4/3 10:13
 **/
@Data
public class LogisticsInformationDto implements Serializable {
    private static final long serialVersionUID = 256310332755968389L;
    @NotBlank(message = "订单车辆编号不能为空")
    @ApiModelProperty(value = "订单车辆编号")
    private String orderCarNo;

    @NotBlank(message = "运输车车牌号不能为空")
    @ApiModelProperty(value = "运输车车牌号")
    private String truckPlateNo;
}

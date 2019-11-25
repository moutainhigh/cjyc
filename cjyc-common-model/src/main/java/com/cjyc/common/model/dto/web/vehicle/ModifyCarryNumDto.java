package com.cjyc.common.model.dto.web.vehicle;

import com.cjyc.common.model.constant.RegexConstant;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
@Data
public class ModifyCarryNumDto implements Serializable {
    private static final long serialVersionUID = 4811129462747451190L;

    @ApiModelProperty(value = "司机id(driverId)",required = true)
    @NotNull(message = "司机id(driverId)不能为空")
    private Long driverId;

    @ApiModelProperty(value = "车辆id(vehicleId)",required = true)
    @NotNull(message = "车辆id(vehicleId)不能为空")
    private Long vehicleId;

    @ApiModelProperty(value = "车位数",required = true)
    @NotNull(message = "车位数不能为空")
    private Integer defauleCarryNum;
}
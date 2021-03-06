package com.cjyc.common.model.dto.web.task;

import com.cjyc.common.model.dto.BaseLoginDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel
public class AllotTaskDto extends BaseLoginDto {

    @NotNull(message = "运单不能为空")
    @ApiModelProperty(value = "运单ID",required = true)
    private Long waybillId;
    @NotNull(message = "司机不能为空")
    @ApiModelProperty(value = "司机ID",required = true)
    private Long driverId;

    @ApiModelProperty(value = "指导线路",required = true)
    private String guideLine;

    @ApiModelProperty(value = "备注",required = true)
    private String remark;
    @ApiModelProperty(value = "运单车辆ID",required = true)
    private List<Long> waybillCarIdList;
}

package com.cjyc.common.model.dto.web.waybill;

import com.cjyc.common.model.dto.BasePageDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CrWaybillDto extends BasePageDto {

    @ApiModelProperty(value = "用户登录ID-司机")
    private Long roleId;
    @ApiModelProperty(value = "承运商ID")
    private Long carrierId;
    @ApiModelProperty(value = "运单号")
    private String waybillNo;

    /*@ApiModelProperty(value = "指派进度: 0全部，1未完成，2已完成")
    private Long allotProgress;*/
    @ApiModelProperty(value = "运单类型：1提车运单，2干线运单，3送车运单")
    private Integer waybillType;
    @ApiModelProperty(value = "创建人")
    private String createUser;

}

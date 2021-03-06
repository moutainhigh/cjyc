package com.cjyc.common.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * 城市信息表
 * </p>
 *
 * @author JPG
 * @since 2019-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="City对象", description="城市信息表")
public class CityDto extends BasePageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "行政区编码（含大区、省、市、区）")
    private String code;

    @ApiModelProperty(value = "上级城市编码")
    private String parentCode;

    @ApiModelProperty(value = "行政区名称")
    private String name;

    @NotNull(message = "level不能为空")
    @ApiModelProperty(value = "行政区级别： 0大区， 1省， 2市， 3区县")
    private Integer level;

    @ApiModelProperty(value = "经度")
    private String lng;

    @ApiModelProperty(value = "纬度")
    private String lat;

    @ApiModelProperty(value = "热门城市： 0否， 1是")
    private Boolean hot;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "更新时间")
    private Long updateTime;


}

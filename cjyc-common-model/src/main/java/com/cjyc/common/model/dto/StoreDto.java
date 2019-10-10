package com.cjyc.common.model.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 韵车业务中心信息表
 * </p>
 *
 * @author JPG
 * @since 2019-10-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("s_store")
@ApiModel(value="StoreDto对象", description="韵车业务中心信息表")
public class StoreDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "城市ID")
    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    @ApiModelProperty(value = "部门ID（架构组sys_dept表）")
    private Long deptId;

    @ApiModelProperty(value = "业务中心名称")
    private String name;

    @ApiModelProperty(value = "省")
    private String province;

    @ApiModelProperty(value = "省编码")
    private String provinceCode;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "市编码")
    private String cityCode;

    @ApiModelProperty(value = "区")
    private String area;

    @ApiModelProperty(value = "区编码")
    private String areaCode;

    @ApiModelProperty(value = "具体地址")
    private String detailAddr;

    @ApiModelProperty(value = "业务中心经度")
    private String lng;

    @ApiModelProperty(value = "业务中心纬度")
    private String lat;

    @ApiModelProperty(value = "状态：0待审核，2已开通，4已取消，7已驳回，9已停用")
    private Integer state;

    @ApiModelProperty(value = "创建人ID")
    private Long createUserId;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新人ID")
    private Long updateUserId;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "备注")
    private String remark;


}
package com.cjyc.common.model.entity;

import java.math.BigDecimal;
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
 * 客户（企业）合同表
 * </p>
 *
 * @author JPG
 * @since 2019-11-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("c_customer_contract")
@ApiModel(value="CustomerContract对象", description="客户（企业）合同表")
public class CustomerContract implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "大客户Id")
    private Long customerId;

    @ApiModelProperty(value = "合同编号")
    private String contractNo;

    @ApiModelProperty(value = "合同性质  0：框架  1：单次")
    private Integer contactNature;

    @ApiModelProperty(value = "结算类型 0:时付 1：账期")
    private Integer settleType;

    @ApiModelProperty(value = "账期/天")
    private Integer settlePeriod;

    @ApiModelProperty(value = "合同有效期")
    private Long contractLife;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "项目级别 0：一级  1：二级")
    private Integer projectLevel;

    @ApiModelProperty(value = "主要产品 0：新车  1：二手车 2：新车+二手车")
    private Integer majorProduct;

    @ApiModelProperty(value = "项目性质 0：新开  1：存量")
    private Integer projectNature;

    @ApiModelProperty(value = "项目预计运量")
    private BigDecimal proTraVolume;

    @ApiModelProperty(value = "月度平均运量")
    private BigDecimal avgMthTraVolume;

    @ApiModelProperty(value = "业务覆盖范围")
    private String busiCover;

    @ApiModelProperty(value = "固定路线")
    private String fixedRoute;

    @ApiModelProperty(value = "项目开发人员")
    private String projectDeper;

    @ApiModelProperty(value = "项目负责人")
    private String projectLeader;

    @ApiModelProperty(value = "负责人电话")
    private String leaderPhone;

    @ApiModelProperty(value = "项目状态 0：停止   1：正常")
    private Integer projectStatus;

    @ApiModelProperty(value = "合同是否可用  0：不可用  1：可用")
    private Integer isActive;

    @ApiModelProperty(value = "项目团队成员")
    private String projectTeamPer;

    @ApiModelProperty(value = "立项日期")
    private Long projectEstabTime;

    @ApiModelProperty(value = "主要KPI")
    private String majorKpi;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;
}

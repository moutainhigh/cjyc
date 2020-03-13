package com.cjyc.foreign.api.dto.res;

import com.cjyc.common.model.serizlizer.BigDecimalSerizlizer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description 订单车辆详情返回类
 * @Author Liu Xing Xiang
 * @Date 2020/3/11 17:21
 **/
@Data
public class OrderCarDetailResDto implements Serializable {
    private static final long serialVersionUID = 1950307814239204252L;

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "车辆编码")
    private String no;

    @ApiModelProperty(value = "品牌")
    private String brand;

    @ApiModelProperty(value = "型号")
    private String model;

    @ApiModelProperty(value = "车牌号")
    private String plateNo;

    @ApiModelProperty(value = "vin码")
    private String vin;

    @ApiModelProperty(value = "是否能动 0-否 1-是")
    private Integer isMove;

    @ApiModelProperty(value = "是否新车 0-否 1-是")
    private Integer isNew;

    @ApiModelProperty(value = "估值/万")
    private Integer valuation;

    @ApiModelProperty(value = "当前所在地所属业务中心")
    private Long nowStoreId;

    @ApiModelProperty(value = "当前所在区")
    private String nowAreaCode;

    @ApiModelProperty(value = "当前位置更新时间")
    private Long nowUpdateTime;

    @ApiModelProperty(value = "状态：0待路由，5待提车调度，10待提车，12待自送交车，15提车中（待交车），25待干线调度<循环>（提车入库），35待干线提车<循环>，40干线中<循环>（待干线交车），45待配送调度（干线入库），50待配送提车，55配送中（待配送交车），70待自取提车，100已签收")
    private Integer state;

    @ApiModelProperty(value = "提车状态(调度状态)：1待调度，5已调度，7无需调度")
    private Integer pickState;

    @ApiModelProperty(value = "干线状态(调度状态)：1待调度，2节点调度，5已调度，7无需调度")
    private Integer trunkState;

    @ApiModelProperty(value = "送车状态(调度状态)：1待调度，5已调度，7无需调度")
    private Integer backState;

    @ApiModelProperty(value = "车辆描述")
    private String description;

    @ApiModelProperty(value = "车辆应收提车费")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal pickFee;

    @ApiModelProperty(value = "车辆应收干线费")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal trunkFee;

    @ApiModelProperty(value = "车辆应收送车费")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal backFee;

    @ApiModelProperty(value = "实际提车类型")
    private Integer pickType;

    @ApiModelProperty(value = "实际送车类型")
    private Integer backType;

    @ApiModelProperty(value = "车辆应收保险费")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal addInsuranceFee;

    @ApiModelProperty(value = "保额/万")
    private Integer addInsuranceAmount;

    @ApiModelProperty(value = "物流券抵消金额")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal couponOffsetFee;

    @ApiModelProperty(value = "车辆代收中介费（为资源合伙人代收）")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal agencyFee;

    @ApiModelProperty(value = "单车总费用")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal totalFee;

    @ApiModelProperty(value = "应收状态：0未支付，2已支付")
    private Integer wlPayState;

    @ApiModelProperty(value = "物流费支付时间")
    private Long wlPayTime;

    @ApiModelProperty(value = "完成时间")
    private Long finishTime;
}

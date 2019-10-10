package com.cjyc.common.model.dto;

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
 * 订单表(客户下单)
 * </p>
 *
 * @author JPG
 * @since 2019-10-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("w_order")
@ApiModel(value="OrderDto对象", description="订单表(客户下单)")
public class OrderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @ApiModelProperty(value = "订单编号")
    private String no;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "省")
    private String startProvince;

    @ApiModelProperty(value = "省编号")
    private String startProvinceCode;

    @ApiModelProperty(value = "市")
    private String startCity;

    @ApiModelProperty(value = "市编号")
    private String startCityCode;

    @ApiModelProperty(value = "区")
    private String startArea;

    @ApiModelProperty(value = "区编号")
    private String startAreaCode;

    @ApiModelProperty(value = "出发地详细地址")
    private String startAddress;

    @ApiModelProperty(value = "出发地经度")
    private String startLng;

    @ApiModelProperty(value = "出发地纬度")
    private String startLat;

    @ApiModelProperty(value = "省")
    private String endProvince;

    @ApiModelProperty(value = "省编号")
    private String endProvinceCode;

    @ApiModelProperty(value = "市")
    private String endCity;

    @ApiModelProperty(value = "市编号")
    private String endCityCode;

    @ApiModelProperty(value = "区")
    private String endArea;

    @ApiModelProperty(value = "区编号")
    private String endAreaCode;

    @ApiModelProperty(value = "目的地详细地址")
    private String endAddress;

    @ApiModelProperty(value = "目的地经度")
    private String endLng;

    @ApiModelProperty(value = "目的地纬度")
    private String endLat;

    @ApiModelProperty(value = "预计出发时间（提车日期）")
    private Long expectStartDate;

    @ApiModelProperty(value = "预计到达时间")
    private Long expectEndDate;

    @ApiModelProperty(value = "车辆总数")
    private Integer carNum;

    @ApiModelProperty(value = "线路ID")
    private Long lineId;

    @ApiModelProperty(value = "提车方式:1 自送，2代驾上门，3拖车上门")
    private Integer pickType;

    @ApiModelProperty(value = "发车人")
    private String pickContactName;

    @ApiModelProperty(value = "发车人联系方式")
    private String pickContactPhone;

    @ApiModelProperty(value = "送车方式： 1 自提，2代驾上门，3拖车上门")
    private Integer backType;

    @ApiModelProperty(value = "收车人")
    private String backContactName;

    @ApiModelProperty(value = "收车人联系方式")
    private String backContactPhone;

    @ApiModelProperty(value = "加急")
    private Integer hurryDays;

    @ApiModelProperty(value = "订单来源：1用户app，2用户小程序，12业务员app，12业务员小程序，21韵车后台")
    private Integer source;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "创建人：客户/业务员")
    private String createUserName;

    @ApiModelProperty(value = "创建人类型：0客户，1业务员")
    private Integer createUserType;

    @ApiModelProperty(value = "确认时间")
    private Long checkTime;

    @ApiModelProperty(value = "确认人：业务员")
    private String checkSalesmanName;

    @ApiModelProperty(value = "确认人ID")
    private Long checkSalesmanId;

    @ApiModelProperty(value = "订单状态：0待提交，2待分配，5待确认，10待复确认，15待预付款，25已确认，55运输中，88待付款，100已完成，111原返（待），112异常结束，113取消（待），114作废（待）")
    private Integer state;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "是否开票：0否（默认根据设置），1是")
    private Integer invoiceFlag;

    @ApiModelProperty(value = "发票类型：0无， 1-普通(个人) ，2增值普票(企业) ，3增值专用发票")
    private Integer invoiceType;

    @ApiModelProperty(value = "应收提车费")
    private BigDecimal pickFee;

    @ApiModelProperty(value = "应收干线费")
    private BigDecimal trunkFee;

    @ApiModelProperty(value = "应收配送费")
    private BigDecimal backFee;

    @ApiModelProperty(value = "应收保险费")
    private BigDecimal insuranceFee;

    @ApiModelProperty(value = "应收订单定金（保留字段）")
    private BigDecimal depositFee;

    @ApiModelProperty(value = "代收中介费（为资源合伙人代收）")
    private BigDecimal agencyFee;

    @ApiModelProperty(value = "应收总价：提车费+干线费+送车费+保险费+中介费")
    private BigDecimal totalFee;

    @ApiModelProperty(value = "车辆均摊费用（提车费/送车费/中转费）方式：0均分余数散列（默认），1不均分")
    private Integer feeShareType;

    @ApiModelProperty(value = "合同ID")
    private Long customerContractId;

    @ApiModelProperty(value = "客户付款方式：0时付（默认），1账期")
    private Integer customerPayType;

    @ApiModelProperty(value = "客户支付尾款状态：0未支付，1部分支付，2支付完成")
    private Integer wlPayState;

    @ApiModelProperty(value = "上次客户支付尾款时间")
    private Long wlPayTime;

    @ApiModelProperty(value = "线下收款标识：默认0（不允许），")
    private Integer offlinePayFlag;


}
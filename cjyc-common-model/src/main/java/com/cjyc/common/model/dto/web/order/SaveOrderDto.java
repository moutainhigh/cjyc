package com.cjyc.common.model.dto.web.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author JPG
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel
public class SaveOrderDto {

    @NotNull
    @ApiModelProperty(value = "1WEB管理后台, 2业务员APP, 4司机APP, 6用户端APP, 7用户端小程序", required = true)
    private int clientId;
    @NotNull
    @ApiModelProperty(value = "操作人id", required = true)
    private Long loginId;
    @ApiModelProperty(value = "操作人(不用传)")
    private Long loginName;
    @ApiModelProperty(value = "物流券抵消金额")
    private BigDecimal couponOffsetFee;
    @ApiModelProperty(value = "车辆列表")
    private List<SaveOrderCarDto> orderCarList;



    @ApiModelProperty(value = "订单ID（修改时传）")
    private Long orderId;
    @NotNull
    @ApiModelProperty(value = "1C端 2大客户 3-伙人", required = true)
    private int customerType;
    @ApiModelProperty(value = "客户id")
    private Long customerId;
    @NotBlank
    @ApiModelProperty(value = "客户电话", required = true)
    private String customerPhone;
    @NotBlank
    @ApiModelProperty(value = "客户姓名", required = true)
    private String customerName;
    @NotBlank
    @ApiModelProperty(value = "区编号", required = true)
    private String startAreaCode;
    @ApiModelProperty(value = "始发地详细地址", required = true)
    private String startAddress;
    @ApiModelProperty(value = "出发地业务中心ID: -1不经过业务中心")
    private Long startStoreId;
    @ApiModelProperty(value = "出发地业务中心名称")
    private String startStoreName;
    @ApiModelProperty(value = "区编号", required = true)
    private String endAreaCode;
    @ApiModelProperty(value = "目的地详细地址", required = true)
    private String endAddress;
    @ApiModelProperty(value = "目的地业务中心ID: -1不经过业务中心")
    private Long endStoreId;
    @ApiModelProperty(value = "目的地业务中心名称")
    private String endStoreName;

    @ApiModelProperty(value = "订单所属业务中心ID")
    private Long inputStoreId;
    @ApiModelProperty(value = "订单所属业务中心名称")
    private String inputStoreName;
    @ApiModelProperty(value = "期望提车日期")
    private String expectStartDate;
    @ApiModelProperty(value = "期望到达日期")
    private String expectEndDate;
    @ApiModelProperty(value = "车辆总数")
    private Integer carNum;
    @ApiModelProperty(value = "线路ID")
    private Long lineId;
    @ApiModelProperty(value = "提车方式:1 自送，2代驾上门，3拖车上门, 4.物流上门")
    private int pickType;
    @ApiModelProperty(value = "发车联系人")
    private String pickContactName;
    @ApiModelProperty(value = "发车联系人电话")
    private String pickContactPhone;
    @ApiModelProperty(value = "送车方式： 1 自提，2代驾上门，3拖车上门, 4.物流上门")
    private int backType;
    @ApiModelProperty(value = "收车联系人")
    private String backContactName;
    @ApiModelProperty(value = "收车联系人电话")
    private String backContactPhone;
    @ApiModelProperty(value = "是否开票：0否（默认根据设置），1是")
    private int invoiceFlag;
    @ApiModelProperty(value = "发票类型：0无， 1-普通(个人) ，2增值普票(企业) ，3增值专用发票")
    private int invoiceType;
    @ApiModelProperty(value = "合同ID")
    private Long customerContractId;
    @ApiModelProperty(value = "备注")
    private String remark;
    @ApiModelProperty(value = "创建人：客户/业务员")
    private String createUserName;
    @ApiModelProperty(value = "创建人类型：0客户，1业务员")
    private Long createUserId;
    @ApiModelProperty(value = "支付方式 0-到付，1-预付，2账期")
    private Integer payType;
    @ApiModelProperty(value = "优惠券id")
    private Long couponSendId;
    @NotNull
    @ApiModelProperty(value = "应收总价：收车后客户应支付平台的费用", required = true)
    private BigDecimal totalFee;

    @ApiModelProperty(value = "状态（不需要传）")
    private Integer state;
}

package com.cjyc.web.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @auther litan
 * @description: com.cjyc.web.api.dto
 * @date:2019/9/29
 */
@Data(staticConstructor = "getInstance")
public class OrderCommitDto {

    @NotNull
    @ApiModelProperty(value = "1WEB管理后台, 2业务员APP, 3业务员小程序, 4业务员APP, 5业务员小程序, 6用户端APP, 7用户端小程序", required = true)
    private int clientId;
    @ApiModelProperty(value = "用户ID")
    private Long orderId;
    @NotNull
    @ApiModelProperty(value = "1-c端 2-大客户 3-合伙人",required = true)
    private int customerType;
    @NotNull
    @ApiModelProperty(value = "0-保存（预订单） 1-提交",required = true)
    private int saveType;//0-保存（草稿） 1-下单
    @ApiModelProperty(value = "客户id")
    private Long customerId;
    @ApiModelProperty(value = "客户姓名",required = true)
    private String customerName;


    @ApiModelProperty(value = "始发地详细地址",required = true)
    private String startAddress;
    @ApiModelProperty(value = "出发地业务中心ID: -1不经过业务中心")
    private Long startStoreId;
    @ApiModelProperty(value = "出发地业务中心名称")
    private String startStoreName;
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
    @ApiModelProperty(value = "目的地详细地址",required = true)
    private String endAddress;
    @ApiModelProperty(value = "目的地业务中心ID: -1不经过业务中心")
    private Long endStoreId;
    @ApiModelProperty(value = "目的地业务中心名称")
    private String endStoreName;


    @ApiModelProperty(value = "订单所属业务中心ID")
    private Long inputStoreId;
    @ApiModelProperty(value = "订单所属业务中心名称")
    private String inputStoreName;

    @ApiModelProperty(value = "期望提车日期",required = true)
    private String expectStartDate;
    @ApiModelProperty(value = "期望到达日期",required = true)
    private String expectEndDate;
    @ApiModelProperty(value = "车辆总数")
    private Integer carNum;
    @ApiModelProperty(value = "线路ID")
    private Long lineId;

    @ApiModelProperty(value = "提车方式:1 自送，2代驾上门，3拖车上门, 4.物流上门",required = true)
    private int pickType;
    @ApiModelProperty(value = "发车联系人",required = true)
    private String pickContactName;
    @ApiModelProperty(value = "发车联系人电话",required = true)
    private String PickContactPhone;
    @ApiModelProperty(value = "送车方式： 1 自提，2代驾上门，3拖车上门, 4.物流上门",required = true)
    private int backType;
    @ApiModelProperty(value = "收车联系人",required = true)
    private String backContactName;
    @ApiModelProperty(value = "收车联系人电话",required = true)
    private String backContactPhone;

    @ApiModelProperty(value = "是否开票：0否（默认根据设置），1是")
    private int invoiceFlag;
    @ApiModelProperty(value = "发票类型：0无， 1-普通(个人) ，2增值普票(企业) ，3增值专用发票")
    private int invoiceType;

    @ApiModelProperty(value = "合同ID")
    private Long customerContractId;//合同ID
    @ApiModelProperty(value = "加急")
    private Integer hurryDays;
    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建人：客户/业务员")
    private String createUserName;
    @ApiModelProperty(value = "创建人类型：0客户，1业务员")
    private Long createUserId;
    @ApiModelProperty(value = "支付方式 0-到付，1-预付，2账期",required = true)
    private Integer payType;

    @ApiModelProperty(value = "优惠券id")
    private Long couponId;//优惠券id

    @ApiModelProperty(value = "实际总费用")
    private BigDecimal realTotalFee;
    @ApiModelProperty(value = "物流券抵消金额")
    private BigDecimal couponOffsetFee;
    @ApiModelProperty(value = "实际总费用")
    private BigDecimal totalFee;

    @ApiModelProperty(value = "状态（不需要传）")
    private Integer state;

    /**车辆列表*/
    private List<OrderCarDto> orderCarDtoList;
}
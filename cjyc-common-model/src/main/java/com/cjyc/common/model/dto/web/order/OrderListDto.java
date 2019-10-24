package com.cjyc.common.model.dto.web.order;

import com.cjyc.common.model.dto.BasePageDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel
public class OrderListDto extends BasePageDto {

    /**
     0待提交    1预订单
     2待分配    5待确认
     5待确认    5待确认
     10待复确认 5待确认
     15待预付款 15待付款
     25已确认   25待调度
     55运输中   55运输中
     88待付款
     100已完成  100已交付
     111原返（待）
     112异常结束
     113取消（待）113已取消
     114作废（待）114已作废
     */
    @ApiModelProperty(value = "订单状态：")
    private Integer outterState;

    @ApiModelProperty(value = "订单编号")
    private String no;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    //客户表
    @ApiModelProperty(value = "客户类型")
    private String customerType;
    //客户表
    @ApiModelProperty(value = "客户电话")
    private String customerPhone;

    @ApiModelProperty(value = "订单所属业务中心ID")
    private Long inputStoreId;

    @ApiModelProperty(value = "省编号")
    private String startProvinceCode;

    @ApiModelProperty(value = "市编号")
    private String startCityCode;

    @ApiModelProperty(value = "区编号")
    private String startAreaCode;

    @ApiModelProperty(value = "出发地业务中心ID: -1不经过业务中心")
    private Long startStoreId;

    @ApiModelProperty(value = "省编号")
    private String endProvinceCode;

    @ApiModelProperty(value = "市编号")
    private String endCityCode;

    @ApiModelProperty(value = "区编号")
    private String endAreaCode;

    @ApiModelProperty(value = "目的地业务中心ID: -1不经过业务中心")
    private Long endStoreId;

    @ApiModelProperty(value = "起始预计出发时间（提车日期）")
    private Long beginExpectStartDate;
    @ApiModelProperty(value = "截止预计出发时间（提车日期）")
    private Long endExpectStartDate;

    @ApiModelProperty(value = "起始预计到达时间")
    private Long beginExpectEndDate;
    @ApiModelProperty(value = "截止预计到达时间")
    private Long endExpectEndDate;


    @ApiModelProperty(value = "提车方式:1 自送，2代驾上门，3拖车上门，4物流上门")
    private Integer pickType;

    @ApiModelProperty(value = "发车人")
    private String pickContactName;

    @ApiModelProperty(value = "发车人联系方式")
    private String pickContactPhone;

    @ApiModelProperty(value = "送车方式： 1 自提，2代驾上门，3拖车上门，4物流上门")
    private Integer backType;

    @ApiModelProperty(value = "收车人")
    private String backContactName;

    @ApiModelProperty(value = "收车人联系方式")
    private String backContactPhone;


    @ApiModelProperty(value = "线路ID")
    private Long lineId;

    @ApiModelProperty(value = "订单来源：1WEB管理后台, 2业务员APP, 3业务员小程序, 4业务员APP, 5业务员小程序, 6用户端APP, 7用户端小程序")
    private Integer source;

    @ApiModelProperty(value = "起始创建时间")
    private Long beginCreateTime;
    @ApiModelProperty(value = "截止创建时间")
    private Long endCreateTime;

    @ApiModelProperty(value = "创建人：客户/业务员")
    private String createUserName;


    @ApiModelProperty(value = "起始确认时间")
    private Long beginCheckTime;
    @ApiModelProperty(value = "截止确认时间")
    private Long endCheckTime;

    @ApiModelProperty(value = "客户付款方式：0到付（默认），1预付，2账期")
    private Integer payType;

    @ApiModelProperty(value = "大区编码")
    private Integer regionCode;


}
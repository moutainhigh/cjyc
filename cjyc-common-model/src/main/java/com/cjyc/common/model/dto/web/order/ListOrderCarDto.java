package com.cjyc.common.model.dto.web.order;

import com.cjyc.common.model.dto.BasePageDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel
public class ListOrderCarDto extends BasePageDto {
    private Long loginId;
    @ApiModelProperty(value = "角色ID")
    private Long roleId;
    @ApiModelProperty(value = "业务范围(不用传)")
    private Set<Long> bizScope;

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
    @ApiModelProperty(value = "订单状态：0全部订单，1预订单，5待确认，15待付款，25待调度，55运输中，100已交付，113已取消，114已作废")
    private Integer outterState;

    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "客户类型")
    private Integer customerType;

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


    @ApiModelProperty(value = "日期类型：1指定提车日期，2预计到达时间，3创建时间，4确认时间，5完结时间")
    private Long timeType;
    @ApiModelProperty(value = "起始（提车日期）")
    private Long beginTime;
    @ApiModelProperty(value = "截止（提车日期）")
    private Long endTime;


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

    @ApiModelProperty(value = "订单来源：0全部订单，1预订单，5待确认，15待付款，25待调度，55运输中，100已交付，113已取消，114已作废")
    private Integer source;

    @ApiModelProperty(value = "创建人：客户/业务员")
    private String createUserName;



    @ApiModelProperty(value = "客户付款方式：0到付（默认），1预付，2账期")
    private Integer payType;
    @ApiModelProperty(value = "大区编码")
    private String regionCode;
    @ApiModelProperty(value = "是否能动 0-否 1-是")
    private Integer isMove;
    @ApiModelProperty(value = "是否新车 0-否 1-是")
    private Integer isNew;
    @ApiModelProperty(value = "车辆编码")
    private String carNo;
    @ApiModelProperty(value = "品牌")
    private String brand;
    @ApiModelProperty(value = "型号")
    private String model;
    @ApiModelProperty(value = "车牌号")
    private String plateNo;
    @ApiModelProperty(value = "vin码")
    private String vin;
    @ApiModelProperty(value = "当前业务中心")
    private String nowStoreId;





}

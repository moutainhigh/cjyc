package com.cjyc.common.model.vo.web.finance;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.cjyc.common.model.enums.customer.ClientTypeEnum;
import com.cjyc.common.model.enums.customer.CustomerPayTypeEnum;
import com.cjyc.common.model.enums.customer.CustomerTypeEnum;
import com.cjyc.common.model.serizlizer.BigDecimalSerizlizer;
import com.cjyc.common.model.util.LocalDateTimeUtil;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: Hut
 * @Date: 2019/12/9 15:
 * 已收款（时付）
 */
@Data
public class PaymentVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "车辆编号")
    @Excel(name = "车辆编号" ,orderNum = "0")
    private String no;
    @ApiModelProperty(value = "品牌")
    @Excel(name = "品牌" ,orderNum = "1")
    private String brand;
    @ApiModelProperty(value = "车系")
    @Excel(name = "车系" ,orderNum = "2")
    private String model;
    @ApiModelProperty(value = "vin码")
    @Excel(name = "vin码" ,orderNum = "3")
    private String vin;
    @ApiModelProperty(value = "结算类型")
    @Excel(name = "结算类型" ,orderNum = "4")
    private String  payModeName;

    @JsonSerialize(using = BigDecimalSerizlizer.class)
    @ApiModelProperty(value = "应收运费")
    @Excel(name = "应收运费" ,orderNum = "5",type = 10)
    private BigDecimal freightReceivable;

    @JsonSerialize(using = BigDecimalSerizlizer.class)
    @ApiModelProperty(value = "实收运费")
    @Excel(name = "实收运费" ,orderNum = "6",type = 10)
    private BigDecimal freightPay;

    @JsonSerialize(using = BigDecimalSerizlizer.class)
    @ApiModelProperty(value = "实际收益")
    @Excel(name = "实际收益" ,orderNum = "7",type = 10)
    private BigDecimal totalIncome;
    @ApiModelProperty(value = "支付方式：0到付（默认），1预付，2账期")
    private Integer payType;

    @ApiModelProperty(value = "支付方式：0到付（默认），1预付，2账期")
    @Excel(name = "支付方式" ,orderNum = "8")
    private String payTypeStr;
    public String getPayTypeStr() {
        Integer type = getPayType();
        if(type!=null && type== CustomerPayTypeEnum.TO_PAY.code){
            return CustomerPayTypeEnum.TO_PAY.name;
        }else if(type!=null && type==CustomerPayTypeEnum.PRE_PAY.code){
            return CustomerPayTypeEnum.PRE_PAY.name;
        }else if(type!=null && type==CustomerPayTypeEnum.PERIOD_PAY.code){
            return CustomerPayTypeEnum.PERIOD_PAY.name;
        }else{
            return "";
        }
    }
    @ApiModelProperty(value = "订单编号")
    @Excel(name = "订单编号" ,orderNum = "9")
    private String orderNo;
    @ApiModelProperty(value = "订单所属大区")
    @Excel(name = "订单所属大区" ,orderNum = "10")
    private String largeArea;
    @ApiModelProperty(value = "订单所属业务中心")
    @Excel(name = "订单所属业务中心" ,orderNum = "11")
    private String inputStoreName;
    @ApiModelProperty(value = "始发地")
    @Excel(name = "始发地" ,orderNum = "12")
    private String startAddress;

    @ApiModelProperty(value = "目的地")
    @Excel(name = "目的地" ,orderNum = "13")
    private String endAddress;

    @ApiModelProperty(value = "交付时间")
    private Long deliveryDate;

    @Excel(name = "交付时间" ,orderNum = "14")
    @ApiModelProperty(value = "交付时间")
    private String deliveryDateStr;
    public String getDeliveryDateStr() {
        Long date = getDeliveryDate();
        if (null == date || date <= 0L) {
            return "";
        }
        return LocalDateTimeUtil.formatLDT(LocalDateTimeUtil.convertLongToLDT(date), "yyyy-MM-dd HH:mm:ss");
    }
    @ApiModelProperty(value = "客户Id")
    private Long customerId;
    @ApiModelProperty(value = "客户类型")
    private Integer type;

    @Excel(name = "客户类型" ,orderNum = "15")
    private String customTypeName;

    public String getCustomTypeName() {
        Integer type = getType();
        if(type!=null && type== ClientTypeEnum.INDIVIDUAL.code){
            return ClientTypeEnum.INDIVIDUAL.name;
        }else if(type!=null && type==CustomerTypeEnum.ENTERPRISE.code){
            return ClientTypeEnum.ENTERPRISE.name;
        }else if(type!=null && type==CustomerTypeEnum.COOPERATOR.code){
            return ClientTypeEnum.COOPERATOR.name;
        }else{
            return "";
        }
    }

    @ApiModelProperty(value = "客户名称")
    @Excel(name = "客户名称" ,orderNum = "16")
    private String customerName;

    @ApiModelProperty(value = "合同Id")
    private Long customerContractId;

    @ApiModelProperty(value = "收款时间")
    private Long payTime;

    @Excel(name = "收款时间" ,orderNum = "17")
    private String payTimeStr;

    public String getPayTimeStr() {

        Long date = getPayTime();
        if (null == date || date <= 0L) {
            return "";
        }
        return LocalDateTimeUtil.formatLDT(LocalDateTimeUtil.convertLongToLDT(date), "yyyy-MM-dd HH:mm:ss");
    }
}

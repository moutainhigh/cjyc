package com.cjyc.common.model.vo.salesman.dispatch;

import com.cjyc.common.model.serizlizer.BigDecimalSerizlizer;
import com.cjyc.common.model.serizlizer.DateLongSerizlizer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description 历史调度记录
 * @Author Liu Xing Xiang
 * @Date 2019/12/13 17:07
 **/
@Data
public class HistoryDispatchRecordVo implements Serializable {
    private static final long serialVersionUID = 7800535148779633095L;
    @ApiModelProperty(value = "运单id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long waybillId;

    @ApiModelProperty(value = "运单编号")
    private String waybillNo;

    @ApiModelProperty(value = "运单类型：1提车运单，2干线运单，3送车运单")
    private Integer waybillType;

    @ApiModelProperty(value = "指导线路")
    private String guideLine;

    @ApiModelProperty(value = "调度日期")
    @JsonSerialize(using = DateLongSerizlizer.class)
    private Long createTime;

    @ApiModelProperty(value = "车辆数量")
    private int carNum;

    @ApiModelProperty(value = "承运商类型：1干线-个人承运商，2干线-企业承运商，3同城-业务员，4同城-代驾，5同城-拖车，6客户自己")
    private Integer carrierType;

    @ApiModelProperty(value = "运单运费")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal freightFee;

    @ApiModelProperty(value = "承运商联系人")
    private String linkMan;

    @ApiModelProperty(value = "承运商手机号")
    private String linkmanPhone;

    @ApiModelProperty(value = "订单车辆ID")
    private Long orderCarId;

    @ApiModelProperty(value = "承运商ID")
    private Long carrierId;

    public String getGuideLine() {
        return StringUtils.isBlank(guideLine) ? "" : guideLine;
    }

    public String getLinkMan() {
        return StringUtils.isBlank(linkMan) ? "" : linkMan;
    }

    public String getLinkmanPhone() {
        return StringUtils.isBlank(linkmanPhone) ? "" : linkmanPhone;
    }

    public BigDecimal getFreightFee() {
        return freightFee == null ? new BigDecimal(0) : freightFee;
    }
}

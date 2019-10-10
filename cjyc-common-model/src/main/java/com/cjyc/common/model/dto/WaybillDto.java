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
 * 运单表(业务员调度单)
 * </p>
 *
 * @author JPG
 * @since 2019-10-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("w_waybill")
@ApiModel(value="WaybillDto对象", description="运单表(业务员调度单)")
public class WaybillDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @ApiModelProperty(value = "运单编号")
    private String waybillNo;

    @ApiModelProperty(value = "运单类型：1提车运单，2送车运单，8干线运单")
    private Integer type;

    @ApiModelProperty(value = "调度类型：1自己处理，2人工调度")
    private Integer dispatchType;

    @ApiModelProperty(value = "指导线路")
    private String guideLine;

    @ApiModelProperty(value = "推荐线路")
    private String recommendLine;

    @ApiModelProperty(value = "承运商ID")
    private Long carrierId;

    @ApiModelProperty(value = "车数量")
    private Integer carNum;

    @ApiModelProperty(value = "运单状态："
                            +"0待分配承运商（竞抢），"
                            +"15待承运商承接任务，"
                            +"30运输中，"
                            +"100已完成，"
                            +"102已撤回，"
                            +"103已拒接，"
                            +"111超时关闭")
    private Integer state;

    @ApiModelProperty(value = "运单总运费")
    private BigDecimal freightFee;

    @ApiModelProperty(value = "运费支付状态")
    private Integer freightPayState;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "运费支付时间")
    private String freightPayTime;

    @ApiModelProperty(value = "运费支付流水单号")
    private String freightPayBillno;

    @ApiModelProperty(value = "调度人")
    private String createUser;

    @ApiModelProperty(value = "调度人ID")
    private Long createUserId;


}
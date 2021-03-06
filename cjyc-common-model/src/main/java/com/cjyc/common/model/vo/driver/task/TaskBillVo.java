package com.cjyc.common.model.vo.driver.task;

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
 * @Description 已交付任务返回实体
 * @Author Liu Xing Xiang
 * @Date 2019/11/19 11:34
 **/
@Data
public class TaskBillVo implements Serializable {
    private static final long serialVersionUID = -5688740696634356580L;
    @ApiModelProperty(value = "运单id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long waybillId;

    @ApiModelProperty(value = "任务单id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long taskId;

    @ApiModelProperty(value = "任务单车辆id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long taskCarId;

    @ApiModelProperty(value = "任务状态：0待承接(弃)，5待装车，55运输中，100已完成，113已取消，115已拒接")
    private Integer taskState;

    @ApiModelProperty(value = "运单编号")
    private String waybillNo;

    @ApiModelProperty(value = "指导线路")
    private String guideLine;

    @ApiModelProperty(value = "车辆数")
    private Integer carNum;

    @ApiModelProperty(value = "运单类型：1提车运单，2干线运单，3送车运单")
    private Integer type;

    @ApiModelProperty(value = "运单总运费")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal freightFee;

    @ApiModelProperty(value = "约定/实际提车时间")
    @JsonSerialize(using = DateLongSerizlizer.class)
    private Long startTime;

    @ApiModelProperty(value = "接单时间 或 分配时间")
    @JsonSerialize(using = DateLongSerizlizer.class)
    private Long createTime;

    @ApiModelProperty(value = "交车时间")
    @JsonSerialize(using = DateLongSerizlizer.class)
    private Long completeTime;

    @ApiModelProperty(value = "司机名称")
    private String driverName;

    @ApiModelProperty(value = "司机电话")
    private String driverPhone;

    @ApiModelProperty(value = "运力车牌号")
    private String vehiclePlateNo;

    @ApiModelProperty(value = "始发地城市")
    private String startCity;

    @ApiModelProperty(value = "目的地城市")
    private String endCity;

    public String getStartCity() {
        return StringUtils.isBlank(startCity) ? "" : startCity;
    }
    public String getEndCity() {
        return StringUtils.isBlank(endCity) ? "" : endCity;
    }
    public Long getWaybillId() {
        return waybillId == null ? 0 : waybillId;
    }
    public Long getTaskCarId() {
        return taskCarId == null ? 0 : taskCarId;
    }
    public Integer getTaskState() {
        return taskState == null ? -1 : taskState;
    }
    public String getWaybillNo() {
        return StringUtils.isBlank(waybillNo) ? "" : waybillNo;
    }
    public Integer getType() {
        return type == null ? -1 : type;
    }
    public BigDecimal getFreightFee() {
        return freightFee == null ? new BigDecimal(0) : freightFee;
    }
    public Long getCreateTime() {
        return createTime == null ? 0 : createTime;
    }
    public Long getTaskId() {
        return taskId == null ? 0 : taskId;
    }
    public Long getStartTime() {
        return startTime == null ? 0 : startTime;
    }
    public Long getCompleteTime() {
        return completeTime == null ? 0 : completeTime;
    }
    public String getDriverName() {
        return StringUtils.isBlank(driverName) ? "" : driverName;
    }
    public String getDriverPhone() {
        return StringUtils.isBlank(driverPhone) ? "" : driverPhone;
    }
    public String getVehiclePlateNo() {
        return StringUtils.isBlank(vehiclePlateNo) ? "" : vehiclePlateNo;
    }
    public String getGuideLine() {
        return StringUtils.isBlank(guideLine) ? "" : guideLine;
    }
    public Integer getCarNum() {
        return carNum == null ? 0 : carNum;
    }
}

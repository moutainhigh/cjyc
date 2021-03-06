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
 * @Description 运单车辆详情
 * @Author Liu Xing Xiang
 * @Date 2019/11/20 13:26
 **/
@Data
public class WaybillCarDetailVo implements Serializable {
    private static final long serialVersionUID = -6283733541452871958L;
    @ApiModelProperty(value = "运单车辆id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @ApiModelProperty(value = "订单ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long orderId;

    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    @ApiModelProperty(value = "运单车辆状态：0待指派，2已指派，5待装车，15待装车确认，45已装车，70已卸车，90确认交车, 100确认收车, 105待重连，120已重连")
    private Integer waybillCarState;

    @ApiModelProperty(value = "提车联系人")
    private String loadLinkName;

    @ApiModelProperty(value = "提车联系人电话")
    private String loadLinkPhone;

    @ApiModelProperty(value = "装车地址")
    private String startAddress;

    @ApiModelProperty(value = "收车人名称")
    private String unloadLinkName;

    @ApiModelProperty(value = "收车人电话")
    private String unloadLinkPhone;

    @ApiModelProperty(value = "卸车地址")
    private String endAddress;

    @ApiModelProperty(value = "提车日期")
    @JsonSerialize(using = DateLongSerizlizer.class)
    private Long expectStartTime;

    @ApiModelProperty(value = "交车日期")
    @JsonSerialize(using = DateLongSerizlizer.class)
    private Long unloadTime;

    @ApiModelProperty(value = "提车图片地址，逗号分隔")
    private String loadPhotoImg;

    @ApiModelProperty(value = "历史图片地址，逗号分隔")
    private String historyLoadPhotoImg;

    @ApiModelProperty(value = "交车图片地址，逗号分隔:已交付的运单")
    private String unloadPhotoImg;

    @ApiModelProperty(value = "指导线路")
    private String guideLine;

    @ApiModelProperty(value = "品牌")
    private String brand;

    @ApiModelProperty(value = "logo图片")
    private String logoPhotoImg;

    @ApiModelProperty(value = "型号")
    private String model;

    @ApiModelProperty(value = "车牌号")
    private String plateNo;

    @ApiModelProperty(value = "vin码")
    private String vin;

    @ApiModelProperty(value = "客户付款方式：0到付（默认），1预付，2账期")
    private Integer payType;

    @ApiModelProperty(value = "提车方式:1 自送，2代驾上门，3拖车上门，4物流上门")
    private Integer pickType;

    @ApiModelProperty(value = "送车方式： 1 自提，2代驾上门，3拖车上门，4物流上门")
    private Integer backType;

    @ApiModelProperty(value = "最后一次运输标识：0否，1是")
    private Boolean receiptFlag;

    @ApiModelProperty(value = "运单ID")
    private Long waybillId;

    @ApiModelProperty(value = "运单编号")
    private String waybillNo;

    @ApiModelProperty(value = "订单车辆ID")
    private Long orderCarId;

    @ApiModelProperty(value = "车辆编号")
    private String orderCarNo;

    @ApiModelProperty(value = "运费")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal freightFee;

    @ApiModelProperty(value = "省")
    private String startProvince;

    @ApiModelProperty(value = "省编码")
    private String startProvinceCode;

    @ApiModelProperty(value = "市")
    private String startCity;

    @ApiModelProperty(value = "市编码")
    private String startCityCode;

    @ApiModelProperty(value = "区")
    private String startArea;

    @ApiModelProperty(value = "区县编码")
    private String startAreaCode;

    @ApiModelProperty(value = "出发地业务中心名称")
    private String startStoreName;

    @ApiModelProperty(value = "出发地业务中心ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long startStoreId;

    @ApiModelProperty(value = "起始地所属业务中心ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long startBelongStoreId;

    @ApiModelProperty(value = "省")
    private String endProvince;

    @ApiModelProperty(value = "省编码")
    private String endProvinceCode;

    @ApiModelProperty(value = "市")
    private String endCity;

    @ApiModelProperty(value = "市编码")
    private String endCityCode;

    @ApiModelProperty(value = "区")
    private String endArea;

    @ApiModelProperty(value = "区县编码")
    private String endAreaCode;

    @ApiModelProperty(value = "目的地业务中心名称")
    private String endStoreName;

    @ApiModelProperty(value = "目的地业务中心ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long endStoreId;

    @ApiModelProperty(value = "目的地所属业务中心ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long endBelongStoreId;

    @ApiModelProperty(value = "线路ID")
    private Long lineId;

    @ApiModelProperty(value = "是否有线路")
    private boolean hasLine;

    @ApiModelProperty(value = "预计到达时间")
    private Long expectEndTime;

    @ApiModelProperty(value = "提车联系人userid")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long loadLinkUserId;

    @ApiModelProperty(value = "实际开始装车时间")
    private Long loadTime;

    @ApiModelProperty(value = "收车联系人userId")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long unloadLinkUserId;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "干线线路费")
    @JsonSerialize(using = BigDecimalSerizlizer.class)
    private BigDecimal lineFreightFee;

    public BigDecimal getLineFreightFee() {
        return lineFreightFee == null ? new BigDecimal(0) : lineFreightFee;
    }
    public BigDecimal getFreightFee() {
        return freightFee == null ? new BigDecimal(0) : freightFee;
    }
    public String getStartProvince() {
        return StringUtils.isBlank(startProvince) ? "" : startProvince;
    }
    public String getStartProvinceCode() {
        return startProvinceCode == null ? "" : startProvinceCode;
    }
    public String getStartCity() {
        return StringUtils.isBlank(startCity) ? "" : startCity;
    }
    public String getStartCityCode() {
        return StringUtils.isBlank(startCityCode) ? "" : startCityCode;
    }
    public String getStartArea() {
        return StringUtils.isBlank(startArea) ? "" : startArea;
    }
    public String getStartAreaCode() {
        return StringUtils.isBlank(startAreaCode) ? "" : startAreaCode;
    }
    public String getStartStoreName() {
        return StringUtils.isBlank(startStoreName) ? "" : startStoreName;
    }
    public Long getStartStoreId() {
        return startStoreId == null ? 0 : startStoreId;
    }
    public Long getStartBelongStoreId() {
        return startBelongStoreId == null ? 0 : startBelongStoreId;
    }
    public String getEndProvince() {
        return StringUtils.isBlank(endProvince) ? "" : endProvince;
    }
    public String getEndProvinceCode() {
        return StringUtils.isBlank(endProvinceCode) ? "" : endProvinceCode;
    }
    public String getEndCity() {
        return StringUtils.isBlank(endCity) ? "" : endCity;
    }
    public String getEndCityCode() {
        return StringUtils.isBlank(endCityCode) ? "" : endCityCode;
    }
    public String getEndArea() {
        return StringUtils.isBlank(endArea) ? "" : endArea;
    }
    public String getEndAreaCode() {
        return StringUtils.isBlank(endAreaCode) ? "" : endAreaCode;
    }
    public String getEndStoreName() {
        return StringUtils.isBlank(endStoreName) ? "" : endStoreName;
    }
    public Long getEndStoreId() {
        return endStoreId == null ? 0 : endStoreId;
    }
    public Long getEndBelongStoreId() {
        return endBelongStoreId == null ? 0 : endBelongStoreId;
    }
    public Long getLineId() {
        return lineId == null ? 0 : lineId;
    }
    public Long getExpectEndTime() {
        return expectEndTime == null ? 0 : expectEndTime;
    }
    public Long getLoadLinkUserId() {
        return loadLinkUserId == null ? 0 : loadLinkUserId;
    }
    public Long getLoadTime() {
        return loadTime == null ? 0 : loadTime;
    }
    public Long getUnloadLinkUserId() {
        return unloadLinkUserId == null ? 0 : unloadLinkUserId;
    }
    public Long getCreateTime() {
        return createTime == null ? 0 : createTime;
    }
    public Integer getPickType() {
        return pickType == null ? -1 : pickType;
    }
    public Integer getBackType() {
        return backType == null ? -1 : backType;
    }
    public String getGuideLine() {
        return StringUtils.isBlank(guideLine) ? "" : guideLine;
    }
    public String getLogoPhotoImg() {
        return StringUtils.isBlank(logoPhotoImg) ? "" : logoPhotoImg;
    }
    public String getHistoryLoadPhotoImg() {
        return StringUtils.isBlank(historyLoadPhotoImg) ? "" : historyLoadPhotoImg;
    }
    public Integer getWaybillCarState() {
        return waybillCarState == null ? -1 : waybillCarState;
    }
    public String getStartAddress() {
        return StringUtils.isBlank(startAddress) ? "" : startAddress;
    }
    public String getEndAddress() {
        return StringUtils.isBlank(endAddress) ? "" : endAddress;
    }
    public String getBrand() {
        return StringUtils.isBlank(brand) ? "" : brand;
    }
    public String getModel() {
        return StringUtils.isBlank(model) ? "" : model;
    }
    public Integer getPayType() {
        return payType == null ? -1 : payType;
    }
    public Long getExpectStartTime() {
        return expectStartTime == null ? 0 : expectStartTime;
    }
    public Long getUnloadTime() {
        return unloadTime == null ? 0 : unloadTime;
    }
    public String getLoadPhotoImg() {
        return StringUtils.isBlank(loadPhotoImg) ? "" : loadPhotoImg;
    }
    public String getUnloadPhotoImg() {
        return StringUtils.isBlank(unloadPhotoImg) ? "" : unloadPhotoImg;
    }
    public String getPlateNo() {
        return StringUtils.isBlank(plateNo) ? "" : plateNo;
    }
    public String getVin() {
        return StringUtils.isBlank(vin) ? "" : vin;
    }
    public String getLoadLinkName() {
        return StringUtils.isBlank(loadLinkName) ? "" : loadLinkName;
    }
    public String getLoadLinkPhone() {
        return StringUtils.isBlank(loadLinkPhone) ? "" : loadLinkPhone;
    }
    public String getUnloadLinkName() {
        return StringUtils.isBlank(unloadLinkName) ? "" : unloadLinkName;
    }
    public String getUnloadLinkPhone() {
        return StringUtils.isBlank(unloadLinkPhone) ? "" : unloadLinkPhone;
    }

    public boolean isHasLine() {
        if (lineId != null && lineId > 0) {
            hasLine = true;
        }
        return hasLine;
    }

}

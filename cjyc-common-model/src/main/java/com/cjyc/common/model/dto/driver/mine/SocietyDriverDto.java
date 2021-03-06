package com.cjyc.common.model.dto.driver.mine;

import com.cjyc.common.model.dto.driver.AppDriverDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Data
public class SocietyDriverDto extends AppDriverDto {
    private static final long serialVersionUID = 8361099293402284327L;

    @ApiModelProperty(value = "标志 0：认证 1：修改",required = true)
    @NotNull(message = "标志不能为空")
    private Integer flag;

    @ApiModelProperty(value = "司机姓名",required = true)
    @NotBlank(message = "司机姓名不能为空")
    private String realName;

    @ApiModelProperty(value = "司机手机号",required = true)
    @NotBlank(message = "司机手机号不能为空")
    private String phone;

    @ApiModelProperty(value = "身份证号",required = true)
    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @ApiModelProperty(value = "车辆id",required = true)
    private Long vehicleId;

    @ApiModelProperty(value = "车牌号",required = true)
    private String plateNo;

    @ApiModelProperty(value = "车位数",required = true)
    private Integer defaultCarryNum;

    @ApiModelProperty(value = "承运方式：2 : 代驾  3 : 干线   4：拖车",required = true)
    @NotNull(message = "承运方式不能为空")
    private Integer mode;

    @ApiModelProperty(value = "身份证正面",required = true)
    @NotBlank(message = "身份证正面不能为空")
    private String idCardFrontImg;
    @ApiModelProperty(value = "身份证反面",required = true)
    @NotBlank(message = "身份证反面不能为空")
    private String idCardBackImg;
    @ApiModelProperty("驾驶证")
    private String driverLicenceFrontImg;
    @ApiModelProperty("从业资格证")
    private String qualifiCertFrontImg;
    @ApiModelProperty("营运证")
    private String taxiLicenceFrontImg;
    @ApiModelProperty("行驶证")
    private String travelLicenceFrontImg;
}
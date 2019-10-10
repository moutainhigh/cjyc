package com.cjyc.common.model.dto;

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
 * 客户表（登录用户端APP用户）
 * </p>
 *
 * @author JPG
 * @since 2019-10-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("c_customer")
@ApiModel(value="CustomerDto对象", description="客户表（登录用户端APP用户）")
public class CustomerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @ApiModelProperty(value = "user_id(查询架构组数据时使用)")
    private Long userId;

    @ApiModelProperty(value = "客户名称")
    private String name;

    @ApiModelProperty(value = "别称")
    private String alias;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "联系人")
    private String contactMan;

    @ApiModelProperty(value = "客户地址")
    private String contactAddress;

    @ApiModelProperty(value = "客户性质")
    private String customerNature;

    @ApiModelProperty(value = "公司性质/规模")
    private String companyNature;

    @ApiModelProperty(value = "主营业务描述")
    private String majorBusDes;

    @ApiModelProperty(value = "首字母")
    private String initials;

    @ApiModelProperty(value = "头像")
    private String photoImg;

    @ApiModelProperty(value = "性别")
    private Integer sex;

    @ApiModelProperty(value = "身份证号")
    private String idCard;

    @ApiModelProperty(value = "身份证正面")
    private String idCardFrontImg;

    @ApiModelProperty(value = "身份证反面")
    private String idCardBackImg;

    @ApiModelProperty(value = "类型：1个人，2企业")
    private Integer type;

    @ApiModelProperty(value = "账号来源：1App注册，2Applet注册，3业务员创建，4企业管理员创建，5合伙人创建")
    private Integer source;

    @ApiModelProperty(value = "公司ID")
    private Long companyId;

    @ApiModelProperty(value = "状态：0未注册，2已注册，7已冻结")
    private Integer state;

    @ApiModelProperty(value = "注册时间，用户自己注册APP或者首次登陆操作APP时间")
    private Long registerTime;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "创建人")
    private Long createUserId;


}
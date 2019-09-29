package com.cjyc.common.model.util;

/**
 * 响应状态码枚举类
 * Created by leo on 2019/7/23.
 */
public enum ResultEnum {
    /* 通用 */
    SUCCESS(0, "成功"),
    //通用业务失败状态， 可以自定义返回消息msg 如：用户名或密码错误、提交失败等
    FAIL(1, "处理失败"),

    /* 移动端错误：1000-1999 */
    MOBILE_TOKEN_ILLEGAL(1000, "token校验失败"),
    MOBILE_TOKEN_TIMEOUT(1001, "token超时"),
    MOBILE_HTTP_ILLEGAL(1002, "非法请求"),
    MOBILE_HEAD_ERROR(1003, "Header参数错误"),
    MOBILE_PARAM_ERROR(1004, "接口请求参数错误"),
    MOBILE_SYSTEM_MAINTAIN(1005, "系统维护中"),
    MOBILE_NO_ACCESS(1006, "无访问权限"),
    MOBILE_VERRION_LOW(1007, "版本低，非强制更新"),
    MOBILE_VERRION_LOW_FORCE(1008, "版本太低，强制更新"),
    MOBILE_HTTP_OFTEN(1009, "请求太过频繁"),

    /* 系统错误：2001-2999*/
    API_INVOKE_ERROR(2001, "接口异常"),
    API_SYSTEM_BUSY(2002, "系统繁忙，请稍后重试"),
    API_FORBID_VISIT(2003, "该接口禁止访问"),
    API_ADDRESS_INVALID(2004, "接口地址无效"),
    API_REQUEST_TIMEOUT(2005, "接口请求超时");

    //状态码
    private Integer code;

    //消息内容
    private String msg;

    //构造方法
    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

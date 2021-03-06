package com.cjyc.common.model.enums.saleman;

/**
 * Saleman状态枚举
 * 状态：0待审核，2审核通过（在职），4已驳回，7已离职
 * @author JPG
 */
public enum SalemanStateEnum {

    /***/
    WAIT_CHECK("待审核",0),
    CHECKED("审核通过",2),
    CANCELED("取消审核", 4),
    REJECTED("已驳回", 7),
    LEAVE("已离职", 9);

    public String name;
    public int code;

    SalemanStateEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }
}

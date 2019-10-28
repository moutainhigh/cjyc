package com.cjyc.common.model.enums;

public enum UseStateEnum {

    DISABLED("不可用", 0),
    USABLE("可用", 1),
    BE_MODIFIED("可以修改",0),
    NOT_BE_MODIFIED("不可以修改",1);

    public String name;
    public int code;

    UseStateEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }
}

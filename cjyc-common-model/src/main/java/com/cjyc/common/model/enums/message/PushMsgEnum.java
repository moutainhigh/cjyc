package com.cjyc.common.model.enums.message;

import java.text.MessageFormat;

public enum PushMsgEnum {

    /***/
    C_COMMIT_ORDER("下单成功提醒", "您的订单：{0}已成功下单，稍后将由工作人员与您确认发运信息，请保持手机畅通，如有疑问请咨询客户服务热线：4009199266", 28),
    C_PAY_ORDER("待支付订单提醒", "您的订单：{0}已接单，请及时付款，如有疑问请咨询客户服务热线：4009199266", 29),
    C_PAID_ORDER("支付成功提醒", "您的订单：{0}已支付成功，如有疑问请咨询客户服务热线：4009199266", 30),
    C_PAID_CAR("车辆支付成功提醒", "您的车辆：{0}已支付成功，如有疑问请咨询客户服务热线：4009199266", 30),
    C_PILOT_PICK("上门提车提醒", "您的订单：{0}由韵车司机（{1}/{2}）提车中，请保持手机畅通，如有疑问请咨询客户服务热线：4009199266", 31),
    C_CONSIGN_PICK("上门提车提醒", "您的订单：{0}由韵车救援车({1}/{2}/{3})提车中，请保持手机畅通，如有疑问请咨询客户服务热线：4009199266", 32),
    C_SELF_PICK("提车自送提醒", "您的订单：{0}已审核通过，请及时送达{1}，地址：{2}，请保持手机畅通，如有疑问请咨询客户服务热线：4009199266", 33),
    C_TRANSPORT("开始运输提醒", "您的订单：您的订单：{0}已成功发运，正在{1}发往{2}，，如有疑问请咨询客户服务热线：4009199266", 34),
    C_SELF_BACK("送车自提提醒", "您的订单：{0}已抵达韵车物流{1}，请您及时前往{2}提车，联系人{4}/{5}，地址：{3}，如有疑问请咨询客户服务热线：4009199266", 35),
    C_RECEIPT_CAR("确认收车提醒", "您的车辆：{0}已确认收车，如有疑问请咨询客户服务热线：4009199266", 36),
    C_FINISHED("订单签收提醒", "您的订单：{0}已成功签收，感谢您选择韵车物流，如有疑问请咨询客户服务热线：4009199266", 37),

    D_NEW_WAYBILL("新运单提醒", "您有新运单：{0}有{1}台车需要运输，请您及时提车，详情登陆APP查看，如有疑问请咨询客户服务热线：4009199266", 38),
    D_LOAD("运单提车成功提醒", "您的运单：{0}有{1}等{2}台车已提车成功，请您根据约定时间进行物流运输，如有疑问请咨询客户服务热线：4009199266", 39),
    D_UNLOAD("运单车辆交付提醒", "您的运单：{0}有{1}台车已成功交车，如有疑问请咨询客户服务热线：4009199266", 40),
    D_PAID_CAR("车辆收款成功提醒", "车辆：{0}已收款成功，如有疑问请咨询客户服务热线：4009199266", 30),
    D_FINISHED("运单完成提醒", "您的运单：{0}已全部完成，感谢您选择韵车物流，如有疑问请咨询客户服务热线：4009199266", 41),

    S_NEW_ORDER("新订单提醒", "您有新的订单{0}，客户{1}/{2}请您于30分钟内与客户联系确认车辆信息及发运计划", 42),
    S_NEW_WAYBILL("新运单提醒", "您有新运单：{0}有{1}台车需要运输，请您及时提车，详情登陆APP查看，，如有疑问请咨询客户服务热线：4009199266", 43),
    S_LOAD("运单提车成功提醒", "您的运单：{0}有{1}等{2}台车已提车成功，请您根据约定时间进行物流运输，如有疑问请咨询客户服务热线：4009199266", 44),
    S_UNLOAD("运单车辆交付提醒", "您的运单：{0}有{1}台车已成功交车，如有疑问请咨询客户服务热线：4009199266", 45),
    S_OUT_STORE("出库任务提醒", "您有新的出库任务{0}，司机{1}/{2}，请您尽快安排出库", 46),
    S_IN_STORE("入库任务提醒", "您有新的任务{0}即将抵达韵车物流{1}，司机{2}/{3}请尽快联系司机安排入库", 49),
    S_PAID_CAR("车辆收款成功提醒", "车辆：{0}已收款成功，如有疑问请咨询客户服务热线：4009199266", 30),
    S_FINISHED("运单完成提醒", "您的运单：{0}已全部完成，感谢您选择韵车物流，如有疑问请咨询客户服务热线：4009199266", 50);

    private String title;
    private String msg;
    private long code;

    PushMsgEnum(String title, String msg, long code) {
        this.title = title;
        this.msg = msg;
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public String getMsg() {
        return msg;
    }

    public long getCode() {
        return code;
    }

    public String getMsg(String... args) {
        return MessageFormat.format(msg, args);
    }
}

package com.cjyc.common.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.cjyc.common.model.constant.AccountConstant;
import com.cjyc.common.model.constant.AmqpConstant;
import com.cjyc.common.model.entity.Order;
import com.cjyc.common.model.entity.OrderCar;
import com.cjyc.common.model.entity.defined.MQMessage;
import com.cjyc.common.model.util.MoneyUtil;
import com.cjyc.common.system.config.AmqpProperty;
import com.cjyc.common.system.service.ICsAmqpService;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class CsAmqpServiceImpl implements ICsAmqpService {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Async
    @Override
    public void sendOrderState(Order order) {
        if(order == null){
            return;
        }
        sendOrderState(Sets.newHashSet(order));
    }
    @Async
    @Override
    public void sendOrderState(Set<Order> orderSet) {
        if(CollectionUtils.isEmpty(orderSet)){
            return;
        }
        log.debug("【发送MQ消息】订单状态------------>" + JSON.toJSONString(orderSet));
        orderSet.forEach(o -> {
            Map<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("orderNo", o.getNo());
            dataMap.put("state", o.getState());
            dataMap.put("createTime", System.currentTimeMillis());
            if(AccountConstant.ACCOUNT_99CC.equals(o.getCustomerPhone())){
                log.info("【发送MQ消息】99车圈-订单状态------------> " + JSON.toJSONString(dataMap));
                send(AmqpProperty.topicExchange, AmqpProperty.commonRoutingKey, new MQMessage<>(AmqpConstant.MQ_TYPE_99CC_ORDER_STATE, dataMap));
            }else{
                log.debug("【发送MQ消息】订单状态------------>不需要发送状态推送" );
            }
        });
    }

    @Async
    @Override
    public void sendOrderConfirm(Order order, List<OrderCar> ocList) {
        if(order == null){
            return;
        }
        log.info("【发送MQ消息】确认订单------------> " + JSON.toJSONString(order));
        BigDecimal pickTotalFee = BigDecimal.ZERO;
        BigDecimal trunkTotalFee = BigDecimal.ZERO;
        BigDecimal backTotalFee = BigDecimal.ZERO;
        BigDecimal addInsuranceTotalFee = BigDecimal.ZERO;
        for (OrderCar orderCar : ocList) {
            pickTotalFee = pickTotalFee.add(MoneyUtil.nullToZero(orderCar.getPickFee()));
            trunkTotalFee = trunkTotalFee.add(MoneyUtil.nullToZero(orderCar.getTrunkFee()));
            backTotalFee = backTotalFee.add(MoneyUtil.nullToZero(orderCar.getBackFee()));
            addInsuranceTotalFee = addInsuranceTotalFee.add(MoneyUtil.nullToZero(orderCar.getAddInsuranceFee()));

        }
        Map<Object, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderNo", order.getNo()); // 订单编号
        dataMap.put("state", order.getState()); //状态
        dataMap.put("expectEndDate", order.getExpectEndDate()); // 预计到达日期
        dataMap.put("pickFee", MoneyUtil.fenToYuan(pickTotalFee)); //提车费
        dataMap.put("trunkFee",  MoneyUtil.fenToYuan(trunkTotalFee));//物流费
        dataMap.put("backFee",  MoneyUtil.fenToYuan(backTotalFee));//送车费
        dataMap.put("addInsuranceFee",  MoneyUtil.fenToYuan(addInsuranceTotalFee));//保险费
        dataMap.put("totalFee",  MoneyUtil.fenToYuan(order.getTotalFee()));//总费用
        dataMap.put("createTime", System.currentTimeMillis());//当前时间

        if(AccountConstant.ACCOUNT_99CC.equals(order.getCustomerPhone())){
            log.info("【发送MQ消息】99车圈-确认订单------------> " + JSON.toJSONString(dataMap));
            send(AmqpProperty.topicExchange, AmqpProperty.commonRoutingKey, new MQMessage<>(AmqpConstant.MQ_TYPE_99CC_ORDER_CONFIRM, dataMap));
        }
    }
    @Async
    @Override
    public void send(String exchange, String routingKey, Object message) {
        try {
            if(message == null){
                return;
            }
            //设置回调对象
            rabbitTemplate.setConfirmCallback(this);
            //构建回调返回的数据
            CorrelationData correlationData = new CorrelationData(String.valueOf(UUID.randomUUID()));
            rabbitTemplate.convertAndSend(exchange, routingKey, JSON.toJSONString(message), correlationData);
            log.info("【发送MQ消息】>>> 发送消息到RabbitMQ, 消息内容: " + message);
        } catch (AmqpException e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 消息回调确认方法
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean isSendSuccess, String s) {
        if(correlationData != null){
            log.info("【发送MQ消息】confirm回调方法>>>>>>>>>>>>>回调消息ID为: " + correlationData.getId());
        }
        s = s == null ? "" : s;
        if (isSendSuccess) {
            log.info("【发送MQ消息】confirm回调方法>>>>>>>>>>>>>消息发送成功" + s);
        } else {
            log.info("【发送MQ消息】confirm回调方法>>>>>>>>>>>>>消息发送失败" + s);
        }

    }
}

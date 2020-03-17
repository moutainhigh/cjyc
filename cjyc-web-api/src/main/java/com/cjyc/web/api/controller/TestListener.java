package com.cjyc.web.api.controller;

import com.alibaba.fastjson.JSON;
import com.cjyc.common.system.config.AmqpProperty;
import com.cjyc.common.system.service.ICsAdminService;
import com.cjyc.common.system.service.ICsAmqpService;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "rabbit")
@RequestMapping("/mq")
@RestController
@Slf4j
public class TestListener {

    @Resource
    private ICsAmqpService csAmqpService;

    /*@RabbitListener(bindings = {@QueueBinding(value = @Queue(value = "${cjkj.amqp.routing-key.common}"),
            exchange = @Exchange(value = "${cjkj.amqp.exchange.topic}"),
            key = "${cjkj.amqp.routing-key.common}")})
    public void testStateListener(String s) {
        log.info("消费信息----------------------》》》》》》" + s);

    }*/

    @GetMapping("/send")
    public String send(String s) {
        System.out.println("----------------------》》》》》》" + AmqpProperty.topicExchange);
        System.out.println("----------------------》》》》》》" + AmqpProperty.commonRoutingKey);
        System.out.println("----------------------》》》》》》" + AmqpProperty.enabled);
        Map<Object, Object> dataMap = Maps.newHashMap();
        dataMap.put("orderNo", "D12134567");
        dataMap.put("state", 1);
        dataMap.put("createTime", System.currentTimeMillis());

        Map<Object, Object> shellMap = Maps.newHashMap();
        shellMap.put("type", "99CC_order_state");
        shellMap.put("data", dataMap);

        csAmqpService.send(AmqpProperty.topicExchange, AmqpProperty.commonRoutingKey, shellMap);
        return "成功";
    }

}

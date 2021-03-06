package com.cjyc.customer.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 启动类
 * @author JPG
 * @date 2019/7/19 17:14
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients({"com.cjyc"})
@MapperScan("com.cjyc.common.model.dao")
@ComponentScan({"com.cjyc.customer.api", "com.cjyc.common.system","com.cjkj.common.redis"})
@EnableAsync
public class CustomerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerApiApplication.class, args);
    }
}

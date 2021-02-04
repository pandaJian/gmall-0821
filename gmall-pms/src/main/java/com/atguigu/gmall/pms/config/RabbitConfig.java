package com.atguigu.gmall.pms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author : panda Jian
 * @date : 2021-02-02 20:59
 * Description
 */
@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
            if (!ack){
                log.error("消息没有到达交换机。原因：" + cause);
            }
        });
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey) -> {
            log.error("消息没有到达队列。交换机：{}，路由键：{}，消息内容：{}",exchange,routingKey,new String(message.getBody()));
        });
    }
}

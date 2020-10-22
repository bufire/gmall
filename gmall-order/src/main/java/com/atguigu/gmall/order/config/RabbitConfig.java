package com.atguigu.gmall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class RabbitConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //创建初始化方法
    @PostConstruct
    public void init(){
        //设置回调方法确认消息是否到达交换机
        this.rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if(!ack){
                    log.error("消息没到到达交换机" + cause);
                }
            }
        });
        //确认消息
        this.rabbitTemplate.setReturnCallback(new ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.warn("消息没有到达队列,来自于交换机:{},路由键:{},消息内容:{}",exchange, routingKey, new String(message.getBody()));
            }
        });
    }

    //创建业务交换机
    @Bean
    public TopicExchange topicExchange(){
        return ExchangeBuilder.topicExchange("ORDER-EXCHANGE").durable(true).build();
    }

    //绑定
}

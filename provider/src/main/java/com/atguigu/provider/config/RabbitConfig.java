package com.atguigu.provider.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        // 确认消息是否到达交换机
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack){
                log.warn("消息没有到达交换机：" + cause);
            }
        });
        // 确认消息是否到达队列，到达队列该方法不执行
        this.rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.warn("消息没有到达队列，来自于交换机：{}，路由键：{}，消息内容：{}", exchange, routingKey, new String(message.getBody()));
        });
    }
    /**
     * 创建业务交换机
     */
    @Bean
    public TopicExchange topicExchange(){
//        return new TopicExchange("spring-test-exchange1",true,false,null);
        return ExchangeBuilder.topicExchange("spring-test-exchange").durable(true).build();
    }

    /**
     * 业务队列
     */
    @Bean
    public Queue queue(){
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","spring-dead-exchange");
        arguments.put("x-dead-letter-routing-key","msg.dead");
        return new Queue("spring-test-queue",true,false,false,arguments);
    }

    /**
     * 业务队列绑定到业务交换机
     */
    @Bean
    public Binding binding(TopicExchange topicExchange,Queue queue){
//        return new Binding("spring-test-queue1", DestinationType.QUEUE,"spring-test-exchange1","msg.work",null);
        return BindingBuilder.bind(queue).to(topicExchange).with("msg.work");
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean
    public TopicExchange deadExchange(){
//        return new TopicExchange("spring-test-exchange1",true,false,null);
        return ExchangeBuilder.topicExchange("spring-dead-exchange").durable(true).build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue deadQueue(){
        return QueueBuilder.durable("spring-dead-queue").build();
    }

    /**
     * 绑定信息队列到死信交换机
     */
    @Bean
    public Binding deadBinding(TopicExchange deadExchange,Queue deadQueue){
        return BindingBuilder.bind(deadQueue).to(deadExchange).with("msg.dead");
    }

}
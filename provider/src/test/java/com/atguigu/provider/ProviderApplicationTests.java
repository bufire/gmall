package com.atguigu.provider;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProviderApplicationTests {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
        this.rabbitTemplate.convertAndSend("spring-test-exchange", "msg.work", "hello spring rabbit!");
//        rabbitTemplate.convertAndSend("SPRING_RABBIT_EXCHANGE", "a.b", "hello spring rabbit!");
    }

}
class Person{

}

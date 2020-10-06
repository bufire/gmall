package com.atguigu.consumer.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConsumerListener {
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "SPRING_RABBIT_QUEUE", durable = "true"),
//            exchange = @Exchange(value = "SPRING_RABBIT_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
//            key = {"a.b"}
//        )
//    )
    @RabbitListener(queues = "spring-test-queue")
    public void listener(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println(msg);
            int i = 1/0;
            System.out.println("good good ");
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            // 是否已经重试过
            if (message.getMessageProperties().getRedelivered()){
                // 未重试过,重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                System.out.println("hi");
            } else {
                // 以重试过直接拒绝
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
                System.out.println("good");
            }
        }
    }
}

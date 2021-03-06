package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class CartListener {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient pmsClient;
    private static final String KEY_PREFIX = "cart:info";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order-cart-queue",durable = "true"),
            exchange = @Exchange(value = "order-exchange",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"cart:delete"}
    ))
    public void deleteCart(Map<String,Object> map, Channel channel, Message message) throws IOException {
        try {
            Long userId = (Long)map.get("userId");
            String skuIds = map.get("skuIds").toString();
            List<String> skuIdList = JSON.parseArray(skuIds, String.class);
            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
            hashOps.delete(skuIdList.toArray());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            if (message.getMessageProperties().getRedelivered()){
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
            }else{
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
            }
        }
    }
}

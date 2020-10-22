package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;
import net.bytebuddy.asm.Advice.Unused;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Data
public class OrderConfirmVo {

    // 收货地址列表
    private List<UserAddressEntity> addresses;

    // 送货清单，根据购物车页面传递过来的skuIds查询
    private List<OrderItemVo> items;

    // 用户的购物积分信息，ums_member表中的integration字段
    private Integer bounds;

    // 防重的唯一标识
    private String orderToken;
}
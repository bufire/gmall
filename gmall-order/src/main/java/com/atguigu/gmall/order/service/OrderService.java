package com.atguigu.gmall.order.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.exception.OrderException;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.order.vo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.wms.entity.SkuLockVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.jsonwebtoken.lang.Collections;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private GmallCartClient cartClient;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "order:token:";
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    public OrderConfirmVo confirm() {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 异步查询查询购物车中选中的商品
        CompletableFuture<List<Cart>> cartCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<List<Cart>> listResponseVo = this.cartClient.queryCheckedCarts(userId);
            List<Cart> carts = listResponseVo.getData();
            if (Collections.isEmpty(carts)) {
                throw new OrderException("购物车没有选中的商品");
            }
            return carts;
        }, threadPoolExecutor);
        CompletableFuture<Void> itemCompletableFuture = cartCompletableFuture.thenAcceptAsync((carts) -> {
            List<OrderItemVo> item = carts.stream().map(cart -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(cart.getSkuId());
                orderItemVo.setCount(cart.getCount().intValue());
                //根据skuId查询sku信息
                CompletableFuture<SkuEntity> skuEntityFuture = CompletableFuture.supplyAsync(() -> {
                    ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
                    SkuEntity skuEntity = skuEntityResponseVo.getData();
                    orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                    orderItemVo.setPrice(skuEntity.getPrice());
                    orderItemVo.setTitle(skuEntity.getTitle());
                    orderItemVo.setWeight(new BigDecimal(skuEntity.getWeight()));
                    return skuEntity;
                }, threadPoolExecutor);
                //查询销售信息
                CompletableFuture<Void> saleAttrsCompletableFuture = skuEntityFuture.thenAcceptAsync((skuEntity) -> {
                    ResponseVo<List<SkuAttrValueEntity>> listResponseVo = this.pmsClient.queryAttrValueBySku(cart.getSkuId(), skuEntity.getCategoryId());
                    List<SkuAttrValueEntity> skuAttrValueEntities = listResponseVo.getData();
                    orderItemVo.setSaleAttrs(skuAttrValueEntities);
                }, threadPoolExecutor);
                //根据Sku信息查询营销信息
                CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<ItemSaleVo>> listResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
                    List<ItemSaleVo> itemSaleVos = listResponseVo.getData();
                    orderItemVo.setSales(itemSaleVos);
                }, threadPoolExecutor);
                //根据sku查询库存信息
                CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
                    ResponseVo<List<WareSkuEntity>> listResponseVo = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
                    List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
                    if (!Collections.isEmpty(wareSkuEntities)) {
                        orderItemVo.setStore(wareSkuEntities.stream().allMatch((wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0)));
                    }
                }, threadPoolExecutor);

                CompletableFuture.allOf(skuEntityFuture, saleAttrsCompletableFuture, salesCompletableFuture, wareCompletableFuture).join();
                return orderItemVo;
            }).collect(Collectors.toList());
            confirmVo.setItems(item);
        }, threadPoolExecutor);
        //查询收获地址列表
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<UserAddressEntity>> listResponseVo = this.umsClient.queryAddressesByUserId(userId);
            List<UserAddressEntity> userAddressEntities = listResponseVo.getData();
            confirmVo.setAddresses(userAddressEntities);
        }, threadPoolExecutor);
        //查询用户的积分信息
        CompletableFuture<Void> boundCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUserById(userId);
            UserEntity userEntity = userEntityResponseVo.getData();
            if (userEntity != null) {
                confirmVo.setBounds(userEntity.getIntegration());
            }
        });
        //防重唯一标识
        CompletableFuture<Void> tokenCompletableFuture = CompletableFuture.runAsync(() -> {
            String timeId = IdWorker.getTimeId();
            this.redisTemplate.opsForValue().set(KEY_PREFIX + timeId, timeId);
            confirmVo.setOrderToken(timeId);
        });

        CompletableFuture.allOf(itemCompletableFuture,addressCompletableFuture,boundCompletableFuture,tokenCompletableFuture).join();
        return confirmVo;
    }

    public OrderEntity submit(OrderSubmitVo submitVo) {
        //防重
        String orderToken = submitVo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(KEY_PREFIX + orderToken), orderToken);
        if(!flag){
            throw new OrderException("已经提交啦,不要多次提交哦");
        }
        //验总价
        BigDecimal totalPrice = submitVo.getTotalPrice();   //获取订单中的总价格
        List<OrderItemVo> items = submitVo.getItems();  //获取订单信息
        if(Collections.isEmpty(items)){
            throw new OrderException("您的购物车中没有商品,请选择要购买的商品");
        }
        //1.1遍历订单信息获取商品sku与订购数量,then query totalprice from mysql
        BigDecimal currentBigDecimal = items.stream().map(item -> {
            Integer count = item.getCount();
            Long skuId = item.getSkuId();
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity != null) {
                return skuEntity.getPrice().multiply(new BigDecimal(count));
            }
            return new BigDecimal(0);
        }).reduce((a, b) -> a.add(b)).get();
        if(totalPrice.compareTo(currentBigDecimal) != 0){
            throw new OrderException("页面以过期,请稍后在试");
        }
        //验库存,并锁定库存
        //1.1获取库存锁定对象
        List<SkuLockVo> lockVos = items.stream().map((item) -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(item.getSkuId());
            skuLockVo.setCount(item.getCount());
            skuLockVo.setOrderToken(submitVo.getOrderToken());
            return skuLockVo;
        }).collect(Collectors.toList());
        //1.2调用远程接口锁定
        ResponseVo<List<SkuLockVo>> listResponseVo = this.wmsClient.checkAndLock(lockVos);
        List<SkuLockVo> skuLockVos = listResponseVo.getData();
        if(!Collections.isEmpty(skuLockVos)){
            throw new OrderException("诶呀,商品没有货啦,选购其他的吧" + JSON.toJSONString(skuLockVos));
        }
            //如果此时宕机商品会一直锁定
        //生成订单开始下单
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        OrderEntity orderEntity = null;
        try {
            ResponseVo<OrderEntity> orderEntityResponseVo = this.omsClient.saveOrder(submitVo, userId);
            orderEntity = orderEntityResponseVo.getData();
        } catch (Exception e) {
            e.printStackTrace();
            //如果发生错误立即释放库存,发送消息给库存服务器
            this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE", "stock.unlock", orderToken);
        }
        //删除购物车中的记录,异步发送用户id和商品sku给购物车服务器并删除
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId",userId);
        List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        map.put("skuIds",skuIds);
        this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","cart:delete",map);
        return orderEntity;
    }
}

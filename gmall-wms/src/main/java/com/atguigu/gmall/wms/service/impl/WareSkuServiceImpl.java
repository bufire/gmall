package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.entity.SkuLockVo;
import io.jsonwebtoken.lang.Collections;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {
    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "store:lock:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> lockVOS) {
        if(Collections.isEmpty(lockVOS)){
            return null;
        }
        lockVOS.forEach((lockVo) -> {
            //检验每一个商品的库存并锁库存
            this.checkLock(lockVo);
        });
        // 如果有一个商品锁定失败了，所有已经成功锁定的商品要解库存
        List<SkuLockVo> successLockVo = lockVOS.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
        List<SkuLockVo> errorLockVo = lockVOS.stream().filter(lockVo -> !lockVo.getLock()).collect(Collectors.toList());
        if(!Collections.isEmpty(errorLockVo)){
            successLockVo.forEach((lockVo) -> {
                this.wareSkuMapper.unlockStock(lockVo.getWareSkuId(),lockVo.getCount());
            });
            return lockVOS;
        }
        // 把库存的锁定信息保存到redis中，以方便将来解锁库存
        String orderToken = lockVOS.get(0).getOrderToken();
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(lockVOS));
        this.rabbitTemplate.convertAndSend("order-exchange","stock.ttl",orderToken);
        return null; // 如果都锁定成功，不需要展示锁定情况
    }

    private void checkLock(SkuLockVo skuLockVo){
        RLock fairLock = this.redissonClient.getFairLock("lock:" + skuLockVo.getLock());
        fairLock.lock();
        //验库存
        List<WareSkuEntity> wareSkuEntities = this.wareSkuMapper.checkStock(skuLockVo.getSkuId(),skuLockVo.getCount());
        if(Collections.isEmpty(wareSkuEntities)){
            skuLockVo.setLock(false);   //库存不足,锁定失败
            fairLock.unlock(); //返回程序之前释放锁
            return;
        }
        // 锁库存。一般会根据运输距离，就近调配。这里就锁定第一个仓库的库存
        if(this.wareSkuMapper.lockStock(wareSkuEntities.get(0).getId(),skuLockVo.getCount())==1){
            skuLockVo.setLock(true); //锁定成功
            skuLockVo.setWareSkuId(wareSkuEntities.get(0).getId());
        }
        fairLock.unlock();
    }

}
package com.atguigu.gmallindex.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmallindex.config.GmallCache;
import com.atguigu.gmallindex.feign.GmallPmsClient;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient pmsClient;
    public static final String KEY_PREFIX_ONE = "index:category:";
    public static final String KEY_PREFIX_TWO = "index:category:";

    public List<CategoryEntity> queryLvl1Categories() {
        String cacheCategories = redisTemplate.opsForValue().get(KEY_PREFIX_ONE  + 01);
        if (StringUtils.isNotBlank(cacheCategories)){
            // 如果缓存中有，直接返回
            System.out.println(cacheCategories);
            List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories, CategoryEntity.class);
            return categoryEntities;
        }
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategory(0l);
        // 把查询结果放入缓存
        this.redisTemplate.opsForValue().set(KEY_PREFIX_ONE  + 01, JSON.toJSONString(listResponseVo.getData()), 30, TimeUnit.DAYS);
        return listResponseVo.getData();
    }

    @GmallCache(prefix = "index:cates:", timeout = 14400, random = 3600, lock = "lock")
    public List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid) {
//        String cacheCategories = this.redisTemplate.opsForValue().get(KEY_PREFIX_TWO + pid);
//        if (StringUtils.isNotBlank(cacheCategories)){
//            // 如果缓存中有，直接返回
//            List<CategoryEntity> categoryEntities = JSON.parseArray(cacheCategories, CategoryEntity.class);
//            return categoryEntities;
//        }
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesWithSub(pid);
        // 把查询结果放入缓存
//        this.redisTemplate.opsForValue().set(KEY_PREFIX_TWO + pid, JSON.toJSONString(listResponseVo.getData()), 30, TimeUnit.DAYS);
        return listResponseVo.getData();
    }

//    public void testLock() {
//        String uuid = UUID.randomUUID().toString();
//        Boolean lock = this.tryLock("lock", uuid, 300l);
////        Boolean lock = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid,5,TimeUnit.SECONDS);
//        if(lock){
//            // 查询redis中的num值
//            String value = this.redisTemplate.opsForValue().get("num");
//            int num = Integer.parseInt(value);
//            // 把redis中的num值+1
//            this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
//
//            // 测试可重入性
//            this.testSubLock(uuid);
//            // 2. 释放锁 del
//            this.unlock("lock", uuid);
////            if(StringUtils.equals(uuid,this.redisTemplate.opsForValue().get("lock"))){
////                this.redisTemplate.delete("lock");
////            }
////            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
////            this.redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class), Arrays.asList("lock"), uuid);
//        }else{
//            try {
//                Thread.sleep(10);
//                this.testLock();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//    public void testLock() {
//        RLock lock = redissonClient.getLock("lock");
//        lock.lock(10,TimeUnit.SECONDS);
//        String num = this.redisTemplate.opsForValue().get("num");
//        int i = Integer.parseInt(num);
//        this.redisTemplate.opsForValue().set("num",String.valueOf(++i));
////        lock.unlock();
//    }
//
//    public String readLock() {
//        // 初始化读写锁
//        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readwriteLock");
//        RLock rLock = readWriteLock.readLock(); // 获取读锁
//
//        rLock.lock(10, TimeUnit.SECONDS); // 加10s锁
//
//        String msg = this.redisTemplate.opsForValue().get("msg");
//
//        //rLock.unlock(); // 解锁
//        return msg;
//    }
//
//    public String writeLock() {
//        // 初始化读写锁
//        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("readwriteLock");
//        RLock rLock = readWriteLock.writeLock(); // 获取写锁
//
//        rLock.lock(10, TimeUnit.SECONDS); // 加10s锁
//
//        this.redisTemplate.opsForValue().set("msg", UUID.randomUUID().toString());
//
//        //rLock.unlock(); // 解锁
//        return "成功写入了内容。。。。。。";
//    }
//
//    public String latch() {
//        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("countdown");
//        try {
//            countDownLatch.trySetCount(6);
//            countDownLatch.await();
//
//            return "关门了。。。。。";
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public String countDown() {
//        RCountDownLatch countDownLatch = this.redissonClient.getCountDownLatch("countdown");
//
//        countDownLatch.countDown();
//        return "出来了一个人。。。";
//    }
//
//    private Boolean tryLock(String lockName, String uuid, Long expire){
//        String script = "if (redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1) " +
//                "then" +
//                "    redis.call('hincrby', KEYS[1], ARGV[1], 1);" +
//                "    redis.call('expire', KEYS[1], ARGV[2]);" +
//                "    return 1;" +
//                "else" +
//                "   return 0;" +
//                "end";
//        if (!this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString())){
//            try {
//                // 没有获取到锁，重试
//                Thread.sleep(200);
//                tryLock(lockName, uuid, expire);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        // 锁续期
//        this.renewTime(lockName, expire);
//        // 获取到锁，返回true
//        return true;
//    }
//
//    private void unlock(String lockName, String uuid){
//        String script = "if (redis.call('hexists', KEYS[1], ARGV[1]) == 0) then" +
//                "    return nil;" +
//                "end;" +
//                "if (redis.call('hincrby', KEYS[1], ARGV[1], -1) > 0) then" +
//                "    return 0;" +
//                "else" +
//                "    redis.call('del', KEYS[1]);" +
//                "    return 1;" +
//                "end;";
//        // 这里之所以没有跟加锁一样使用 Boolean ,这是因为解锁 lua 脚本中，三个返回值含义如下：
//        // 1 代表解锁成功，锁被释放
//        // 0 代表可重入次数被减 1
//        // null 代表其他线程尝试解锁，解锁失败
//        Long result = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Lists.newArrayList(lockName), uuid);
//        // 如果未返回值，代表尝试解其他线程的锁
//        if (result == null) {
//            throw new IllegalMonitorStateException("attempt to unlock lock, not locked by lockName: "
//                    + lockName + " with request: "  + uuid);
//        }
//    }
//
//    // 测试可重入性
//    private void testSubLock(String uuid){
//        // 加锁
//        Boolean lock = this.tryLock("lock", uuid, 300l);
//
//        if (lock) {
//            System.out.println("分布式可重入锁。。。");
//
//            this.unlock("lock", uuid);
//        }
//    }
//
//    /**
//     * 锁延期
//     * 线程等待超时时间的2/3时间后,执行锁延时代码,直到业务逻辑执行完毕,因此在此过程中,其他线程无法获取到锁,保证了线程安全性
//     * @param lockName
//     * @param expire 单位：毫秒
//     */
//    private void renewTime(String lockName, Long expire){
//        String script = "if redis.call('exists', KEYS[1]) == 1 then return redis.call('expire', KEYS[1], ARGV[1]) else return 0 end";
//        new Thread(() -> {
//            while (this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Lists.newArrayList(lockName), expire.toString())){
//                try {
//                    // 到达过期时间的2/3时间，自动续期
//                    Thread.sleep(expire * 2 / 3);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
}

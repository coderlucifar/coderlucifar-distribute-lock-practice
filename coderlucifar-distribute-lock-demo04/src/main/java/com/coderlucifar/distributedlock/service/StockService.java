package com.coderlucifar.distributedlock.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sunyuan
 * @version 1.0
 * @description: stock service
 * @date 2022/9/3 15:25
 */
@Service
public class StockService {

//    @Resource
//    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private RedissonClient redissonClient;

    /**
     * 使用redisson分布式锁
     */
    public void deduct() {
        RLock lock = redissonClient.getLock("redissonLock");
        lock.lock();
        try {
            System.out.println("拿到锁执行业务逻辑");
//            String stock = this.stringRedisTemplate.opsForValue().get("stock");
//            if (stock != null && stock.length() != 0) {
//                Integer st = Integer.valueOf(stock);
//                if (st > 0) {
//                    this.stringRedisTemplate.opsForValue().set("stock", String.valueOf(--st));
//                }
//            }
        } finally {
            lock.unlock();
        }
    }

}

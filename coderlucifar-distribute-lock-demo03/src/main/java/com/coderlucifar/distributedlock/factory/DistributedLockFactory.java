package com.coderlucifar.distributedlock.factory;

import com.coderlucifar.distributedlock.lock.ReentrantRedisDistributedLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 分布式锁工厂类（单例）
 * @date 2022/9/2 12:24
 */
@Component
public class DistributedLockFactory {

    @Resource
    private StringRedisTemplate srt;

    /**
     * 服务唯一id
     */
    private final String uuid = UUID.randomUUID().toString();

    public Lock getReentrantRedisDistributedLock(String lockName) {
        return new ReentrantRedisDistributedLock(this.srt, lockName, this.uuid);
    }

}

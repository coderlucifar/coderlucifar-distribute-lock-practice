package com.coderlucifar.distributedlock.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 自定义Redis可重入分布式Redis锁
 * @date 2022/9/2 12:24
 */
public class ReentrantRedisDistributedLock implements Lock {

    private String uuid;
    private String lockName;
    private StringRedisTemplate stringRedisTemplate;

    public ReentrantRedisDistributedLock(StringRedisTemplate stringRedisTemplate, String lockName, String uuid) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockName = lockName;
        this.uuid = uuid + Thread.currentThread().getId();
    }

    @Override
    public void lock() {
        this.tryLock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        try {
            return tryLock(-1L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /**
     * 核心加锁方法
     *
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        // 默认过期时间
        long expire = 30;
        if (time != -1L) {
            // 设置锁过期时间
            expire = unit.toSeconds(time);
        }
        // lua脚本实现加可重入锁
        String script = "if redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1 then redis.call('hincrby', KEYS[1], ARGV[1], 1) redis.call('expire', KEYS[1], ARGV[2]) return 1 else return 0 end";
        // 自旋加锁
        while (!this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(this.lockName), this.uuid, String.valueOf(expire))) {
            Thread.sleep(10);
        }
        // 拿到锁后，启动定时任务给锁续期
        reSetExpire(expire);
        return true;
    }

    /**
     * 给锁续约的方法
     * 每过过期时间的三分之一就给锁续约。
     * 使用java.util.Timer实现定时任务。
     * 满足续约条件就递归调用，隔一段时间就续约，不满足条件就终止递归退出定时任务。
     */
    private void reSetExpire(long expire) {
        String script = "if redis.call('hexists', KEYS[1], ARGV[1]) == 1 then redis.call('expire', KEYS[1], ARGV[2]) return 1 else return 0 end";
        // 这是个一次性的任务，怎么让他定时触发，用递归
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, String.valueOf(expire))) {
                    reSetExpire(expire);
                }
            }
        }, expire * 1000 / 3);

    }

//    private String getId() {
//        return this.uuid + ":" + Thread.currentThread().getId();
//    }

    /**
     * 核心解锁方法
     */
    @Override
    public void unlock() {
        // lua 脚本中 elseif 要连着写， 不能像java中一样 else if 分开写
        String script =
                "if redis.call('hexists', KEYS[1], ARGV[1]) == 0 then return nil elseif redis.call('hincrby', KEYS[1], ARGV[1], -1) == 0 then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(this.lockName), this.uuid);
        if (execute == null) {
            throw new RuntimeException("this lock is not belong to you!, unlock() fail!");
        }
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}

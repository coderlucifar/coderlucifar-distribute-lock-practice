package com.coderlucifar.distributedlock.service;

import com.coderlucifar.distributedlock.factory.DistributedLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 库存服务
 * @date 2022/8/31 22:55
 */
@Service
public class StockService {

    private final Logger log = LoggerFactory.getLogger(StockService.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DistributedLockFactory distributedLockFactory;

    /**
     * 使用redis扣减库存
     * 不加锁
     */
    public void deduct() {

        String stock = this.stringRedisTemplate.opsForValue().get("stock");

        if (stock != null && stock.length() != 0) {
            Integer st = Integer.valueOf(stock);
            if (st > 0) {
                this.stringRedisTemplate.opsForValue().set("stock", String.valueOf(--st));
            }
        }
    }

    /**
     * 使用redis乐观锁玩儿法
     *
     */
    public void deduct01() {
        this.stringRedisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.watch("stock");

                String stock = operations.opsForValue().get("stock").toString();
                if (stock != null && stock.length() != 0) {
                    Integer st = Integer.valueOf(stock);
                    if (st > 0) {
                        operations.multi();
                        operations.opsForValue().set("stock", String.valueOf(--st));
                        List res = operations.exec();
                        if (res == null || res.size() == 0) {
                            try {
                                Thread.sleep(40);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            deduct01();
                        }
                        return res;
                    }
                }
                return null;
            }
        });
    }

    /**
     *
     *    问题一：拿到锁之后宕机然后没释放锁，造成死锁！如何解决？给锁设置过期时间(注意原子性问题).
     *    问题二：如何保证验证是否是自己加的锁与删除锁这两个操作的原子性？使用lua脚本让多条redis命令的执行具有原子性！要么都成功，要么都失败。
     *    问题三：自己的锁被别人删了！如何防误删？
     *    问题四：不可重入导致死锁！如何实现可重入？
     *    问题五：锁过期了，但是业务还没执行完！如何解决？给锁续命！如何续命？
     *
     *    注意：一定要注意原子性问题！有些操作如果不是原子操作，就会有并发安全问题。
     *    1、加锁和给锁设置过期时间这两个操作的原子性，通过redis指令，redis单条指令天生具有原子性。
     *    2、检查锁是不是自己的和释放锁这两个操作的原子性，没有对应的redis指令，只能通过lua脚本来实现。
     *
     *    使用分布式锁version01：
     *      解决问题一、二、三
     *
     */
    public void deduct02() {
        /**
         * 唯一id防止锁误删
         */
        final String uuid = UUID.randomUUID().toString();
        /**
         * 自旋获取锁(设置key值)
         *      设置唯一id为value
         *      设置锁过期时间
         */
        while (!this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        /**
         * 获取锁之后执行业务操作
         */
        try {
            String stock = this.stringRedisTemplate.opsForValue().get("stock");
            if (stock != null && stock.length() != 0) {
                Integer st = Integer.valueOf(stock);
                if (st > 0) {
                    this.stringRedisTemplate.opsForValue().set("stock", String.valueOf(--st));
                }
            }
        } finally {
            /**
             * 1、判断是否是自己加的锁
             * 2、解锁
             * 注：这两步操作必须要保证原子性，用lua脚本保证
             */
//            if (uuid.equals(stringRedisTemplate.opsForValue().get("lock"))) {
//                this.stringRedisTemplate.delete("lock");
//            }
            // 1、写lua脚本
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]) return 1 else return 0 end";
            // 2、创建redis lua脚本对象
            DefaultRedisScript<Boolean> defaultRedisScript = new DefaultRedisScript<>(script, Boolean.class);
            // 3、执行
            stringRedisTemplate.execute(defaultRedisScript, Arrays.asList("lock"), uuid);
        }
    }

    /**
     * 将锁改造为可重入锁
     *
     */
    public void deduct03() {

        /**
         * 加可重入锁！
         */
        Lock mylock = distributedLockFactory.getReentrantRedisDistributedLock("mylock");
        try {
            mylock.tryLock(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        /**
         * 获取锁之后执行业务操作
         */
        try {
            String stock = this.stringRedisTemplate.opsForValue().get("stock");
            if (stock != null && stock.length() != 0) {
                Integer st = Integer.valueOf(stock);
                if (st > 0) {
                    this.stringRedisTemplate.opsForValue().set("stock", String.valueOf(--st));
                }
            }
//            try {
//                // 为了演示锁续约的效果
//                Thread.sleep(100000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        } finally {
            /**
             * 1、判断是否是自己加的锁
             * 2、解锁
             * 注：这两步操作必须要保证原子性，用lua脚本保证
             */
            mylock.unlock();
        }
    }



}

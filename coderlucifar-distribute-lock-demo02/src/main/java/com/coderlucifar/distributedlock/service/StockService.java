package com.coderlucifar.distributedlock.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.coderlucifar.distributedlock.mapper.StockMapper;
import com.coderlucifar.distributedlock.pojo.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 库存服务
 * @date 2022/8/30 17:00
 */
@Service
//@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)     // 多例模式导致锁失效
public class StockService {

    private final Logger log = LoggerFactory.getLogger(StockService.class);

    @Resource
    private StockMapper stockMapper;

    private ReentrantLock lock = new ReentrantLock();


    /**
     * 方法一：使用JVM层面提供的锁
     *   缺点：
     *      集群部署下无法保证不超卖；
     *      吞吐量较低。
     */
    //    @Transactional  // 事务导致锁失效，mysql默认隔离级别是 可重复读
//    @Transactional(isolation = Isolation.READ_UNCOMMITTED)  // 尝试设置隔离级别为读未提交是否可以解决，但是真实环境怎么可能设置为读未提交
    public void deduct() {
        lock.lock();
        try {
            Stock stock = this.stockMapper.selectOne(new QueryWrapper<Stock>().eq("product_code", "1001"));
            if (stock != null && stock.getCount() > 0) {
                stock.setCount(stock.getCount() - 1);
                this.stockMapper.updateById(stock);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     *  方法二：一个sql语句，更新数量时判断，只锁要更新的那行记录，注意 product_code 必须加索引，并且判断条件必须是确切值，不能是模糊条件
     *      update db_stock set count = count - #{count} where product_code = #{productCode} and count >= #{count}
     *
     *      优点：
     *          这种写法可以保证
     *          多例模式、事务、集群等情景下的并发安全问题
     *          并且吞吐量比使用JVM层面的锁提高了吞吐量
     *
     *       缺点：
     *          1、需要注意不要误用表级锁！
     *          2、无法解决多个库存记录的问题！
     *          3、无法记录库存前后变化的状态
     *
     */
    @Transactional
    public void deduct01() {
        this.stockMapper.updateStock("1001", 1);
    }

    /**
     *  方法三：mysql层面使用了悲观锁， 即 select ... for update;
     *
     *      优点：
     *          1、解决了有多条库存记录的问题，也可以记录库存变化前后的状态。
     *          2、比JVM层面的锁吞吐量高。
     *      缺点：
     *          1、性能问题。
     *          2、死锁问题：对多条数据加锁时要保证顺序一致。
     *          3、库存操作要统一：相关库存操作全部要使用 select... for update
     *
     *
     */
    @Transactional
    public void deduct02() {

        // step1：查出库存信息，加行锁
        List<Stock> stocks = this.stockMapper.queryStock("1001");
        // 这里我们重点在学习分布式锁，所以取第一个库存进行扣减，在实际业务中，这里会经过复杂的分析，决定扣减哪个库存
        Stock stock = stocks.get(0);
        // step2: 判断库存是否充足
        if (stock != null && stock.getCount() > 0) {
            // step3: 扣减库存
            stock.setCount(stock.getCount() - 1);
            this.stockMapper.updateById(stock);
        }

    }

    /**
     * 方法四：使用乐观锁！ 利用CAS思想！Compare And Set。
     * 如何避免ABA问题？ 使用版本号或时间戳机制来保证！下面我们使用版本号机制来实现！
     *
     *      缺点：
     *          1、高并发情况下性能低下！反复重试，浪费CPU资源！
     *          2、ABA问题需要使用版本号机制解决！
     *          3、读写分离情况下导致乐观锁不可靠！
     *          主从之间数据同步有延迟，导致在从库中读到的数据可能是主库还没同步过去的旧数据，
     *          这时候由于版本号机制在主库更新数据就会不成功！
     *
     */
//    @Transactional
    public void deduct03() {
        List<Stock> stocks = this.stockMapper.selectList(new QueryWrapper<Stock>().eq("product_code", "1001"));
        Stock stock = stocks.get(0);
        if (stock != null && stock.getCount() > 0) {
            final Integer oldVersion = stock.getVersion();
            stock.setCount(stock.getCount() - 1);
            stock.setVersion(stock.getVersion() + 1);
            int update = this.stockMapper.update(stock, new UpdateWrapper<Stock>().eq("id", stock.getId()).eq("version", oldVersion));
            if (update < 0) {
                // 如果更新失败，递归重试（江湖人称-自旋）
                // 注意，如果一直自旋，1、会导致StackOverFlow栈溢出  2、连接mysql超时
                // 如何解决栈溢出, 让当前线程睡一会儿，让别人先执行，降低冲突。
                // 如果解决连接mysql超时，将事务注解取消，是因为事务是长链接么？ mqsql增删改操作都会加锁
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                deduct();
            }
        }

    }

}

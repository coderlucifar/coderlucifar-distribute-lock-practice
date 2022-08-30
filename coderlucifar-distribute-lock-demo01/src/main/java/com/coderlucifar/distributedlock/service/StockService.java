package com.coderlucifar.distributedlock.service;

import com.coderlucifar.distributedlock.pojo.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 库存服务
 * @date 2022/8/30 17:00
 */
@Service
public class StockService {

    private final Logger log = LoggerFactory.getLogger(StockService.class);

    private Stock stock = new Stock();

    /**
     * 减库存,
     * 线程不安全
     */
    public void deductNotSafe() {
        stock.setStock(stock.getStock() - 1);
        log.info("库存余量为：" + stock.getStock());
    }

    /**
     * 加synchronized保证线程安全
     */
    public synchronized void deductSafeWithSynchronized() {
        stock.setStock(stock.getStock() - 1);
        log.info("库存余量为：" + stock.getStock());
    }

    /**
     * 使用ReentrantLock保证线程安全
     */
    public void deductSafeWithReentrantLock() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            stock.setStock(stock.getStock() - 1);
            log.info("库存余量为：" + stock.getStock());
        } finally {
            lock.unlock();
        }
    }




}

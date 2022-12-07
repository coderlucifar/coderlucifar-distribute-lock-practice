package com.coderlucifar.distributedlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 使用redis实现分布式锁启动类
 * @date 2022/8/31 01:09
 */
@SpringBootApplication
public class DistributedLockApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistributedLockApplication.class, args);
    }
}

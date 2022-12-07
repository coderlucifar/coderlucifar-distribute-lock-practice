package com.coderlucifar.distributedlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 启动类
 * @date 2022/9/3 15:03
 */
@SpringBootApplication
public class DistributedLockApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistributedLockApplication.class, args);
    }
}

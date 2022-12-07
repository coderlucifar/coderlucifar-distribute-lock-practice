package com.coderlucifar.distributedlock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author sunyuan
 * @version 1.0
 * @description: redisson配置类
 * @date 2022/9/3 15:16
 */
@Configuration
public class RedissonConfig {

    /**
     * redis哨兵模式节点配置
     * @return
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSentinelServers()
                .setMasterName("monitor-man")
                .setCheckSentinelsList(false)
                .addSentinelAddress("redis://192.168.2.102:26379");

        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

//    /**
//     * redis单节点模式配置
//     * @return
//     */
//    @Bean
//    public RedissonClient redissonClient() {
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("redis://192.168.2.102:6380");
//        return Redisson.create(config);
//    }



}

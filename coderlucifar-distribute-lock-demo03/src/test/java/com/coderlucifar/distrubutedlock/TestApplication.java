package com.coderlucifar.distrubutedlock;

import com.coderlucifar.distributedlock.DistributedLockApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author sunyuan
 * @version 1.0
 * @description: TODO
 * @date 2022/8/31 23:02
 */
@SpringBootTest(classes = DistributedLockApplication.class)
@RunWith(SpringRunner.class)
public class TestApplication {

    @Resource
    private StringRedisTemplate srt;

    @Test
    public void setStock() {
        this.srt.opsForValue().set("stock", String.valueOf(5000));
    }

    @Test
    public void getStock() {
        String stock = this.srt.opsForValue().get("stock");
        System.out.println(stock);
    }

    @Test
    public void delStock() {
        this.srt.delete("stock");
    }

}

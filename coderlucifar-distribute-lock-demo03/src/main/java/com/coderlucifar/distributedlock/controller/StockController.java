package com.coderlucifar.distributedlock.controller;

import com.coderlucifar.distributedlock.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 库存controller
 * @date 2022/8/31 22:57
 */
@RestController
public class StockController {

    @Resource
    private StockService stockService;

    @GetMapping("/stock/deduct")
    public String deduct() {
        stockService.deduct();
        return "deduct success!";
    }

    @GetMapping("/stock/deduct01")
    public String deduct01() {
        stockService.deduct01();
        return "deduct01 success!";
    }

    @GetMapping("/stock/deduct02")
    public String deduct02() {
        stockService.deduct02();
        return "deduct02 success!";
    }

    @GetMapping("/stock/deduct03")
    public String deduct03() {
        stockService.deduct03();
        return "deduct03 success!";
    }

}

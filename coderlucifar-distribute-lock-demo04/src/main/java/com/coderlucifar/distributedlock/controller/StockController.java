package com.coderlucifar.distributedlock.controller;

import com.coderlucifar.distributedlock.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author sunyuan
 * @version 1.0
 * @description: controller
 * @date 2022/9/3 15:25
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


}

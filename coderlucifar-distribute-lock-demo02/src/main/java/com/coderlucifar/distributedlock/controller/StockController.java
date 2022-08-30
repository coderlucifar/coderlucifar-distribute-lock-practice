package com.coderlucifar.distributedlock.controller;

import com.coderlucifar.distributedlock.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author sunyuan
 * @version 1.0
 * @description:
 * @date 2022/8/30 17:01
 */
@RestController
public class StockController {

    @Resource
    private StockService stockService;

    @GetMapping("/stock/deduct")
    public String deduct() {
        this.stockService.deduct();
        return "success";
    }

}

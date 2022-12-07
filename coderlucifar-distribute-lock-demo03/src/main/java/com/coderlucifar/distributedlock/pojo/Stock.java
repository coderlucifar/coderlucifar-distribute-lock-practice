package com.coderlucifar.distributedlock.pojo;

import lombok.Data;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 库存
 * @date 2022/8/31 22:53
 */
@Data
public class Stock {

    private Long id;
    private String productCode;
    private String warehouse;
    private Integer count;
    private Integer version;

}

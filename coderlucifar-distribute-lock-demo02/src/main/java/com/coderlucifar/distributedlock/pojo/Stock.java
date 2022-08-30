package com.coderlucifar.distributedlock.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 库存
 * @date 2022/8/30 16:59
 */
@Data
@TableName("db_stock")
public class Stock {
    // 库存记录id
    private Long id;
    // 产品编码
    private String productCode;
    // 产品仓库
    private String warehouse;
    // 库存
    private Integer count;
    // 版本号，为了防止ABA问题
    private Integer version;

    public Stock() {

    }

}

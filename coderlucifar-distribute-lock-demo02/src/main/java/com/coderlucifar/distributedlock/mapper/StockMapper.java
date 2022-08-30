package com.coderlucifar.distributedlock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coderlucifar.distributedlock.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author sunyuan
 * @version 1.0
 * @description: 库存Mapper
 * @date 2022/8/30 20:45
 */
public interface StockMapper extends BaseMapper<Stock> {

    @Update("update db_stock set count = count - #{count} where product_code = #{productCode} and count >= #{count}")
    int updateStock(@Param("productCode") String productCode, @Param("count") Integer count);

    // 查出来之后加了行锁
    @Select("select * from db_stock where product_code = #{productCode} for update")
    List<Stock> queryStock(@Param("productCode") String productCode);


}

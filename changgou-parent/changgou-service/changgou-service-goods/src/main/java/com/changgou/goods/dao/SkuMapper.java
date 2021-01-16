package com.changgou.goods.dao;
import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:itheima
 * @Description:Sku的Dao
 *****/
public interface SkuMapper extends Mapper<Sku> {
    //扣减库存
    @Update("update tb_sku " +
            "set num = num - #{num},sale_num = sale_num + #{num} " +
            "where id = #{id} and num >= #{num}")
    int decrSkuNum(@Param("id") String id, @Param("num") Integer num);

}

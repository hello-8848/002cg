package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:itheima
 * @Description:Brand的Dao
 *****/
public interface BrandMapper extends Mapper<Brand> {
    /**
     * 根据分类id查询对应的品牌信息
     *
     * @param cid :
     * @return : java.util.List<com.changgou.goods.pojo.Brand>
     */
    @Select("select *from tb_brand b,tb_category_brand cb where b.id=cb.brand_id and category_id=#{cid}")
    List<Brand> findByCategoryId(@Param(value = "cid") Integer cid);
}

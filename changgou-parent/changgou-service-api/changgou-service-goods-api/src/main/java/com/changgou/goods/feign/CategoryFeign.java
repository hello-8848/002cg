package com.changgou.goods.feign;

import com.changgou.goods.pojo.Category;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Auther lxy
 * @Date
 */
@FeignClient(name = "goods")
@RequestMapping(value = "/category")
public interface CategoryFeign {
    /**
    *根据id查询分类信息
     * @param id :
     * @return : com.changgou.util.Result<com.changgou.goods.pojo.Category>
     */
    @GetMapping("/{id}")
    public Result<Category> findById(@PathVariable(value = "id") Integer id);
}

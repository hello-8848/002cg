package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {
    /**
     * 根据状态查询sku
     *
     * @param status :
     * @return : com.changgou.util.Result<java.util.List<com.changgou.goods.pojo.Sku>>
     */
    @GetMapping(value = "/status/{status}")
    public Result<List<Sku>> findByStatus(@PathVariable(value = "status") String status);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable(value = "id") String id);

    /**
     * 下单后扣减库存
     *
     * @param decrData :
     * @return : com.changgou.util.Result
     */
    @GetMapping(value = "/decr/count")
    public Result decrSkuNum(Map<String, Object> decrData);
}

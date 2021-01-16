package com.changgou.goods.feign;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Auther lxy
 * @Date
 */
@FeignClient(name = "goods")
@RequestMapping(value = "/spu")
public interface SpuFeign {
    /**
     * 根据spuId查询商品信息回显
     *
     * @param spid :
     * @return : com.changgou.util.Result<com.changgou.goods.pojo.Goods>
     */
    @GetMapping(value = "/goods/{spid}")
    public Result<Goods> findBySpuId(@PathVariable(value = "spid") String spid);

    /***
     * 查询Spu全部数据
     * @return
     */
    @GetMapping
    public Result<List<Spu>> findAll();

    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable(value = "id") String id);
}

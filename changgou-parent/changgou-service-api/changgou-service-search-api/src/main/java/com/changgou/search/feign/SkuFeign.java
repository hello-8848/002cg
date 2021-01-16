package com.changgou.search.feign;

import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@FeignClient(name = "search")
@RequestMapping(value = "/search")
public interface SkuFeign {
    /**
     * 条件搜索商品
     *
     * @param searchMap :
     * @return : com.changgou.util.Result<java.util.Map<java.lang.String,java.lang.Object>>
     */
    @GetMapping
    public Result<Map<String, Object>> search(@RequestParam(required = false) Map<String, String> searchMap);
}

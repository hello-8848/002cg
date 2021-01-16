package com.changgou.content.feign;

import com.changgou.content.pojo.Content;
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
@FeignClient(value = "content")
@RequestMapping(value = "/content")
public interface ContentFeign {
    /**
     * 根据分类id查询广告信息
     *
     * @param cid :
     * @return : com.changgou.util.Result<java.util.List<com.changgou.content.pojo.Content>>
     */
    @GetMapping(value = "/list/category/{cid}")
    public Result<List<Content>> findByCategoryId(@PathVariable(value = "cid") Long cid);

}

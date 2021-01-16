package com.changgou.controller;

import com.changgou.service.ItemService;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther lxy
 * @Date
 */
@RestController
@RequestMapping(value = "/page")
@CrossOrigin
public class ItemController {
    @Autowired
    private ItemService itemService;
/**
*根据spuid生成一个静态页面
 * @param spid :
 * @return : com.changgou.util.Result
 */
    @GetMapping(value = "/createHtml/{spid}")
    public Result createOne(@PathVariable(value = "spid") String spid) {
        itemService.createPageHtml(spid);
        return new Result(true, StatusCode.OK, "根据spuid生成一个静态页面成功");
    }
/**
*生成所有静态页面
 * @return : com.changgou.util.Result
 */
    @GetMapping
    public Result createAll() {
        itemService.createAllPageHtml();
        return new Result(true, StatusCode.OK, "生成所有静态页面成功");
    }
}

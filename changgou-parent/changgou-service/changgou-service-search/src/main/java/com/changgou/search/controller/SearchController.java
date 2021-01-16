package com.changgou.search.controller;

import com.changgou.search.service.EsSkuService;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SearchController {
    @Autowired
    private EsSkuService esSkuService;
/**
*导入数据到Es
 * @return : com.changgou.util.Result
 */
    @GetMapping(value = "/import")
    public Result importData2Es() {
        esSkuService.importData2Es();
        return new Result(true, StatusCode.OK, "导入数据到索引库成功");
    }
/**
*条件搜索商品
 * @param searchMap :
 * @return : com.changgou.util.Result<java.util.Map<java.lang.String,java.lang.Object>>
 */
    @GetMapping
    public Result<Map<String, Object>> search(@RequestParam(required = false) Map<String, String> searchMap) {
        Map<String, Object> search = esSkuService.search(searchMap);

        return new Result<>(true, StatusCode.OK, "条件搜索商品成功", search);
    }
}

package com.changgou.searchWeb.controller;

import com.changgou.exception.ChanggouException;
import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.util.Page;
import com.changgou.util.Result;
import com.changgou.util.UrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@Controller
@RequestMapping(value = "/search")
@CrossOrigin
public class SearchController {
    @Autowired
    private SkuFeign skuFeign;

    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {

        //调用search微服务
        Result<Map<String, Object>> result = skuFeign.search(searchMap);
        if (!result.isFlag()) {
            throw new ChanggouException("搜索商品失败");
        }
        Map<String, Object> searchResult = result.getData();
        model.addAttribute("result", searchResult);
        //搜索条件回显
        model.addAttribute("searchMap", searchMap);
        //获取搜索的url
        String url = getUrl(searchMap);
        model.addAttribute("url", UrlUtils.replateUrlParameter(url,"pageNum"));
        //去掉排序域或排序类型的地址
        model.addAttribute("sortUrl", UrlUtils.replateUrlParameter(url, "sortRule", "sortField","pageNum"));
        //获得分页信息
        int totalElements = Integer.parseInt(searchResult.get("totalElements").toString());
        int pageNumber = Integer.parseInt(searchResult.get("pageNumber").toString());
        int pageSize = Integer.parseInt(searchResult.get("pageSize").toString());
        //获取分页对象
        Page<SkuInfo> page = new Page<>(totalElements, pageNumber, pageSize);
        model.addAttribute("page", page);
        return "search";
    }

    private String getUrl(Map<String, String> searchMap) {
        String url = "/search/list?";
        //循环遍历条件
        for (Map.Entry<String, String> entry : searchMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            url += key + "=" + value + "&";
        }
        return url.substring(0,url.length() - 1);
    }
}

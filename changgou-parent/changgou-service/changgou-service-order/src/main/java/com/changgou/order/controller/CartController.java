package com.changgou.order.controller;

import com.changgou.order.config.TokenDecode;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@RestController
@RequestMapping(value = "/cart")
@CrossOrigin
public class CartController {
    @Autowired
    private CartService cartService;
/**
*加载购物车数据
 * @param ids :
 * @return : com.changgou.util.Result<java.util.List<com.changgou.order.pojo.OrderItem>>
 */
    @GetMapping(value = "/list/choose")
    public Result<List<OrderItem>> choose(@RequestParam("ids") String[] ids) {
        //从cookie中获取用户名
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        //查询
        List<OrderItem> orderItems = cartService.choose(username, ids);
        return new Result<>(true, StatusCode.OK, "加载购物车数据成功", orderItems);
    }
/**
*加入商品到购物车
 * @param num :
 * @param skuId :
 * @return : com.changgou.util.Result
 */
    @RequestMapping(value = "/add")
    public Result add(Integer num,@RequestParam("id") String skuId) {
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        cartService.add(num, skuId, userInfo.get("username"));
        return new Result(true, StatusCode.OK, "商品加入购物车成功");
    }
/**
*根据用户名加载购物车列表
 * @return : com.changgou.util.Result<java.util.List<com.changgou.order.pojo.OrderItem>>
 */
    @GetMapping(value = "/list")
    public Result<List<OrderItem>> list() {
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        List<OrderItem> orderItems = cartService.findCart(userInfo.get("username"));
        return new Result<>(true, StatusCode.OK, "加载购物车成功", orderItems);
    }
}

package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

/**
 * @Auther lxy
 * @Date
 */
public interface CartService {
/**
*查询购物车信息
 * @param username :
 * @param ids : skuId
 * @return : java.util.List<com.changgou.order.pojo.OrderItem>
 */
    List<OrderItem> choose(String username, String[] ids);
/**
*添加购物车
 * @param num :
 * @param skuId :
 * @param username :
 * @return : void
 */
    void add(Integer num, String skuId, String username);
/**
*加载购物车数据
 * @param username :
 * @return : java.util.List<com.changgou.order.pojo.OrderItem>
 */
    List<OrderItem> findCart(String username);
}

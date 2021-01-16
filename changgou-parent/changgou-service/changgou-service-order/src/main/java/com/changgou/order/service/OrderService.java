package com.changgou.order.service;

import com.changgou.order.pojo.Order;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * @Author:itheima
 * @Description:Order业务层接口
 *****/
public interface OrderService {
/**
*查询未支付订单
 * @return : java.util.List<java.lang.String>
 */
    List<Order> queryByStatus(String status);
/**
*支付失败,逻辑删除订单
 * @param orderId :
 * @return : void
 */
    void deleteOrder(String orderId);
    /**
     * 支付成功,修改订单状态
     *
     * @param orderId       :
     * @param transactionid :
     * @return : void
     */
    void updateStatus(String orderId, String transactionid);

    /***
     * Order多条件分页查询
     * @param order
     * @param page
     * @param size
     * @return
     */
    PageInfo<Order> findPage(Order order, int page, int size);

    /***
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Order> findPage(int page, int size);

    /***
     * Order多条件搜索方法
     * @param order
     * @return
     */
    List<Order> findList(Order order);

    /***
     * 删除Order
     * @param id
     */
    void delete(String id);

    /***
     * 修改Order数据
     * @param order
     */
    void update(Order order);

    /***
     * 新增Order
     * @param order
     */
    Order add(Order order);

    /**
     * 根据ID查询Order
     *
     * @param id
     * @return
     */
    Order findById(String id);

    /***
     * 查询所有Order
     * @return
     */
    List<Order> findAll();
}

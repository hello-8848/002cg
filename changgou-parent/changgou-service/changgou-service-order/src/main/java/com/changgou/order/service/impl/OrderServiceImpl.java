package com.changgou.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.exception.ChanggouException;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import com.changgou.user.feign.UserFeign;
import com.changgou.util.IdWorker;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/****
 * @Author:itheima
 * @Description:Order业务层接口实现类
 *****/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private UserFeign userFeign;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 增加Order
     * @param order
     */
    @Override
    public Order add(Order order){
        //补全订单缺少的信息
        order.setId("Order"+idWorker.nextId());
        //定义数据模型接收操作订单的数据,用于扣减库存
        Map<String, Object> map = new HashMap<>();
        //补全订单详情信息
        Integer totalMoney = 0;//商品总价格
        Integer totalNum = 0;//商品总数量
        //获取用户名
        String username = order.getUsername();
        //获取选中商品ids
        String[] ids = order.getIds();
        //遍历ids,设置订单详情数据
        for (String id : ids) {
            //根据id查询购物车中对应商品
            OrderItem orderItem=(OrderItem) redisTemplate.boundHashOps("Cart_" + username).get(id);
            //计算总价格
            totalMoney += orderItem.getMoney();
            //计算总数量
            totalNum += orderItem.getNum();
            //设置订单详情id
            orderItem.setId("OrderItem" + idWorker.nextId());
            //设置订单详情关联订单id
            orderItem.setOrderId(order.getId());
            //保存单个sku订单详情数据
            orderItemMapper.insertSelective(orderItem);
            //保存操作的订单详情数据
            map.put(orderItem.getId(), orderItem.getNum());

        }
        //设置订单数据
        //设置总金额
        order.setTotalMoney(totalMoney);
        //设置总数量
        order.setTotalNum(totalNum);
        //设置订单生成时间
        order.setCreateTime(new Date());
        //设置订单修改时间
        order.setUpdateTime(new Date());
        //设置订单状态
        order.setOrderStatus("0");
        //设置发货状态
        order.setConsignStatus("0");
        //设置支付状态
        order.setPayStatus("0");
        //保存订单数据到数据库
        orderMapper.insertSelective(order);
        //扣库存
        skuFeign.decrSkuNum(map);
        //增加用户积分
        Integer point = 10000;
        userFeign.incrUserPoints(point);
        //清理购物车缓存
        redisTemplate.boundHashOps("Cart_" + username).delete(Arrays.asList(ids));
        //将订单支付日志存入redis
        redisTemplate.boundHashOps("Order_"+username).put(order.getId(),order);
        //------10分钟自动回滚
        //发送消息到死信队列
        sendMessage(order);
        return order;
    }
/**
*
下单后发送消息到死信队列
 * @return : void
 */
    private void sendMessage(Order order) {
        System.out.println("用户下单成功,10分钟未支付,则过期回滚取消订单");
        System.out.println("下单时间为:" + new Date());
        //发送消息到死信队列
        rabbitTemplate.convertAndSend("dead_queue", (Object) JSONObject.toJSONString(order), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //设置消息过期时间
                message.getMessageProperties().setExpiration("1000*60*10");
                return message;
            }
        });
    }
    /**
     * 查询未支付订单
     *
     * @return : java.util.List<java.lang.String>
     */
    @Override
    public List<Order> queryByStatus(String status) {
        Order order = new Order();
        order.setPayStatus(status);
        return orderMapper.select(order);
    }

    /**
     * 支付失败,逻辑删除订单
     *
     * @param orderId :
     * @return : void
     */
    @Override
    public void deleteOrder(String orderId) {
        //根据id查询订单信息
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null || order.getId() == null) {
            throw new ChanggouException("订单数据不存在");
        }
        //修改状态
        order.setPayStatus("2");
        order.setUpdateTime(new Date());
        //同步修改数据到数据库
        orderMapper.updateByPrimaryKeySelective(order);
        //同步数据到redis
        redisTemplate.boundHashOps("Order_" + order.getUsername()).put(orderId, order);
        //支付失败,回滚库存
        //根据id查询订单详情
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        //查询订单详情
        List<OrderItem> orderItems = orderItemMapper.select(orderItem);
        //定义数据转换模型
        Map<String, Object> map = new HashMap<>();
        //遍历订单集合
        for (OrderItem item : orderItems) {
            map.put(item.getId(), item.getNum() * (-1));
        }
        //执行减库存操作
        skuFeign.decrSkuNum(map);
        //执行减积分操作
        userFeign.incrUserPoints(-1000);
    }

    /**
     * 支付成功,修改订单状态
     *
     * @param orderId       :
     * @param transactionid :交易流水号
     * @return : void
     */
    @Override
    public void updateStatus(String orderId, String transactionid) {
        //根据订单id查询订单信息
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null || order.getId() == null) {
            throw new ChanggouException("订单数据不存在");
        }
        order.setPayStatus("1");
        order.setUpdateTime(new Date());
        order.setPayTime(new Date());
        order.setTransactionId(transactionid);
        //同步修改数据到数据库
        orderMapper.updateByPrimaryKeySelective(order);
        //同步数据到redis
        redisTemplate.boundHashOps("Order_" + order.getUsername()).put(orderId, order);
    }

    /**
     * Order条件+分页查询
     * @param order 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> findPage(Order order, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(order);
        //执行搜索
        return new PageInfo<Order>(orderMapper.selectByExample(example));
    }

    /**
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Order> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Order>(orderMapper.selectAll());
    }

    /**
     * Order条件查询
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order){
        //构建查询条件
        Example example = createExample(order);
        //根据构建的条件查询数据
        return orderMapper.selectByExample(example);
    }


    /**
     * Order构建查询对象
     * @param order
     * @return
     */
    public Example createExample(Order order){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(order!=null){
            // 订单id
            if(!StringUtils.isEmpty(order.getId())){
                    criteria.andEqualTo("id",order.getId());
            }
            // 数量合计
            if(!StringUtils.isEmpty(order.getTotalNum())){
                    criteria.andEqualTo("totalNum",order.getTotalNum());
            }
            // 金额合计
            if(!StringUtils.isEmpty(order.getTotalMoney())){
                    criteria.andEqualTo("totalMoney",order.getTotalMoney());
            }
            // 优惠金额
            if(!StringUtils.isEmpty(order.getPreMoney())){
                    criteria.andEqualTo("preMoney",order.getPreMoney());
            }
            // 邮费
            if(!StringUtils.isEmpty(order.getPostFee())){
                    criteria.andEqualTo("postFee",order.getPostFee());
            }
            // 实付金额
            if(!StringUtils.isEmpty(order.getPayMoney())){
                    criteria.andEqualTo("payMoney",order.getPayMoney());
            }
            // 支付类型，1、在线支付、0 货到付款
            if(!StringUtils.isEmpty(order.getPayType())){
                    criteria.andEqualTo("payType",order.getPayType());
            }
            // 订单创建时间
            if(!StringUtils.isEmpty(order.getCreateTime())){
                    criteria.andEqualTo("createTime",order.getCreateTime());
            }
            // 订单更新时间
            if(!StringUtils.isEmpty(order.getUpdateTime())){
                    criteria.andEqualTo("updateTime",order.getUpdateTime());
            }
            // 付款时间
            if(!StringUtils.isEmpty(order.getPayTime())){
                    criteria.andEqualTo("payTime",order.getPayTime());
            }
            // 发货时间
            if(!StringUtils.isEmpty(order.getConsignTime())){
                    criteria.andEqualTo("consignTime",order.getConsignTime());
            }
            // 交易完成时间
            if(!StringUtils.isEmpty(order.getEndTime())){
                    criteria.andEqualTo("endTime",order.getEndTime());
            }
            // 交易关闭时间
            if(!StringUtils.isEmpty(order.getCloseTime())){
                    criteria.andEqualTo("closeTime",order.getCloseTime());
            }
            // 物流名称
            if(!StringUtils.isEmpty(order.getShippingName())){
                    criteria.andEqualTo("shippingName",order.getShippingName());
            }
            // 物流单号
            if(!StringUtils.isEmpty(order.getShippingCode())){
                    criteria.andEqualTo("shippingCode",order.getShippingCode());
            }
            // 用户名称
            if(!StringUtils.isEmpty(order.getUsername())){
                    criteria.andLike("username","%"+order.getUsername()+"%");
            }
            // 买家留言
            if(!StringUtils.isEmpty(order.getBuyerMessage())){
                    criteria.andEqualTo("buyerMessage",order.getBuyerMessage());
            }
            // 是否评价
            if(!StringUtils.isEmpty(order.getBuyerRate())){
                    criteria.andEqualTo("buyerRate",order.getBuyerRate());
            }
            // 收货人
            if(!StringUtils.isEmpty(order.getReceiverContact())){
                    criteria.andEqualTo("receiverContact",order.getReceiverContact());
            }
            // 收货人手机
            if(!StringUtils.isEmpty(order.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",order.getReceiverMobile());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(order.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",order.getReceiverAddress());
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(!StringUtils.isEmpty(order.getSourceType())){
                    criteria.andEqualTo("sourceType",order.getSourceType());
            }
            // 交易流水号
            if(!StringUtils.isEmpty(order.getTransactionId())){
                    criteria.andEqualTo("transactionId",order.getTransactionId());
            }
            // 订单状态,0:未完成,1:已完成，2：已退货
            if(!StringUtils.isEmpty(order.getOrderStatus())){
                    criteria.andEqualTo("orderStatus",order.getOrderStatus());
            }
            // 支付状态,0:未支付，1：已支付，2：支付失败
            if(!StringUtils.isEmpty(order.getPayStatus())){
                    criteria.andEqualTo("payStatus",order.getPayStatus());
            }
            // 发货状态,0:未发货，1：已发货，2：已收货
            if(!StringUtils.isEmpty(order.getConsignStatus())){
                    criteria.andEqualTo("consignStatus",order.getConsignStatus());
            }
            // 是否删除
            if(!StringUtils.isEmpty(order.getIsDelete())){
                    criteria.andEqualTo("isDelete",order.getIsDelete());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Order
     * @param order
     */
    @Override
    public void update(Order order){
        orderMapper.updateByPrimaryKey(order);
    }



    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
    @Override
    public Order findById(String id){
        return  orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Order全部数据
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }
}

package com.changgou.order.listener;

import com.alibaba.fastjson.JSONObject;
import com.changgou.exception.ChanggouException;
import com.changgou.order.pojo.Order;
import com.changgou.order.service.OrderService;
import com.changgou.pay.fegin.PayFegin;
import com.changgou.util.Result;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@Component
@RabbitListener(queues = "normal_queue")
public class OrderRabbitListener {
    @Autowired
    private PayFegin payFegin;
    @Autowired
    private OrderService orderService;
/**
*处理延时队列消息
 * @param message :
 * @param channel :
 * @param msg :
 * @return : void
 */
    @RabbitHandler
    public void deadOrderHandler(Message message, Channel channel, String msg) {
        try {
            System.out.println("获取到消息:" + msg + ":,时间为:" + new Date());
            //查询是否支付
            Order order = JSONObject.parseObject(msg,Order.class);
            //调用微信支付查询是否支付
            Result<Map<String, String>> mapResult = payFegin.queryPayStatus(order.getId());
            if (mapResult.isFlag() == false) {
                throw new ChanggouException("该订单不存在");
            }
            Map<String, String> map = mapResult.getData();
            String return_code = map.get("return_code");
            //判断是否访问到微信支付
            if (return_code.equals("SUCCESS")) {
                //判断是否支付成功
                String result_code = map.get("result_code");
                //获取订单id
                String out_trade_no = map.get("out_trade_no");
                if (result_code.equals("SUCCESS")) {
                    //支付成功,调用订单微服务修改订单状态
                    String transaction_id = map.get("transaction_id");
                    orderService.updateStatus(out_trade_no, transaction_id);
                    System.out.println("订单10分钟内已付款");
                } else {
                    //支付失败,删除订单
                    orderService.deleteOrder(out_trade_no);
                    System.out.println("订单超时10分钟未支付,自动取消");
                }
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

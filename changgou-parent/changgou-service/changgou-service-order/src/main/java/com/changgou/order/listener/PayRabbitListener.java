package com.changgou.order.listener;

import com.alibaba.fastjson.JSONObject;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@Component
@RabbitListener(queues = "order_queue")
public class PayRabbitListener {
    @Autowired
    private OrderService orderService;
    /**
     * 监听支付成功的消息
     *
     * @return : void
     */
    @RabbitHandler
    public void payRabbit(String msg) {
        //获取队列中的消息
        Map<String, String> map = JSONObject.parseObject(msg, Map.class);
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
            } else {
                //支付失败,删除订单
                orderService.deleteOrder(out_trade_no);
            }
        }
    }

}

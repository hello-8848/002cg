package com.changgou.order.job;

import com.changgou.exception.ChanggouException;
import com.changgou.order.pojo.Order;
import com.changgou.order.service.OrderService;
import com.changgou.pay.fegin.PayFegin;
import com.changgou.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
//定时任务,每10分钟调用微信支付查询一次未支付订单是否已支付
@Component
public class OrderStatus {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PayFegin payFegin;
    @Scheduled(initialDelay = -1,fixedDelay = 1000*60*10)
    public void checkOrderStatus() {
        System.out.println("定时任务执行了" + System.currentTimeMillis());

        String status = "0";
        //查询数据库中未支付订单
        List<Order> orders = orderService.queryByStatus(status);
        //遍历orders
        for (Order order : orders) {
            //调用微信支付查询是否支付完成
            Result<Map<String, String>> mapResult = payFegin.queryPayStatus(order.getId());
            if (mapResult.isFlag()==false) {
                throw new ChanggouException("根据状态查询订单失败");
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
                } else {
                    //支付失败,删除订单
                    orderService.deleteOrder(out_trade_no);
                }
            }
        }
    }
}

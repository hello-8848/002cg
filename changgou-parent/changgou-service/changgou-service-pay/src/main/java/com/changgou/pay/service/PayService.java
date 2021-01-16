package com.changgou.pay.service;

import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
public interface PayService {
/**
*创建二维码
 * @param out_trade_no :订单编号
 * @param total_fee : 订单金额
 * @return : java.util.Map<java.lang.String,java.lang.String>
 */
    Map<String, String> createNative(String out_trade_no, String total_fee);
/**
*查询支付状态
 * @param out_trade_no :
 * @return : java.util.Map<java.lang.String,java.lang.String>
 */
    Map<String, String> queryPayStatus(String out_trade_no);
}

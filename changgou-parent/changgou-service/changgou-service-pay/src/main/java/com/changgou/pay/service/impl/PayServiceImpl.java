package com.changgou.pay.service.impl;

import com.changgou.pay.service.PayService;
import com.changgou.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@Service
public class PayServiceImpl implements PayService {
    @Value("${weixin.appid}")
    private String appid;
    @Value("${weixin.partner}")
    private String partner;
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    @Value("${weixin.notifyurl}")
    private String notifyurl;

    /**
     * 请求微信创建二维码
     *
     * @param out_trade_no :订单编号
     * @param total_fee    : 订单金额
     * @return : java.util.Map<java.lang.String,java.lang.String>
     */
    @Override
    public Map<String, String> createNative(String out_trade_no, String total_fee) {
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        try {
            String xmlParam = getParam(out_trade_no, total_fee);
            //发送请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //获取微信响应结果
            String xmlResult = httpClient.getContent();
            //解析结果xml2map
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            return decodeResult(mapResult);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no :
     * @return : java.util.Map<java.lang.String,java.lang.String>
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {
        //请求微信支付路径
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            //定义封装请求参数的模型
            Map<String, String> param = new HashMap<>();
            param.put("appid", appid);
            param.put("mch_id", partner);
            param.put("out_trade_no",out_trade_no );
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            //将参数转换成xml并生成签名
            String xml = WXPayUtil.generateSignedXml(param, partnerkey);
            //发送请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xml);
            httpClient.post();
            //获取结果
            String content = httpClient.getContent();
            //xml转换成map
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
            //解析结果
            return decodeResult(xmlToMap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 解析微信支付返回结果的方法
     *
     * @param mapResult :
     * @return : java.util.Map<java.lang.String,java.lang.String>
     */
    private Map<String, String> decodeResult(Map<String, String> mapResult) {
        //判断返回结果
        String return_code = mapResult.get("return_code");
        //判断是否连接上微信支付
        if (return_code.equals("SUCCESS")) {
            String result_code = mapResult.get("result_code");
            //判断是否请求成功
            if (result_code.equals("SUCCESS")) {
                //返回数据
                return mapResult;
            }
        }
        return null;
    }

    /**
     * 封装请求微信支付生成二维码参数的方法
     *
     * @param out_trade_no :
     * @param total_fee    :
     * @return : java.lang.String
     */
    private String getParam(String out_trade_no, String total_fee) {
        try {
            //定义封装参数模型
            Map<String, String> param = new HashMap<>();
            param.put("appid", appid);
            param.put("mch_id", partner);
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            param.put("body", "畅购下单支付测试");
            param.put("out_trade_no", out_trade_no);
            param.put("total_fee", total_fee);
            param.put("spbill_create_ip", "192.168.211.1");
            param.put("notify_url", notifyurl);
            param.put("trade_type", "NATIVE");
            //将map转换成xml格式,并生成签名
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            return xmlParam;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

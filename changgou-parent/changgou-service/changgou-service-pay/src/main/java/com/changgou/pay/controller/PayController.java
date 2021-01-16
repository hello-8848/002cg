package com.changgou.pay.controller;


import com.alibaba.fastjson.JSONObject;
import com.changgou.pay.service.PayService;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@RestController
    @RequestMapping(value = "/weixin/pay")
public class PayController {

    @Autowired
    private PayService payService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 请求微信支付生成支付二维码
     *
     * @param out_trade_no :
     * @param total_fee    :
     * @return : com.changgou.util.Result<java.util.Map<java.lang.String,java.lang.String>>
     */
    @RequestMapping(value = "/create/native")
    public Result<Map<String, String>> createNative(String out_trade_no, String total_fee) {
        Map<String, String> map = payService.createNative(out_trade_no, total_fee);
        return new Result<>(true, StatusCode.OK, "生成微信支付二维码成功", map);
    }
/**
*查询支付信息
 * @param out_trade_no :
 * @return : com.changgou.util.Result<java.util.Map<java.lang.String,java.lang.String>>
 */
   /* @GetMapping(value = "/status/query")
    public Result<Map<String, String>> queryPayStatus(String out_trade_no) {
        Map<String, String> map = payService.queryPayStatus(out_trade_no);
        return new Result<>(true, StatusCode.OK, "查询支付状态成功", map);
    }*/
/**
*支付成功,微信回调支付系统
 * @param request :
 * @return : java.lang.String
 */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request) {
        try {
            //获取输入流
            ServletInputStream inputStream = request.getInputStream();
            //创建输出流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //定义缓冲空间
            byte[] buffer = new byte[1024];
            //定义每次读取长度
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                //写到输出流
                outputStream.write(buffer, 0, len);
            }
            //释放资源
            outputStream.close();
            inputStream.close();
            //将输出流转换为字符串
            String xml = new String(outputStream.toByteArray(), "UTF-8");
            //将xml转为map
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            //输出map支付结果
            System.out.println(map);
            //发送支付消息到rabbitmq
            rabbitTemplate.convertAndSend("order_exchange", "order_queue", JSONObject.toJSONString(map));
            //创建反馈微信已收到支付结果的模型
            Map<String, String> param = new HashMap<>();
            param.put("return_code", "SUCCESS");
            param.put("return_msg", "OK");
            return WXPayUtil.mapToXml(param);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    @RequestMapping(value = "/upload")
    public void upload(@RequestParam("file") MultipartFile file) throws Exception{
       /* String url = "http://localhost:8080/upload/upload04";
        //发送请求
        HttpClient httpClient = new HttpClient(url);
        httpClient.setHttps(true);
        httpClient.setXmlParam(new String(file.getBytes()));
        httpClient.post();
        //响应结果
        String result = httpClient.getContent();
        //解析结果xml2map
        System.out.println(result);*/

    }

}

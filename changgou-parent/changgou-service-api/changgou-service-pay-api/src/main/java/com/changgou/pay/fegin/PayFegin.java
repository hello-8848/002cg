package com.changgou.pay.fegin;

import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@FeignClient(name = "pay")
@RequestMapping(value = "/weixin/pay")
public interface PayFegin {
    /**
     * 查询支付信息
     *
     * @param out_trade_no :
     * @return : com.changgou.util.Result<java.util.Map<java.lang.String,java.lang.String>>
     */
    @GetMapping(value = "/status/query")
    public Result<Map<String, String>> queryPayStatus(String out_trade_no);
}

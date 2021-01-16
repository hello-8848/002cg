package com.changgou.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Auther lxy
 * @Date
 */
@Configuration
public class LimitRequest {
/**
*使用令牌桶指定IP地址限流
 * @return : org.springframework.cloud.gateway.filter.ratelimit.KeyResolver
 */
    @Bean(name = "ipKeyResolver")
    public KeyResolver userKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                //获得请求url
                String hostAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                System.out.println("hostAddress" + hostAddress);
                return Mono.just(hostAddress);
            }
        };
    }
}

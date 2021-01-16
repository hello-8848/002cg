package com.changgou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @Auther lxy
 * @Date
 */
//跨域访问
@Configuration
public class WebFilter {
/**
*跨域访问
 * @return : org.springframework.web.cors.reactive.CorsWebFilter
 */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        //设置cookie跨域
        configuration.setAllowCredentials(Boolean.TRUE);
        configuration.addAllowedMethod("*");
        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        //跨域解析器
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", configuration);//所有请求路径都支持跨域
        return new CorsWebFilter(source);
    }

}

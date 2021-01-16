package com.changgou.oauth.config;

import com.changgou.oauth.util.JwtToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Auther lxy
 * @Date
 */
//拦截器
@Configuration
public class FeignOauth2RequestInterceptor implements RequestInterceptor {

    /**
     * Called for every request. Add data using methods on the supplied {@link RequestTemplate}.
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        //创建令牌信息
        String token = "Bearer "+JwtToken.adminJwt();
        //将令牌信息加入到头文件中
        template.header("Authorization", token);
        //使用RequestContextHolder工具获取request相关变量
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            //取出request
            HttpServletRequest request = attributes.getRequest();
            //获取头信息
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    //获取头文件的key
                    String name = headerNames.nextElement();
                    //获取头文件的value
                    String header = request.getHeader(name);
                    //将key,value添加到redisTemplate中
                    template.header(name, header);
                }
            }
        }
    }
}

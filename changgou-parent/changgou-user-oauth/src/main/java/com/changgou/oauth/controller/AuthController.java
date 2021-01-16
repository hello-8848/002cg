package com.changgou.oauth.controller;

import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import com.changgou.oauth.util.CookieTools;
import com.changgou.oauth.util.CookieUtil;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther lxy
 * @Date
 */
@RestController
@RequestMapping(value = "/user")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private HttpServletRequest request;

    @Value("${auth.ttl}")
    private String ttl;
    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;
    @Value("${auth.cookieDomain}")
    private String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    private Integer cookieMaxAge;


    /**
     * 用户登录获取令牌
     *
     * @param username : 用户用户名
     * @param password : 用户密码
     * @return : com.changgou.util.Result
     */
    @PostMapping(value = "/login")
    public Result login(String username, String password) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return new Result(false, StatusCode.ERROR, "用户名和密码都不能空");
        }
        //获取令牌
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);
        //获取token
        String token = authToken.getAccessToken();
        //将令牌存入cookie
        CookieTools.setCookie(request,response,"Authorization",token);
        //将用户名放入cookie
        CookieTools.setCookie(request, response, "cuname", username);
        return new Result(true, StatusCode.OK, "登录成功");

    }
}

package com.changgou.oauth.service.impl;

import com.changgou.exception.ChanggouException;
import com.changgou.oauth.service.AuthService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    /**
     * 授权认证方法
     *
     * @param username     : 用户用户名
     * @param password     : 用户密码
     * @param clientId     :客户端Id
     * @param clientSecret :客户端秘钥
     * @return : com.changgou.oauth.util.AuthToken
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //申请令牌
        AuthToken authToken = getAuthToken(username, password, clientId, clientSecret);
        //判断
        if (authToken == null) {
            throw new ChanggouException("申请令牌失败");
        }
        return authToken;
    }
/**
*获取令牌的方法
 * @param username :用户名
 * @param password :密码
 * @param clientId :客户端Id
 * @param clientSecret :客户端秘钥
 * @return : com.changgou.oauth.util.AuthToken
 */
    private AuthToken getAuthToken(String username, String password, String clientId, String clientSecret) {

        //请求地址
       // String url = "http://localhost:9001/oauth/token";
        //通过负载均衡客户端获取服务的url
        ServiceInstance instance = loadBalancerClient.choose("user-auth");
        String url = instance.getUri().toString();
        url += "/oauth/token";
        try {
            //定义请求体body
            MultiValueMap<String, String> body=new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("username", username);
            body.add("password", password);
            //定义请求头headers
            MultiValueMap<String, String> headers=new LinkedMultiValueMap<>();
            //获取bse64编码
            String basciAuth = getBasciAuth(clientId, clientSecret);
            headers.add("Authorization",basciAuth);
            //发送post请求
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<MultiValueMap<String, String>>(body,headers), Map.class);
            Map<String, String> map = responseEntity.getBody();
            if(map == null || map.get("access_token") == null || map.get("refresh_token") == null || map.get("jti") == null)  {
                throw new ChanggouException("创建令牌失败");
            }
            //封装数据返回
            AuthToken authToken = new AuthToken();
            String access_token = map.get("access_token");
            String refresh_token = map.get("refresh_token");
            String jti = map.get("jti");
            authToken.setAccessToken(access_token);
            authToken.setRefreshToken(refresh_token);
            authToken.setJti(jti);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
/**
*获取客户端信息的base64编码
 * @param clientId :客户端id
 * @param clientSecret :客户端私钥
 * @return : java.lang.String
 */
    private String getBasciAuth(String clientId, String clientSecret) {
        String string = clientId + ":" + clientSecret;
        //加密
        String encode = Base64.getEncoder().encodeToString(string.getBytes());
        return "Basic " + encode;
    }



}

package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

/**
 * @Auther lxy
 * @Date
 */
public interface AuthService {
/**
*授权认证方法
 * @param username : 用户用户名
 * @param password : 用户密码
 * @param clientId :客户端Id
 * @param clientSecret :客户端秘钥
 * @return : com.changgou.oauth.util.AuthToken
 */
    AuthToken login(String username, String password,String clientId,String clientSecret);
}

package com.changgou.token;

import org.junit.Test;

import java.util.Base64;

/**
 * @Auther lxy
 * @Date
 */
public class TestBase64 {
    @Test
    public void test(){
        String string = "changgou:changgou";
        //加密
        String encode = Base64.getEncoder().encodeToString(string.getBytes());
        System.out.println("Basic " + encode);
    }
}

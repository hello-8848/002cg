package com.changgou.util;

/*****
 * @Author: 黑马训练营
 * @Description: com.changgou.util
 ****/
public class UrlUtils {

    /**
     * 去掉URL中指定的参数
     */
    public static String replateUrlParameter(String url,String... names){
        for (String name : names) {
            url = url.replaceAll("(&"+name+"=([0-9\\w]+))|("+name+"=([0-9\\w]+)&)|("+name+"=([0-9\\w]+))", "");
        }
        return url;
    }
}

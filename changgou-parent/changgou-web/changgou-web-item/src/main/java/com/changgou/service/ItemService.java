package com.changgou.service;

/**
 * @Auther lxy
 * @Date
 */
public interface ItemService {
    /**
     * 根据spuid生成静态页面
     *
     * @param spid :
     * @return : void
     */
    void createPageHtml(String spid);

    /**
     * 生成所有静态页面
     *
     * @return : void
     */
    void createAllPageHtml();
}

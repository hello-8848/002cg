package com.changgou.search.service;

import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
public interface EsSkuService {
/**
*导入数据到es
 * @return : void
 */
    void importData2Es();
/**
*条件搜索商品
 * @param searchMap :
 * @return : java.util.Map
 */
    Map<String,Object> search(Map<String, String> searchMap);
}

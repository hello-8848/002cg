package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * @Author:itheima
 * @Description:Spu业务层接口
 *****/
public interface SpuService {
/**
*还原物理删除的商品
 * @param spid :
 * @return : void
 */
    void restore(String spid);

/**
*逻辑删除商品
 * @param spid :
 * @return : void
 */
    void logicDelete(String spid);
/**
*批量下架
 * @param spids :
 * @return : int
 */
    int pullMany(String[] spids);
/**
*批量上架
 * @param spids :
 * @return : int
 */
    int putMany(String[] spids);
/**
*上架商品
 * @param spid :
 * @return : void
 */
    void put(String spid);
/**
*下架商品
 * @param spid :
 * @return : void
 */
    void pull(String spid);
/**
*审核商品并上架
 * @param spid :
 * @return : void
 */
    void audit(String spid);
/**
*根据spuId查询商品信息
 * @param spid :
 * @return : com.changgou.goods.pojo.Goods
 */
    Goods findBySpuId(String spid);
    /**
     * 新增商品
     *
     * @param goods :
     * @return : void
     */
    void saveGoods(Goods goods);

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 物理删除Spu
     * @param id
     */
    void delete(String id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     *
     * @param id
     * @return
     */
    Spu findById(String id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();
}

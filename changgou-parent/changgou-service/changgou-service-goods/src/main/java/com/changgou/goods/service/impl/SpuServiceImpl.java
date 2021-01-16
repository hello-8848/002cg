package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.exception.ChanggouException;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.util.IdWorker;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sun.xml.internal.bind.v2.model.core.ID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/****
 * @Author:itheima
 * @Description:Spu业务层接口实现类
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SkuMapper skuMapper;

    /**
     * 还原物理删除的商品
     *
     * @param spid :
     * @return : void
     */
    @Override
    public void restore(String spid) {
        //查询spu信息
        Spu spu = spuMapper.selectByPrimaryKey(spid);
        if (!"1".equals(spu.getIsDelete())) {
            throw new ChanggouException("商品未处于已删除状态,不能删除");
        }
        //设置未删除
        spu.setIsDelete("0");
        //设置未审核
        spu.setStatus("0");
        //修改spu
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 逻辑删除商品
     *
     * @param spid :
     * @return : void
     */
    @Override
    public void logicDelete(String spid) {
        //查询spu信息
        Spu spu = spuMapper.selectByPrimaryKey(spid);
        if (!"0".equals(spu.getIsMarketable())) {
            throw new ChanggouException("商品未下架,不能删除");
        }
        if (!"0".equals(spu.getStatus())) {
            throw new ChanggouException("商品未处于未审核状态,不能删除");
        }
        //设置物理删除
        spu.setIsDelete("1");
        //修改spu
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 批量下架
     *
     * @param spids :
     * @return : int
     */
    @Override
    public int pullMany(String[] spids) {
        //创建spu对象
        Spu spu = new Spu();
        spu.setIsMarketable("0");
        //构造条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(spids));
        criteria.andEqualTo("isMarketable", "1");//已上架
        criteria.andEqualTo("isDelete", "0");//未删除
        //下架
        return spuMapper.updateByExampleSelective(spu, example);
    }

    /**
     * 批量上架
     *
     * @param spids :
     * @return : int
     */
    @Override
    public int putMany(String[] spids) {
        //创建spu对象
        Spu spu = new Spu();
        //设置上架
        spu.setIsMarketable("1");
        //设置上架前提条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(spids));
        criteria.andEqualTo("isDelete", "0");//未删除
        criteria.andEqualTo("status", "1");//已审核
        criteria.andEqualTo("isMarketable", "0");//未上架
        //上架
        return spuMapper.updateByExampleSelective(spu, example);
    }


    /**
     * 上架商品
     *
     * @param spid :
     * @return : void
     */
    @Override
    public void put(String spid) {
        //查询spu信息
        Spu spu = spuMapper.selectByPrimaryKey(spid);
        //判断是否删除
        if ("1".equals(spu.getIsDelete())) {
            throw new ChanggouException("商品已经删除,无法上架");
        }
        //判断是否审核通过
        if (!"1".equals(spu.getStatus())) {
            throw new ChanggouException("商品未审核完成,无法上架");
        }
        //修改状态
        spu.setIsMarketable("1");
        //修改spu
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 下架商品
     *
     * @param spid :
     * @return : void
     */
    @Override
    public void pull(String spid) {
        //查询spu信息判断是否删除
        Spu spu = spuMapper.selectByPrimaryKey(spid);
        if ("1".equals(spu.getIsDelete())) {
            throw new ChanggouException("商品被逻辑删除,无法下架");
        }
        if ("0".equals(spu.getIsMarketable())) {
            throw new ChanggouException("商品已下架,请勿重复操作");
        }
        //反之下架商品
        spu.setIsMarketable("0");
        //修改spu
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 审核商品并且上架
     *
     * @param spid :
     * @return : void
     */
    @Override
    public void audit(String spid) {
        //根据spuid查询商品信息判断是否删除
        Spu spu = spuMapper.selectByPrimaryKey(spid);
        if ("1".equals(spu.getIsDelete())) {
            throw new ChanggouException("商品被逻辑删除,无法审核");
        }
        //反之审核通过并且上架
        spu.setStatus("1");
        spu.setIsMarketable("1");
        //修改spu
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 根据spuId查询商品信息
     *
     * @param spid :
     * @return : com.changgou.goods.pojo.Goods
     */
    @Override
    public Goods findBySpuId(String spid) {
        //根据spuId查询spu信息
        Spu spu = spuMapper.selectByPrimaryKey(spid);
        //根据spuId查询sku信息
        //构造查询条件传入spuId
        Sku sku = new Sku();
        sku.setSpuId(spid);
        List<Sku> skuList = skuMapper.select(sku);
        //将spu,sku放入到商品goods中
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    /**
     * 新增或修改商品
     *
     * @param goods :
     * @return : void
     */
    @Override
    @Transactional
    public void saveGoods(Goods goods) {
        //获得spu
        Spu spu = goods.getSpu();
        //判断是新增操作还是修改操作
        if (StringUtils.isEmpty(spu.getId())) {
            //新增操作
            //生成主键
            spu.setId("No" + idWorker.nextId());
            spuMapper.insertSelective(spu);
        } else {
            //修改操作
            //修改spu
            spuMapper.updateByPrimaryKey(spu);
            //构建删除旧的sku条件
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            //删除
            skuMapper.delete(sku);
        }
        //新增sku
        List<Sku> skuList = goods.getSkuList();
        //根据三级分类id查询分类信息
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        //根据品牌id查询品牌信息
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        skuList.forEach((Sku sku)->{
            //构建sku名称,格式为spu+sku规格信息
            //获取spu的名字
            String name = spu.getName();
            if (StringUtils.isEmpty(sku.getSpec())) {
                //如果未传规格信息
                sku.setSpec("{}");
            }
            //获取传入的规格信息用map接收
            Map<String, String> sepcMap = JSON.parseObject(sku.getSpec(), Map.class);
            //获取集合键值对
            Set<Map.Entry<String, String>> entries = sepcMap.entrySet();
            //遍历集合键值对
            for (Map.Entry<String, String> entry : entries) {
                //拼接spu名称+sku规格信息
                name += " " + entry.getValue();
            }
            sku.setId("S"+idWorker.nextId());//skuid
            sku.setName(name);//sku名字
            Date date = new Date();
            sku.setCreateTime(date);//sku创建时间
            sku.setUpdateTime(date);//sku修改时间
            sku.setSpuId(spu.getId());//sku对应的spuid
            sku.setCategoryId(spu.getCategory3Id());//sku对应的三级分类id
            sku.setCategoryName(category.getName());//sku对应的三级分类名称
            sku.setBrandName(brand.getName());//sku对应的品牌名称
            //插入sku数据
            skuMapper.insertSelective(sku);
        });

    }

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu){
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     * @param spu
     * @return
     */
    public Example createExample(Spu spu){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(spu!=null){
            // 主键
            if(!StringUtils.isEmpty(spu.getId())){
                    criteria.andEqualTo("id",spu.getId());
            }
            // 货号
            if(!StringUtils.isEmpty(spu.getSn())){
                    criteria.andEqualTo("sn",spu.getSn());
            }
            // SPU名
            if(!StringUtils.isEmpty(spu.getName())){
                    criteria.andLike("name","%"+spu.getName()+"%");
            }
            // 副标题
            if(!StringUtils.isEmpty(spu.getCaption())){
                    criteria.andEqualTo("caption",spu.getCaption());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(spu.getBrandId())){
                    criteria.andEqualTo("brandId",spu.getBrandId());
            }
            // 一级分类
            if(!StringUtils.isEmpty(spu.getCategory1Id())){
                    criteria.andEqualTo("category1Id",spu.getCategory1Id());
            }
            // 二级分类
            if(!StringUtils.isEmpty(spu.getCategory2Id())){
                    criteria.andEqualTo("category2Id",spu.getCategory2Id());
            }
            // 三级分类
            if(!StringUtils.isEmpty(spu.getCategory3Id())){
                    criteria.andEqualTo("category3Id",spu.getCategory3Id());
            }
            // 模板ID
            if(!StringUtils.isEmpty(spu.getTemplateId())){
                    criteria.andEqualTo("templateId",spu.getTemplateId());
            }
            // 运费模板id
            if(!StringUtils.isEmpty(spu.getFreightId())){
                    criteria.andEqualTo("freightId",spu.getFreightId());
            }
            // 图片
            if(!StringUtils.isEmpty(spu.getImage())){
                    criteria.andEqualTo("image",spu.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(spu.getImages())){
                    criteria.andEqualTo("images",spu.getImages());
            }
            // 售后服务
            if(!StringUtils.isEmpty(spu.getSaleService())){
                    criteria.andEqualTo("saleService",spu.getSaleService());
            }
            // 介绍
            if(!StringUtils.isEmpty(spu.getIntroduction())){
                    criteria.andEqualTo("introduction",spu.getIntroduction());
            }
            // 规格列表
            if(!StringUtils.isEmpty(spu.getSpecItems())){
                    criteria.andEqualTo("specItems",spu.getSpecItems());
            }
            // 参数列表
            if(!StringUtils.isEmpty(spu.getParaItems())){
                    criteria.andEqualTo("paraItems",spu.getParaItems());
            }
            // 销量
            if(!StringUtils.isEmpty(spu.getSaleNum())){
                    criteria.andEqualTo("saleNum",spu.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(spu.getCommentNum())){
                    criteria.andEqualTo("commentNum",spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if(!StringUtils.isEmpty(spu.getIsMarketable())){
                    criteria.andEqualTo("isMarketable",spu.getIsMarketable());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(spu.getIsEnableSpec())){
                    criteria.andEqualTo("isEnableSpec",spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if(!StringUtils.isEmpty(spu.getIsDelete())){
                    criteria.andEqualTo("isDelete",spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if(!StringUtils.isEmpty(spu.getStatus())){
                    criteria.andEqualTo("status",spu.getStatus());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        //根据id查询spu信息
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断是否逻辑删除
        if (!"0".equals(spu.getIsDelete())) {
            throw new ChanggouException("商品未逻辑删除,不能删除");
        }
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }
}

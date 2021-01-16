package com.changgou.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.exception.ChanggouException;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.service.ItemService;
import com.changgou.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * @Auther lxy
 * @Date
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private CategoryFeign categoryFeign;
    //传入输出路径
    @Value("${pagepath}")
    private String pagepath;
    /**
     * 根据spuid生成静态页面
     *
     * @param spid :
     * @return : void
     */
    @Override
    public void createPageHtml(String spid) {
        //根据spuid查询spu信息
        Result<Goods> result = spuFeign.findBySpuId(spid);
        if (!result.isFlag()) {
            throw new ChanggouException("根据spu查询信息失败");
        }
        Goods goods = result.getData();
        //创建静态页面
        createHtml(goods);
    }

    /**
     * 生成所有静态页面
     *
     * @return : void
     */
    @Override
    public void createAllPageHtml() {
        //查询所有spu列表
        Result<List<Spu>> result = spuFeign.findAll();
        List<Spu> spuList = result.getData();
        //遍历spuList
        for (Spu spu : spuList) {
            //根据spuid查询spu信息
            Result<Goods> goodsResult = spuFeign.findBySpuId(spu.getId());
            Goods goods = goodsResult.getData();
            //创建静态页面
            createHtml(goods);
        }

    }
/**
*生成静态页面的方法
 * @param goods :
 * @return : void
 */
    private void createHtml(Goods goods) {
        //商品获取spu信息
        Spu spu = goods.getSpu();
        //获取skuList
        List<Sku> skuList = goods.getSkuList();
        try {
            //创建上下文对象
            Context context = new Context();
            //根据id查询分类信息
            Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
            Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
            Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
            //设置到上下文对象中
            context.setVariable("category1", category1);
            context.setVariable("category2", category2);
            context.setVariable("category3", category3);
            //设置sku信息
            context.setVariable("skuList", skuList);
            //设置图片信息
            String images = spu.getImages();
            String[] strings = images.split(",");
            context.setVariable("imageList", strings);
            //设置规格信息
            String specItems = spu.getSpecItems();
            //序列化
            Map<String, String> specMap = JSONObject.parseObject(specItems, Map.class);
            context.setVariable("specMap", specMap);
            //创建文件对象
            File file = new File(pagepath, spu.getId() + ".html");
            PrintWriter writer = new PrintWriter(file,"UTF-8");

            templateEngine.process("item",context,writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

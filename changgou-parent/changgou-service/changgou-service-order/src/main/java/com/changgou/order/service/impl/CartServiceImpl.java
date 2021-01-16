package com.changgou.order.service.impl;

import com.changgou.exception.ChanggouException;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther lxy
 * @Date
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询购物车信息
     *
     * @param username :
     * @param ids      : skuId
     * @return : java.util.List<com.changgou.order.pojo.OrderItem>
     */
    @Override
    public List<OrderItem> choose(String username, String[] ids) {
        //定义接收数据模型
        List<OrderItem> orderItemList = new ArrayList<>();
        //遍历skuId
        for (String id : ids) {
            //从购物车中获取skuId对应信息
            OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps("Cart_" + username).get(id);
            //放入集合中
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }

    /**
     * 添加购物车
     *
     * @param num      :
     * @param skuId    :
     * @param username :
     * @return : void
     */
    @Override
    public void add(Integer num, String skuId, String username) {
        if (StringUtils.isEmpty(skuId)) {
            throw new ChanggouException("skuId参数错误");
        }
        if (num <= 0) {
            redisTemplate.boundHashOps("Cart_" + username).delete(skuId);
            return;
        }

        //根据skuId查询对应的sku信息
        Sku sku = skuFeign.findById(skuId).getData();
        if (sku == null||sku.getId() == null) {
            throw new ChanggouException("该商品sku不存在");
        }
        //根据spuId查询spu信息
        Spu spu = spuFeign.findById(sku.getSpuId()).getData();
        if (spu == null || spu.getId() == null) {
            throw new ChanggouException("该商品spu不存在");
        }
        //保存数据
        OrderItem orderItem = ConvertSku2OrderItem(num, sku, spu);
        //将购物车数据保存到redis中
        redisTemplate.boundHashOps("Cart_" + username).put(sku.getId(),orderItem);
    }

    /**
     * 加载购物车数据
     *
     * @param username :
     * @return : java.util.List<com.changgou.order.pojo.OrderItem>
     */
    @Override
    public List<OrderItem> findCart(String username) {
        List<OrderItem> orderItemList = redisTemplate.boundHashOps("Cart_"+username).values();
        return orderItemList;
    }

    /**
*将sku数据封装成orderItem
 * @param num :
 * @param sku :
 * @param spu :
 * @return : com.changgou.order.pojo.OrderItem
 */
    private OrderItem ConvertSku2OrderItem(Integer num, Sku sku, Spu spu) {
        //创建封装购物车商品信息的对象
        OrderItem orderItem = new OrderItem();
        //填充sku信息
        orderItem.setNum(num);
        orderItem.setSkuId(sku.getId());
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setImage(sku.getImage());
        orderItem.setMoney(num * sku.getPrice());
        //填充spu信息
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        return orderItem;
    }
}

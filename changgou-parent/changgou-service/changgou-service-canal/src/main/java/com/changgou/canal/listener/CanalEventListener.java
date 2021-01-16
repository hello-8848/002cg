package com.changgou.canal.listener;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.changgou.util.Result;
import com.xpand.starter.canal.annotation.DeleteListenPoint;
import com.xpand.starter.canal.annotation.InsertListenPoint;
import com.xpand.starter.canal.annotation.UpdateListenPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @Auther lxy
 * @Date
 */
@com.xpand.starter.canal.annotation.CanalEventListener
public class CanalEventListener {
    @Autowired
    private ContentFeign contentFeign;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 监控数据插入操作
     *
     * @param rowData :
     * @return : void
     */
    @InsertListenPoint
    public void insertEvent(CanalEntry.RowData rowData) {
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            System.out.println("新增之后影响的列名:" + column.getName() + "新增之后影响的列值:" + column.getValue());
        }
        syncFromDatabaseToRedis(afterColumnsList);
    }

    /**
     * 监控修改操作
     *
     * @param rowData :
     * @return : void
     */
    @UpdateListenPoint
    public void updateEvent(CanalEntry.RowData rowData) {
        //修改之前数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        syncFromDatabaseToRedis(beforeColumnsList);
        System.out.println("*********************以上是修改前*********************");
        //修改后数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        syncFromDatabaseToRedis(afterColumnsList);

    }

    /**
     * 监控删除操作
     *
     * @param rowData :
     * @return : void
     */
    @DeleteListenPoint
    public void deleteEvent(CanalEntry.RowData rowData) {
        //删除前数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            String name = column.getName();
            String value = column.getValue();
            System.out.println("删除前列名:" + name + "删除前列值:" + value);
        }
    }

    /**
     * 自定义数据监听
     *
     * @param eventType :
     * @param rowData   :
     * @return : void
     */
    //@ListenPoint(destination = "example",
    //        schema = "changgou_centent",
    //        eventType = {CanalEntry.EventType.INSERT,
    //                CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE}, table = "tb_content")
    //public void myEvent(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
    //    List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
    //    if (beforeColumnsList != null && beforeColumnsList.size() > 0) {
    //        //同步修改前数据
    //        syncFromDatabaseToRedis(beforeColumnsList);
    //    }
    //    List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
    //    if (afterColumnsList != null && afterColumnsList.size() > 0) {
    //        //同步修改后数据
    //        syncFromDatabaseToRedis(afterColumnsList);
    //    }
    //}

    /**
     * 同步数据库数据到redis
     *
     * @param ColumnsList :
     * @return : void
     */
    private void syncFromDatabaseToRedis(List<CanalEntry.Column> ColumnsList) {
        for (CanalEntry.Column column : ColumnsList) {
            String name = column.getName();
            String cid = column.getValue();
            System.out.println("列名:" + name + "---列值:" + cid);
            //如果列的名字为categoryId
            if ("category_id".equals(name)) {
                //根据分类id查询广告信息
                Result<List<Content>> result = contentFeign.findByCategoryId(Long.valueOf(cid));
                //将广告信息存入redis
                if (result.isFlag()) {
                    List<Content> contents = result.getData();
                    stringRedisTemplate.boundValueOps("content_" + cid).set(JSONObject.toJSONString(contents));
                }
            }
        }
    }

}

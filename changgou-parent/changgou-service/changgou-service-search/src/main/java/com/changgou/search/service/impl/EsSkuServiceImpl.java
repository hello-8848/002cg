package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.changgou.exception.ChanggouException;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.EsSkuMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.EsSkuService;
import com.changgou.util.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @Auther lxy
 * @Date
 */
@Service
public class EsSkuServiceImpl implements EsSkuService {
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private EsSkuMapper esSkuMapper;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 导入数据到es
     *
     * @return : void
     */
    @Override
    public void importData2Es() {
        //根据状态查询Sku
        Result<List<Sku>> result = skuFeign.findByStatus("1");
        if (!result.isFlag()) {
            throw new ChanggouException("根据状态查询sku信息失败");
        }
        //将Sku转成SkuInfo
        List<SkuInfo> skuInfos = JSONObject.parseArray(JSONObject.toJSONString(result.getData()), SkuInfo.class);
        //将SkuInfo中spe转换成specMap
        skuInfos.forEach((SkuInfo skuInfo) ->
        {
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec());
            skuInfo.setSpecMap(specMap);
        });
        //将SkuInfo导入到Es中
        esSkuMapper.saveAll(skuInfos);
    }

    /**
     * 条件搜索商品
     *
     * @param searchMap :
     * @return : java.util.Map
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //构建搜索条件
        NativeSearchQueryBuilder builder = buildBasicQuery(searchMap);
        //执行搜索
        Map<String, Object> resultMap = getSearch(builder, searchMap);
        return resultMap;
    }

    /**
     * 执行搜索的方法
     *
     * @param builder :
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     */
    private Map<String, Object> getSearch(NativeSearchQueryBuilder builder, Map<String, String> searchMap) {
        //定义接收数据的模型
        Map<String, Object> resultMap = new HashMap<>();
        //查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class, new SearchResultMapper() {
            /**
             *对搜索出来的结果进行处理
             * @param response :
             * @param clazz :
             * @param pageable :
             * @return : org.springframework.data.elasticsearch.core.aggregation.AggregatedPage<T>
             */
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //定义接收高亮数据的模型
                List<T> skuList = new ArrayList<>();
                //获得所有数据
                SearchHits hits = response.getHits();
                //获得数据结果集
                Iterator<SearchHit> iterator = hits.iterator();
                //迭代遍历数据
                while (iterator.hasNext()) {
                    //获得每一条数据
                    SearchHit hit = iterator.next();
                    //每条数据转化为SkuInfo类型
                    SkuInfo skuInfo = JSONObject.parseObject(hit.getSourceAsString(), SkuInfo.class);
                    //获得高亮数据
                    HighlightField highlightField = hit.getHighlightFields().get("name");
                    if (highlightField != null) {
                        Text[] fragments = highlightField.getFragments();
                        String name = "";
                        for (Text fragment : fragments) {
                            name += fragment;
                        }
                        skuInfo.setName(name);//设置高亮
                        skuList.add((T) skuInfo);//添加到集合中
                    }
                }
                //获得查询的结果
                Aggregations aggregations = response.getAggregations();
                return new AggregatedPageImpl<T>(skuList, pageable, hits.getTotalHits(), aggregations);
            }
        });
        //获得结果集
        List<SkuInfo> skuInfoList = skuInfos.getContent();
        //存入集合
        resultMap.put("rows", skuInfoList);
        //获得分页结果--开始
        //获得总条数
        long totalElements = skuInfos.getTotalElements();
        resultMap.put("totalElements", totalElements);
        //获得当前页码
        Pageable pageable = skuInfos.getPageable();
        int pageNumber = pageable.getPageNumber();
        resultMap.put("pageNumber", pageNumber + 1);
        //获得每页大小
        int pageSize = pageable.getPageSize();
        resultMap.put("pageSize", pageSize);
        //获得分页结果结束---结束
        //获取聚合查询结果----开始----start
        Aggregations aggregations = skuInfos.getAggregations();
        //当类别不是查询条件,获得类别查询结果
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            List<String> categoryList = getList(aggregations, "category");
            resultMap.put("categoryList", categoryList);
        }
        //当品牌不是查询条件,获得品牌查询结果
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            List<String> brandList = getList(aggregations, "brand");
            resultMap.put("brandList", brandList);
        }
        //获得规格查询结果
        List<String> specList = getList(aggregations, "spec");
        //转换为map集合去重
        Map<String, Set<String>> specMap = getSpecMap(specList);
        resultMap.put("specList", specMap);
        //获取聚合查询结果----结束----end
        return resultMap;
    }

    /**
     * 构建搜索条件的方法
     *
     * @param searchMap :
     * @return : org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //搜索条件
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //高亮显示
        HighlightBuilder.Field field = new HighlightBuilder
                .Field("name")
                .preTags("<font style='color:red'>")
                .postTags("</font>")
                .fragmentSize(100);//高亮个数
        builder.withHighlightFields(field);
        //搜索条件构造对象
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //条件组合
        if (searchMap != null && searchMap.size() > 0) {

            //关键字
            if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
                queryBuilder.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")));
            }
            //分类
            if (!StringUtils.isEmpty(searchMap.get("category"))) {
                queryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }
            //品牌
            if (!StringUtils.isEmpty(searchMap.get("brand"))) {
                queryBuilder.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            //规格
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if (entry.getKey().startsWith("spec_")) {
                    //截取spec后面字符串
                    String substring = entry.getKey().substring(5);
                    //拼接
                    queryBuilder.must(QueryBuilders.termQuery("specMap." + substring + ".keyword", entry.getValue()));
                }
            }
            //价格条件
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                price = price.replace("元", "").replace("以上", "");
                //截取字符串获取价格极值
                String[] strings = price.split("-");
                //价格下限条件
                queryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(strings[0])));
                if (strings.length > 1) {
                    //证明有价格上限
                    queryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(strings[1])));
                }
            }
            //分页条件
            int pageNo = pageConvert(searchMap);//当前页码
            int pageSize = 10;//每页大小
            builder.withPageable(PageRequest.of(pageNo - 1, pageSize));
            //排序条件
            String sortRule = searchMap.get("sortRule");
            String sortField = searchMap.get("sortField");
            if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
                builder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
            }
            builder.withQuery(queryBuilder);
            //聚合查询条件的构建------开始------start
            if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
                //构建分类域聚合搜索条件
                builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(100000));
            }
            //品牌
            if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
                //构建品牌域聚合搜索条件
                builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));
            }
            //构建规格聚合查询条件
            builder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(100000));
            //聚合查询条件的构建------结束------end
        }
        return builder;
    }

    /**
     * 规格数组转换成规格名称详情集合,去重规格详情数据
     *
     * @param specList :
     * @return : java.util.Map<java.lang.String,java.util.Set<java.lang.String>>
     */
    private Map<String, Set<String>> getSpecMap(List<String> specList) {
        //定义接收数据的模型
        Map<String, Set<String>> specMap = new HashMap<>();
        //list转换成map集合
        for (String spec : specList) {
            //将每种规格转换成名称和数据的键值对map集合
            Map<String, String> map = JSONObject.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entries = map.entrySet();
            //遍历键值对
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                //判断是否规格名称已经存对应set集合
                Set<String> stringSet = specMap.get(key);
                if (stringSet == null || stringSet.size() == 0) {
                    stringSet = new HashSet<String>();
                }
                //该规格名称对应数据放入set集合中
                stringSet.add(value);
                //将该规格名称和数据set集合放入specMap中
                specMap.put(key, stringSet);
            }
        }
        return specMap;
    }

    /**
     * 处理聚合查询结果,获得聚合查询列表
     *
     * @param aggregations :
     * @param termName     :
     * @return : java.util.List<java.lang.String>
     */
    private List<String> getList(Aggregations aggregations, String termName) {
        //定义接收结果模型
        List<String> list = new ArrayList<>();
        //获得聚合查询结果
        StringTerms stringTerms = aggregations.get(termName);
        stringTerms.getBuckets().forEach(bucket -> list.add(bucket.getKeyAsString()));
        return list;
    }

    /**
     * 获取当前页码
     *
     * @param searchMap :
     * @return : int
     */
    private int pageConvert(Map<String, String> searchMap) {
        try {
            int pageNum = Integer.parseInt(searchMap.get("pageNum"));
            return pageNum;
        } catch (Exception e) {
        }
        return 1;
    }
//========================================================================================================


    //Map<String, Object> aggregationResult = getAggregationResult(builder, searchMap);
    //resultMap.putAll(aggregationResult);
    //分类信息聚合搜索
    //if (searchMap == null || searchMap.get("category") == null) {
    //    //如果用户没有输入分类信息,则查询展示分类信息
    //    List<String> categoryList = getCategoryList(builder);
    //    resultMap.put("categoryList", categoryList);
    //}
    ////品牌信息聚合查询
    //if (searchMap == null || searchMap.get("brand") == null) {
    //    //如果用户没有输入品牌信息,则查询展示品牌信息
    //    List<String> brandList = getBrandList(builder);
    //    resultMap.put("brandList", brandList);
    //}
    //聚合查询分类信息及品牌信息
    //Map<String, List<String>> dataMap = getCategoryAndBrandList(builder);
    //dataMap.entrySet().forEach((Map.Entry<String, List<String>> entry)->{resultMap.put(entry.getKey(),entry.getValue());});
    //规格信息聚合查询
    //Map<String, Set<String>> specList = getSpecList(builder);
    //resultMap.put("specList", specList);

    /**
     * 获得聚合查询结果
     *
     * @param builder   :
     * @param searchMap :
     * @return : java.util.List<java.lang.String>
     */
    private Map<String, Object> getAggregationResult(NativeSearchQueryBuilder builder, Map<String, String> searchMap) {
        //定义接收数据的模型
        Map<String, Object> resultMap = new HashMap<>();
        //分类

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            //构建分类域聚合搜索条件
            builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(100000));
        }
        //品牌
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            //构建品牌域聚合搜索条件
            builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));
        }
        //构建规格聚合查询条件
        builder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(100000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获得聚合查询结果
        Aggregations aggregations = skuInfos.getAggregations();
        //当类别不是查询条件,获得类别查询结果
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            List<String> categoryList = getList(aggregations, "category");
            resultMap.put("categoryList", categoryList);
        }
        //当品牌不是查询条件,获得品牌查询结果
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            List<String> brandList = getList(aggregations, "brand");
            resultMap.put("brandList", brandList);
        }
        //获得规格查询结果
        List<String> specList = getList(aggregations, "spec");
        //转换为map集合去重
        Map<String, Set<String>> specMap = getSpecMap(specList);
        resultMap.put("specList", specMap);
        return resultMap;
    }

    /**
     * 品牌信息聚合查询
     *
     * @param builder :
     * @return : java.util.List<java.lang.String>
     */
    private List<String> getBrandList(NativeSearchQueryBuilder builder) {
        //定义数据接收模型
        List<String> brandList = new ArrayList<>();
        //指定需要聚合的域
        builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获得聚合结果
        StringTerms stringTerms = skuInfos.getAggregations().get("brand");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        buckets.forEach(bucket -> brandList.add(bucket.getKeyAsString()));
        return brandList;
    }

    /**
     * 分类信息聚合查询
     *
     * @param builder :
     * @return : java.util.List<java.lang.String>
     */
    private List<String> getCategoryList(NativeSearchQueryBuilder builder) {
        //定义数据接收模型
        List<String> categoryList = new ArrayList<>();
        //指定对那个域进行聚合
        builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(100000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获得聚合查询的结果
        StringTerms stringTerms = skuInfos.getAggregations().get("category");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        buckets.forEach(bucket -> categoryList.add(bucket.getKeyAsString()));
        return categoryList;
    }


    /**
     * 聚合查询所有规格信息
     *
     * @param builder :
     * @return : java.util.List<java.lang.String>
     */
    private Map<String, Set<String>> getSpecList(NativeSearchQueryBuilder builder) {
        //定义数据接收模型
        List<String> specList = new ArrayList<>();
        //指定聚合查询的域
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(100000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获得聚合结果
        StringTerms stringTerms = skuInfos.getAggregations().get("skuSpec");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        buckets.forEach(bucket -> specList.add(bucket.getKeyAsString()));
        //list集合转换成map集合
        Map<String, Set<String>> specMap = getSpecMap(specList);
        return specMap;
    }


}

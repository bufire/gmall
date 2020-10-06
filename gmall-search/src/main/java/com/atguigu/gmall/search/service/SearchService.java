package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchParamVo;
import com.atguigu.gmall.search.entity.SearchResponseAttrVo;
import com.atguigu.gmall.search.entity.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    //通过前端传入的数据从elasticSearch查询相应的goods对象并封装为前端所需要的数据格式
    public SearchResponseVo search(SearchParamVo searchParam) {
        try {

            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(searchParam));
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(response);
            SearchResponseVo responsevo = this.parseResult(response);
            responsevo.setPageNum(searchParam.getPageNum());
            responsevo.setPageSize(searchParam.getPageSize());
            return responsevo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析搜索结果集
     * @param
     * @return
     */
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo responseVo = new SearchResponseVo();
        SearchHits hits = response.getHits();
        // 总命中的记录数
        responseVo.setTotal(hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            // 获取内层hits的_source 数据
            String goodsJson = hitsHit.getSourceAsString();
            // 反序列化为goods对象
            Goods goods = JSON.parseObject(goodsJson, Goods.class);
            // 获取高亮的title覆盖掉普通的title
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            if(highlightField != null){
                String highlightTitle = highlightField.getFragments()[0].toString();
                goods.setTitle(highlightTitle);
            }
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);
        //聚合结果集的解析
        System.out.println(response.getAggregations());
        if(response.getAggregations() != null){
            Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
            ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");
            List<? extends Bucket> buckets = brandIdAgg.getBuckets();
            if(!CollectionUtils.isEmpty(buckets)){
                buckets.stream().map(bucket -> {
                    BrandEntity brandEntity = new BrandEntity();
                    Long brandId = ((Bucket) bucket).getKeyAsNumber().longValue();
                    brandEntity.setId(brandId);
                    // 解析品牌名称的子聚合,获取品牌名称
                    Map<String,Aggregation> brandAggregationMap = ((Bucket) bucket).getAggregations().asMap();
                    ParsedStringTerms brandNameAgg = (ParsedStringTerms)brandAggregationMap.get("brandNameAgg");
                    brandEntity.setName(brandNameAgg.getBuckets().get(0).getKeyAsString());
                    // 解析品牌logo的子聚合,获取品牌的logo
                    ParsedStringTerms logoAgg = (ParsedStringTerms)brandAggregationMap.get("logoAgg");
                    List<? extends Bucket> logoAggBuckets = logoAgg.getBuckets();
                    if(!CollectionUtils.isEmpty(logoAggBuckets)){
                        brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
                    }
                    // 把map反序列化为json字符串
                    return brandEntity;
                }).collect(Collectors.toList());
            }

            // 2.解析聚合结果集,获取分类
            ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
            List<? extends Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
            if(!CollectionUtils.isEmpty(categoryIdAggBuckets)){
                List<CategoryEntity> categories = categoryIdAggBuckets.stream().map(bucket -> {
                    CategoryEntity categoryEntity = new CategoryEntity();
                    // 获取bucket的key,key就是分类的id
                    long categoryId = ((Bucket) bucket).getKeyAsNumber().longValue();
                    categoryEntity.setId(categoryId);
                    // 解析分类名称的子聚合,获取分类名称
                    ParsedStringTerms categoryNameAgg = (ParsedStringTerms) ((Bucket) bucket).getAggregations().get("categoryNameAgg");
                    categoryEntity.setName(categoryNameAgg.getBuckets().get(0).getKeyAsString());
                    return categoryEntity;
                }).collect(Collectors.toList());
                responseVo.setCategories(categories);
            }
//         3.解析聚合结果集,获取规格参数
            ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");
            ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
            List<? extends Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
            if(!CollectionUtils.isEmpty(attrIdAggBuckets)){
                List<SearchResponseAttrVo> filters = attrIdAggBuckets.stream().map(bucket -> {
                    SearchResponseAttrVo responseAttrvo = new SearchResponseAttrVo();
                    // 规格参数id
                    long attrId = ((Terms.Bucket) bucket).getKeyAsNumber().longValue();
                    responseAttrvo.setAttrId(((Bucket) bucket).getKeyAsNumber().longValue());
                    // 规格参数的名称

                    ParsedStringTerms attrNameAgg = (ParsedStringTerms) ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                    if(attrNameAgg != null){
                        responseAttrvo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                    }
                    // 规格参数值
                    ParsedStringTerms attrValueAgg = (ParsedStringTerms) ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
                    List<? extends Bucket> attrValueAggBuckets = null;
                    if(attrValueAgg != null){
                        attrValueAggBuckets = attrValueAgg.getBuckets();
                        if (!CollectionUtils.isEmpty(attrIdAggBuckets)) {
                            List<String> attrValues = attrValueAggBuckets.stream().map(Bucket::getKeyAsString).collect(Collectors.toList());
                            responseAttrvo.setAttrValues(attrValues);
                        }
                    }

                    return responseAttrvo;
                }).collect(Collectors.toList());
                responseVo.setFilters(filters);
            }
        }

        return responseVo;
    }

    /**
     * 构建查询DSL语句
     * @param paramVo
     * @return
     */
    private SearchSourceBuilder buildDsl(SearchParamVo paramVo){
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String keyword = paramVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            // TODO：打广告
            System.out.println("good morning");
            return sourceBuilder;
        }
        // 1. 构建查询条件（bool查询）
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1.1. 匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));
        // 1.2. 过滤
        // 1.2.1. 品牌过滤
        List<Long> brandId = paramVo.getBrandId();
        if(!CollectionUtils.isEmpty(brandId)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brandId));
        }
        // 1.2.2. 分类过滤
        Long cid = paramVo.getCid();
        if(cid != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId",cid));
        }
        // 1.2.3. 价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if(priceFrom != null || priceTo != null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null){
                rangeQuery.gte(priceFrom);
            }
            if(priceTo != null){
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);
        }
        // 1.2.4. 是否有货
        Boolean store = paramVo.getStore();
        if(store != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }
        // 1.2.5. 规格参数的过滤 props=5:高通-麒麟&props=6:骁龙865-硅谷1000
        List<String> props = paramVo.getProps();
        if(props != null){
            props.forEach(prop ->{
                String[] attrs = StringUtils.split(prop, ":");
                if (attrs != null && attrs.length == 2){
                    String attrId = attrs[0];
                    String attrValueString = attrs[1];
                    String[] attrValues = StringUtils.split(attrValueString,"-");
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId",attrId));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue",attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",boolQuery,ScoreMode.None));
                }
            });
        }
        sourceBuilder.query(boolQueryBuilder);
        // 2. 构建排序 0-默认，得分降序；1-按价格升序；2-按价格降序；3-按创建时间降序；4-按销量降序
        Integer sort = paramVo.getSort();
        String field = "";
        SortOrder order = null;
        switch (sort){
            case 1: field = "price"; order = SortOrder.ASC; break;
            case 2: field = "price"; order = SortOrder.DESC; break;
            case 3: field = "createTime"; order = SortOrder.DESC; break;
            case 4: field = "sales"; order = SortOrder.DESC; break;
            default: field = "_score"; order = SortOrder.DESC; break;
        }
        sourceBuilder.sort(field,order);
        // 3. 构建分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        System.out.println((pageNum - 1) * pageSize);
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        // 4. 构建高亮
        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red'>").postTags("</font>"));
        // 5. 构建聚合
        // 5.1. 构建品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
            .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
            .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );
        // 5.2. 构建分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
            .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );
        // 5.3. 构建规格参数的嵌套聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","searchAttrs")
            .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId"))
            .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
            .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs,attrValue"))
        );
        // 6. 构建结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId","title","price","defaultImage"},null);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }
}

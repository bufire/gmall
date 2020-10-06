package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttrValue;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import io.jsonwebtoken.lang.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    ElasticsearchRestTemplate restTemplate;
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;

    @Test
    void importData() {
        //创建索引及映射
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);
        Integer pageNum = 1;
        Integer pageSize = 100;
        do {
            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            ResponseVo<List<SpuEntity>> spuResp = pmsClient.querySpusByPage(pageParamVo);
            List<SpuEntity> spus = spuResp.getData();

            // 遍历spu,查询sku
            spus.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResp = this.pmsClient.querySkusBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResp.getData();
                if(!Collections.isEmpty(skuEntities)){
                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        // 查询spu搜索属性及值
                        ResponseVo<List<SpuAttrValueEntity>> attrValueResp = pmsClient.querySearchAttrValueBySpuId(spuEntity.getId(), spuEntity.getCategoryId());
                        List<SpuAttrValueEntity> attrValueEntities = attrValueResp.getData();
                        List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                        if (!Collections.isEmpty(attrValueEntities)) {
                            searchAttrValues = attrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                searchAttrValue.setAttrId(spuAttrValueEntity.getAttrId());
                                searchAttrValue.setAttrName(spuAttrValueEntity.getAttrName());
                                searchAttrValue.setAttrValue(spuAttrValueEntity.getAttrValue());
                                return searchAttrValue;
                            }).collect(Collectors.toList());
                        }
                        //查询sku搜索属性及值
                        ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResp = pmsClient.queryAttrValueBySku(spuEntity.getId(), spuEntity.getCategoryId());
                        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResp.getData();
                        List<SearchAttrValue> searchSKuAttrValues = new ArrayList<>();
                        if (!Collections.isEmpty(skuAttrValueEntities)) {
                            searchSKuAttrValues = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                searchAttrValue.setAttrId(skuAttrValueEntity.getAttrId());
                                searchAttrValue.setAttrValue(skuAttrValueEntity.getAttrValue());
                                searchAttrValue.setAttrName(skuAttrValueEntity.getAttrName());
                                return searchAttrValue;
                            }).collect(Collectors.toList());
                        }
                        searchAttrValues.addAll(searchSKuAttrValues);
                        goods.setSearchAttrs(searchAttrValues);

                        // 查询品牌
                        ResponseVo<BrandEntity> brandEntityResp = pmsClient.queryBrandById(skuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResp.getData();
                        if (brandEntity != null) {
                            goods.setBrandId(skuEntity.getBrandId());
                            goods.setBrandName(skuEntity.getName());
                        }

                        //查询分类
                        ResponseVo<CategoryEntity> cateGoryEntityResp = pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                        CategoryEntity categoryEntity = cateGoryEntityResp.getData();
                        if (categoryEntity != null) {
                            goods.setCategoryId(skuEntity.getCatagoryId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        goods.setCreateTime(spuEntity.getCreateTime());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setSales(0L);
                        goods.setSkuId(skuEntity.getId());

                        // 查询库存信息
                        ResponseVo<List<WareSkuEntity>> listResp = wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = listResp.getData();
                        if (!Collections.isEmpty(wareSkuEntities)) {
                            boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                            goods.setStore(flag);
                        }
                        goods.setTitle(skuEntity.getTitle());
                        return goods;
                    }).collect(Collectors.toList());
                    this.goodsRepository.saveAll(goodsList);
                }
            });
            pageSize = spus.size();
            pageNum++;
        }while (pageSize==100);
    }

}

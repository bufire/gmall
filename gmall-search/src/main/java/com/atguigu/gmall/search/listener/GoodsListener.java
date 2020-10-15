package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchAttrValue;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.impl.AMQChannel;
import io.jsonwebtoken.lang.Collections;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    GmallPmsClient pmsClient;
    @Autowired
    GmallWmsClient wmsClient;

    @RabbitListener(bindings = @QueueBinding(
            value =@Queue(value = "SEARCH_ADD_QUEUE",durable = "true"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {
        try {
            System.out.println(spuId);
            ResponseVo<SpuEntity> responseVo = this.pmsClient.querySpuById(spuId);
            SpuEntity spuEntity = responseVo.getData();
            ResponseVo<List<SkuEntity>> skuResp = this.pmsClient.querySkusBySpuId(spuId);
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
                    ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResp = pmsClient.queryAttrValueBySku(spuId, spuEntity.getCategoryId());
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
                    ResponseVo<CategoryEntity> cateGoryEntityResp = pmsClient.queryCategoryById(skuEntity.getCategoryId());
                    CategoryEntity categoryEntity = cateGoryEntityResp.getData();
                    if (categoryEntity != null) {
                        goods.setCategoryId(skuEntity.getCategoryId());
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
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            // 是否已经重试过
            if (message.getMessageProperties().getRedelivered()){
                // 未重试过,重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
                System.out.println("hi");
            } else {
                // 以重试过直接拒绝
                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,false);
                System.out.println("good");
            }
        }

    }
}

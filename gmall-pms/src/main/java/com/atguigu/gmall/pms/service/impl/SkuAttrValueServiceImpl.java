package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {
    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    //根据属性sku与分组id查询sku检索属性与值
    @Override
    public List<SkuAttrValueEntity> queryAttrValueBySku(Long skuId, Long category_id) {
//        //1.获取检索属性列表
////        QueryWrapper<AttrEntity> attQueryWrapper = new QueryWrapper<>();
////        attQueryWrapper.eq("category_id", category_id).eq("search_type", 1);
////        List<AttrEntity> attrEntities = attrMapper.selectList(attQueryWrapper);
////        List<Long> attrIds = new ArrayList<>();
////        attrEntities.forEach(attrEntity -> attrIds.add(attrEntity.getId()));
////        //2 获取sku检索属性与值集合
////        QueryWrapper<SkuAttrValueEntity> skuAttrValueEntityQueryWrapper = new QueryWrapper<>();
////        skuAttrValueEntityQueryWrapper.eq("sku_id",skuId).in("attr_id",attrIds);
////        return this.list(skuAttrValueEntityQueryWrapper);
        // 根据分类id查询出检索类型的规格参数
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", category_id).eq("search_type", 1));
        if (CollectionUtils.isEmpty(attrEntities)){
            return null;
        }
        // 获取检索规格参数id
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        // 根据skuId和attrIds查询销售检索类型的规格参数和值
        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", attrIds));
    }
}
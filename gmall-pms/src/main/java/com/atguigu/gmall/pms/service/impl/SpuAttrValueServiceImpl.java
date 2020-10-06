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

import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("spuAttrValueService")
public class SpuAttrValueServiceImpl extends ServiceImpl<SpuAttrValueMapper, SpuAttrValueEntity> implements SpuAttrValueService {
    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    //根据spu查询检索属性及值
    @Override
    public List<SpuAttrValueEntity> querySearchAttrValueBySpuId(Long spuId,Long categoryId) {
        // 查询分类下的检索类型的规格参数
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("category_id", categoryId).eq("search_type", 1));

        if (CollectionUtils.isEmpty(attrEntities)){
            return null;
        }
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        return this.list(new QueryWrapper<SpuAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));
//        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
//        //1根据类别id查询出检索属性id
//        List<AttrEntity> attrEntities = attrMapper.selectList(queryWrapper.eq("category_id", categoryId).eq("search_type", 1));
//        //1.1提取检索属性集合
//        List<Long> attrIds = new ArrayList<>();
//        attrEntities.forEach(attrEntity -> attrIds.add(attrEntity.getId()));
//        attrIds.forEach(System.out::print);
//        //2 获取检索属性及值
//        QueryWrapper<SpuAttrValueEntity> AVqueryWrapper = new QueryWrapper<>();
//        AVqueryWrapper.eq("spu_id",spuId).in("attr_id",attrIds);
//        return this.list(AVqueryWrapper);
    }


}
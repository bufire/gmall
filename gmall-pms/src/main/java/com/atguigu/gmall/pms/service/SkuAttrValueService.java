package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author Aaron
 * @email 153398483@qq.com
 * @date 2020-09-21 16:30:40
 */
public interface SkuAttrValueService extends IService<SkuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SkuAttrValueEntity> queryAttrValueBySku(Long skuId,Long category_id);

    //根据spu查询对应的销售信息
    String querySkusJsonBySpuId(Long spuId);

    List<SaleAttrValueVo> querySkuAttrValuesBySpuId(Long spuId);
}


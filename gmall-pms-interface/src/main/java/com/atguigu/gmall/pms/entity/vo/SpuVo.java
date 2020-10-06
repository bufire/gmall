package com.atguigu.gmall.pms.entity.vo;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Data
public class SpuVo extends SpuInfoEntity {
    // 图片信息
    private List<String> spuImages;

    // 基本属性信息
    private List<SpuAttrValueVo> baseAttrs;

    // sku信息
    private List<SkuVo> skus;
}

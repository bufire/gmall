package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SkuAttrValueServiceImplTest {
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Test
    void querySkusJsonBySpuId() {
        skuAttrValueMapper.querySkusJsonBySpuId(20L);
    }
}
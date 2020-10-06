package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author Aaron
 * @email 153398483@qq.com
 * @date 2020-09-21 16:30:40
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuPageByCategory(PageParamVo pageParamVo, Long cId);

    void bigSave(SpuVo spuVo);
}


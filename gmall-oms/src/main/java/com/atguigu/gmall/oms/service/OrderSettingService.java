package com.atguigu.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.oms.entity.OrderSettingEntity;

import java.util.Map;

/**
 * 订单配置信息
 *
 * @author Aaron
 * @email 153398483@qq.com
 * @date 2020-09-21 17:48:38
 */
public interface OrderSettingService extends IService<OrderSettingEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}


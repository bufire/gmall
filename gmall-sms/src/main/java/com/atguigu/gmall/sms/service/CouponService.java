package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.CouponEntity;

import java.util.Map;

/**
 * 优惠券信息
 *
 * @author Aaron
 * @email 153398483@qq.com
 * @date 2020-09-23 11:45:17
 */
public interface CouponService extends IService<CouponEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}


package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Aaron
 * @email 153398483@qq.com
 * @date 2020-09-21 17:48:38
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
    public int closeOrder(String orderToken);

    int successOrder(String orderToken);
}

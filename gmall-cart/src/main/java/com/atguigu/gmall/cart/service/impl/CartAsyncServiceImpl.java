package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.bean.Cart;
import com.atguigu.gmall.cart.map.CartMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncServiceImpl implements CartAsyncService {
    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateCartByUserIdAndSkuId(String userId, Cart cart){
        cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
    }

    /**
     * 为了方便将来在异常处理器中获取异常用户信息
     * 所有异步方法的第一个参数统一为userId
     * @param userId
     * @param cart
     */
    @Async
    public void saveCart(String userId, Cart cart){
        this.cartMapper.insert(cart);
    }

    @Override
    public void deleteCartByUserId(String userKey) {
        cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userKey));
    }

    @Async
    @Override
    public void deleteCartByUserIdAndSkuId(String userId, Long skuId) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }
}

package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.bean.Cart;
import org.springframework.stereotype.Service;

@Service
public interface CartAsyncService {
    public void updateCartByUserIdAndSkuId(String userId, Cart cart);
    public void saveCart(String userId, Cart cart);

    void deleteCartByUserId(String userKey);

    void deleteCartByUserIdAndSkuId(String userId, Long skuId);
}

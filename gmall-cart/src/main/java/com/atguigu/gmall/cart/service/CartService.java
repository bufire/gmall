package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.bean.Cart;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;

@Service
public interface CartService {
    void addCart(Cart cart);

    Cart queryCartBySkuId(Long skuId);

    ListenableFuture<String> executor1();

    String executor2();

    List<Cart> queryCarts();

    void updateNum(Cart cart);

    void deleteCart(Long skuId);

    //获取购物城勾选状态
    List<Cart> queryCheckedCarts(Long userId);
}

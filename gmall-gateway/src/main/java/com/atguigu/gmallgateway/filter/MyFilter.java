//package com.atguigu.gmallgateway.filter;
//
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.lang.annotation.Annotation;
//
//@Component
//public class MyFilter implements GlobalFilter, Order {
//    @Override
//    public Class<? extends Annotation> annotationType() {
//        return null;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        System.out.println("This is my customize Filter");
//        return chain.filter(exchange);
//    }
//
//    @Override
//    public int value() {
//        return 0;
//    }
//}

package com.atguigu.gmallitem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import rx.Completable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootTest
class GmallItemApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
//        CompletableFuture<Object> future = CompletableFuture.supplyAsync(new Supplier<Object>() {
//            @Override
//            public Object get() {
//                System.out.println(Thread.currentThread().getName() + "completableFuture");
////                int i = 10 / 0;
//                return 1024;
//            }
//        }).whenComplete((Object o, Throwable throwable) -> {
//                System.out.println("--------o" + o.toString());
//                System.out.println("--------throwable" + throwable);
//        }).exceptionally((Throwable throwable) -> {
//                System.out.println("throwable--------" + throwable);
//                return 66666;
//        });
//        CompletableFuture<String> future1 = future.thenApplyAsync(apple -> {
//            System.out.println("上一级参数" + apple);
//            System.out.println("thenApplyAsync 开始执行");
//            return "aaa";
//        });
//        CompletableFuture<Void> future3 = future1.thenAcceptAsync(a -> {
//            System.out.println("上一级参数" + a);
//            System.out.println("thenAcceptAsync 开始执行");
//        });
//        future3.thenRunAsync(() ->{
//            System.out.println("thenRunAsync 开始执行");
//        });
//        try {
//            System.out.println(future.get());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        List<CompletableFuture<String>> futures = Arrays.asList(CompletableFuture.completedFuture("java"),
                CompletableFuture.completedFuture("python"),
                CompletableFuture.completedFuture("go"),
                CompletableFuture.completedFuture("php")
        );
        final CompletableFuture<Void> allCompleted = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
        allCompleted.thenRun(() -> {
            futures.stream().forEach(f -> {
                try {
                    System.out.println("get future at:"+System.currentTimeMillis()+", result:"+f.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
    }

}

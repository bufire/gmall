package com.atguigu.gmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@RefreshScope
@EnableSwagger2
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.atguigu.gmall.wms.mapper")
public class GmallWmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallWmsApplication.class, args);
    }
}

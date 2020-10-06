package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.common.bean.ResponseVo;

import com.atguigu.gmall.smsinterface.entity.vo.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {

}
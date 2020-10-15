package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.entity.UserEntity;

import java.util.Map;

/**
 * 用户表
 *
 * @author Aaron
 * @email 153398483@qq.com
 * @date 2020-10-13 19:46:48
 */
public interface UserService extends IService<UserEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    //校验信息是否可用
    Boolean checkData(String data, Integer type);

    //用户注册
    void register(UserEntity userEntity, String code);

    //查询用户信息
    UserEntity queryUser(String loginName, String password);
}


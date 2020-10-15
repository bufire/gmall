package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.StringUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        switch(type){
            case 1:
                queryWrapper.eq("username",data);
                break;
            case 2:
                queryWrapper.eq("phone",data);
                break;
            case 3:
                queryWrapper.eq("email",data);
                break;
        }
        return userMapper.selectCount(queryWrapper) == 0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // 生成盐
        String salt = StringUtils.replace(UUID.randomUUID().toString(), "-", "");
        userEntity.setSalt(salt);
        userEntity.setPassword(DigestUtils.md5Hex(salt + DigestUtils.md5Hex(userEntity.getPassword())));
        // 设置创建时间等
        userEntity.setCreateTime(new Date());
        userEntity.setLevelId(1l);
        userEntity.setStatus(1);
        userEntity.setIntegration(0);
        userEntity.setGrowth(0);
        userEntity.setNickname(userEntity.getUsername());

        // 添加到数据库
        boolean b = this.save(userEntity);
    }

    //查询用户信息
    @Override
    public UserEntity queryUser(String loginName, String password) {
        //获取盐与加密密码
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("username",loginName);
        UserEntity userEntity = this.userMapper.selectOne(wrapper);
        String md5 = userEntity.getPassword();
        String salt = userEntity.getSalt();
        if(!md5.equals(DigestUtils.md5Hex(salt + DigestUtils.md5Hex(password)))){
            return null;
        }
        return userEntity;
    }

}
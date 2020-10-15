package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author Aaron
 * @email 153398483@qq.com
 * @date 2020-10-13 19:46:48
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}

package com.atguigu.gmall.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategory(Long parentId) {
        //如果传入的值为-1则查询所有的父级信息
        if(parentId == -1){
            return this.list(null);
        }
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_id",parentId));
    }

    //获取一级分类下的二级分类和三级分类
    @Override
    public List<CategoryEntity> queryCategoriesWithSub(Long pid) {
        return this.categoryMapper.queryCategoriesByPid(pid);
    }

    //根据三级分类id获取一二三级分类
    @Override
    public List<CategoryEntity> queryCategoriesByCid3(Long cid3) {
        // 查询三级分类
        CategoryEntity categoryEntity3 = this.categoryMapper.selectById(cid3);

        // 查询二级分类
        CategoryEntity categoryEntity2 = this.categoryMapper.selectById(categoryEntity3.getParentId());

        // 查询一级分类
        CategoryEntity categoryEntity1 = this.categoryMapper.selectById(categoryEntity2.getParentId());

        return Arrays.asList(categoryEntity1, categoryEntity2, categoryEntity3);
    }

}
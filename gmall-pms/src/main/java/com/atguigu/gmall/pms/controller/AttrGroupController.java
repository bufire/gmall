package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 属性分组
 *
 * @author Aaron
 * @email 153398483@qq.com
 * @date 2020-09-21 16:30:40
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    //获取属性组以及对应的参数和值
    @GetMapping("withattrvalues")
    public ResponseVo<List<ItemGroupVo>> queryGroupsBySpuIdAndCid(
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId,
            @RequestParam("cid")Long cid
    ){
        List<ItemGroupVo> itemGroupVOS = attrGroupService.queryGroupsBySpuIdAndCid(spuId, skuId, cid);
        return ResponseVo.ok(itemGroupVOS);
    }

    @ApiOperation("获取商品属性")
    @GetMapping("withattrs/{cId}")
    public ResponseVo<List<GroupVo>> queryAttrByCid(@PathVariable("cId") Long cId){
        List<GroupVo> list = attrGroupService.queryAttrByCid(cId);
        return ResponseVo.ok(list);
    }

    @ApiOperation("获取属性组")
    @GetMapping("category/{cId}")
    public ResponseVo<List<AttrGroupEntity>> queryByCidPage(@PathVariable("cId")Long cId){
        List<AttrGroupEntity> list = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("category_id",cId));
        return ResponseVo.ok(list);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);
        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}

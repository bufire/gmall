package com.atguigu.gmallitem.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmallitem.service.ItemService;
import com.atguigu.gmallitem.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
//@RequestMapping("item")
public class ItemController {

    @Autowired
    private ItemService itemService;

//    @GetMapping("{skuId}")
//    public ResponseVo<ItemVo> load(@PathVariable("skuId")Long skuId){
//
//        ItemVo itemVo = this.itemService.load(skuId);
//
//        return ResponseVo.ok(itemVo);
//    }

    @GetMapping("/{skuId}.html")
    public String load(@PathVariable("skuId")Long skuId, Model model){

        ItemVo itemVo = this.itemService.load(skuId);
        model.addAttribute("itemVo", itemVo);

        return "item";
    }
}

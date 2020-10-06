package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.entity.SearchParamVo;
import com.atguigu.gmall.search.entity.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchService searchService;
//    @GetMapping
//    public ResponseVo<Object> search(SearchParamVo searchParam) throws IOException {
//        SearchResponseVo search = this.searchService.search(searchParam);
//        return ResponseVo.ok(search);
//    }
    @GetMapping
    public String search(SearchParamVo paramVo, Model model){
        System.out.println("1234");
        SearchResponseVo responseVo = this.searchService.search(paramVo);
        model.addAttribute("response", responseVo);
        model.addAttribute("searchParam", paramVo);
        System.out.println("567");
        return "search";
    }
}

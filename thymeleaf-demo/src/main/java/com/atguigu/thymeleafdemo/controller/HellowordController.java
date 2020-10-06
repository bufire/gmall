package com.atguigu.thymeleafdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HellowordController {
    @GetMapping("test")
    public String test(Model model){
        model.addAttribute("msg","hello thymeleaf");
        return "hello";
    }
}

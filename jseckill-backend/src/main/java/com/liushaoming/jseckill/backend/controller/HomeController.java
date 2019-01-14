package com.liushaoming.jseckill.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("")
public class HomeController {

    @RequestMapping("")
    public String index(){
        return "index";
    }
}

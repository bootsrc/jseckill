package com.liushaoming.jseckill.backend.controller;

import com.alibaba.fastjson.JSON;
import com.liushaoming.jseckill.backend.dao.SeckillDAO;
import com.liushaoming.jseckill.backend.entity.Seckill;
import com.liushaoming.jseckill.backend.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @RequestMapping("/demo")
    @ResponseBody
    public String demo() {
        long seckillId = 1000L;
        Seckill seckill = seckillService.getById(seckillId);
        return JSON.toJSONString(seckill);
    }
}

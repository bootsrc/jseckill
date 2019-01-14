package com.liushaoming.jseckill.backend.service.impl;

import com.liushaoming.jseckill.backend.dao.SeckillDAO;
import com.liushaoming.jseckill.backend.entity.Seckill;
import com.liushaoming.jseckill.backend.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private SeckillDAO seckillDAO;

    @Override
    public Seckill getById(long seckillId) {
        return seckillDAO.queryById(seckillId);
    }
}

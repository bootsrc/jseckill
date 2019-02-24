package com.liushaoming.jseckill.backend.controller;

import com.alibaba.fastjson.JSON;
import com.liushaoming.jseckill.backend.dto.Exposer;
import com.liushaoming.jseckill.backend.dto.SeckillExecution;
import com.liushaoming.jseckill.backend.dto.SeckillResult;
import com.liushaoming.jseckill.backend.entity.Seckill;
import com.liushaoming.jseckill.backend.enums.SeckillStateEnum;
import com.liushaoming.jseckill.backend.exception.SeckillException;
import com.liushaoming.jseckill.backend.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @RequestMapping("/demo")
    @ResponseBody
    public String demo() {
        long seckillId = 1000L;
        Seckill seckill = seckillService.getById(seckillId);
        Thread currentThread = Thread.currentThread();
        logger.info("thread.hashCode={},id={},name={}"
                , new Object[]{currentThread.hashCode(), currentThread.getId(), currentThread.getName()});
        return JSON.toJSONString(seckill);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);
        return "list";
    }

    @RequestMapping(value = "/detail/{seckillId}", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";
        }
        model.addAttribute("seckill", seckill);
        return "detail";
    }

    //ajax json
    @RequestMapping(value = "/exposer/{seckillId}",
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable Long seckillId) {
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true, exposer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    @RequestMapping(value = "/execution/{seckillId}/{phone}/{md5}",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("phone") Long phone,
                                                   @PathVariable("md5") String md5) {
        //springmvc valid
        if (phone == null) {
            return new SeckillResult<SeckillExecution>(false, "未注册");
        }
        SeckillResult<SeckillExecution> result;
        try {
            SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (SeckillException e1) {
            SeckillExecution execution = new SeckillExecution(seckillId, e1.getSeckillStateEnum());
            return new SeckillResult<SeckillExecution>(true, execution);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true, execution);
        }
    }

    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time() {
        Date now = new Date();
        return new SeckillResult(true, now.getTime());
    }

    /**
     * @param seckillId
     * @param phone
     * @return 返回"1"代表秒杀成功，返回"0", 表示没有抢到。
     * @TODO String boughtKey = RedisKeyPrefix.BOUGHT_USERS + seckillId
     * 还有一个redisKey存放已经入队列了的userPhone，   ENQUEUED_USER
     * 进队列的时候sadd ENQUEUED_USER , 消费成功的时候，sdel ENQUEUED_USER
     * 查询这个isGrab接口的时候，先查sismembles boughtKey, true则表明秒杀成功.
     * 否则，ismembles ENQUEUED_USER, 如果在队列中，说明排队中， 如果不在，说明秒杀失败
     */
    @RequestMapping(value = "/isGrab/{seckillId}/{phone}")
    @ResponseBody
    public String isGrab(@PathVariable("seckillId") Long seckillId,
                         @PathVariable("phone") Long phone) {
        int result = seckillService.isGrab(seckillId, phone);
        return result + "";
    }
}

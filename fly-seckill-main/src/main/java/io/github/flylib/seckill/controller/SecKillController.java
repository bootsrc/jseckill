package io.github.flylib.seckill.controller;

import io.github.flylib.seckill.common.Message;
import io.github.flylib.seckill.common.SecKillEnum;
import io.github.flylib.seckill.service.SecKillService;
import io.github.flylib.seckill.web.req.SecKillRequest;
import io.github.flylib.seckill.web.vo.SecKillResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/seckill")
@RestController
public class SecKillController {

    @Autowired
    private SecKillService secKillService;

    /**
     * MySQL悲观锁
     *
     * @param requestMessage
     * @return
     */
    @RequestMapping(value = "/pessLockInMySQL", method = RequestMethod.POST)
    public Message<SecKillResponse> pessLockInMySQL(@RequestBody Message<SecKillRequest> requestMessage) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", requestMessage.getBody().getUserId());
        paramMap.put("productId", requestMessage.getBody().getProductId());
        SecKillEnum secKillEnum = secKillService.handleByPessLockInMySQL(paramMap);
        Message<SecKillResponse> responseMessage = new Message<>(secKillEnum, null);
        return responseMessage;
    }

    /**
     * MySQL乐观锁
     *
     * @param requestMessage
     * @return
     */
//    @RequestMapping(value = "/posiLockInMySQL",method = RequestMethod.POST)
//    public Message<SecKillResponse> posiLockInMySQL(@RequestBody Message<SecKillRequest> requestMessage){
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("userId",requestMessage.getBody().getUserId());
//        paramMap.put("productId",requestMessage.getBody().getProductId());
//        SecKillEnum secKillEnum = secKillService.handleByPosiLockInMySQL(paramMap);
//        Message<SecKillResponse> responseMessage = new Message<>(secKillEnum,null);
//        return responseMessage;
//    }
    @RequestMapping(value = "/posiLockInMySQL")
    public Message<SecKillResponse> posiLockInMySQL(int userId, int productId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", userId);
        paramMap.put("productId", productId);
        SecKillEnum secKillEnum = secKillService.handleByPosiLockInMySQL(paramMap);
        Message<SecKillResponse> responseMessage = new Message<>(secKillEnum, null);
        return responseMessage;
    }

    /**
     * 利用redis的watch监控的特性
     *
     * @throws InterruptedException
     */
    @RequestMapping(value = "/baseOnRedisWatch", method = RequestMethod.POST)
    public Message<SecKillResponse> baseOnRedisWatch(@RequestBody Message<SecKillRequest> requestMessage) throws InterruptedException {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", requestMessage.getBody().getUserId());
        paramMap.put("productId", requestMessage.getBody().getProductId());
        SecKillEnum secKillEnum = secKillService.handleByRedisWatch(paramMap);
        Message<SecKillResponse> responseMessage = new Message<>(secKillEnum, null);
        return responseMessage;
    }

    @RequestMapping(value = "/baseOnRedisWatchSimple")
    public Message<SecKillResponse> baseOnRedisWatchSimple(SecKillRequest requestMessage) throws InterruptedException {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", requestMessage.getUserId());
        paramMap.put("productId", requestMessage.getProductId());
        SecKillEnum secKillEnum = secKillService.handleByRedisWatch(paramMap);
        Message<SecKillResponse> responseMessage = new Message<>(secKillEnum, null);
        return responseMessage;
    }

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public String test(@RequestBody Message<SecKillRequest> requestMessage) {
        System.out.println(requestMessage.getBody().getUserId() + "," + requestMessage.getBody().getProductId());
        return "success";
    }
}

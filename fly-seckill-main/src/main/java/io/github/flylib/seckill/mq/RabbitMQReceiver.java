package io.github.flylib.seckill.mq;

//@Component
//@RabbitListener(queues = "seckillQueue")
public class RabbitMQReceiver {

/*    @Autowired
    private SecKillMapper secKillMapper;

    @RabbitHandler
    public void process(String message) throws Exception {
        Record record = JSON.parseObject(message, new TypeReference<Record>(){});
        //插入record
        secKillMapper.insertRecord(record);
        //更改物品库存
        secKillMapper.updateByAsynPattern(record.getProduct());
    }*/
}

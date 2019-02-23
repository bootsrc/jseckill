# jseckill


![license](https://img.shields.io/github/license/alibaba/dubbo.svg)


<code>jseckill</code>:Java实现的秒杀网站，基于Spring Boot 2.X。 

<code>jseckill</code>:Seckill website implemented with Java, based on Spring Boot 2.X.

**访问这里进行在线演示**：[http://jseckill.appjishu.com](http://jseckill.appjishu.com)

<br/>

| 🚀 | 🔍 |💡
| :--------: | :---------: | :------: |
| [百度](https://www.baidu.com) | [源码解析](#源码解析) | [联系作者](#联系作者) |


### 源码解析             
👉 [源码解析文档](SOURCE-README.md)
<br/><br/>

### 演示
![](doc/image/demo-1.jpg)  &nbsp;&nbsp; ![](doc/image/demo-2.jpg) 
<br/>
<br/>
![](doc/image/demo-3.jpg)


### 技术栈
1.Spring Boot <br/>
2.MyBatis <br/>
3.Redis <br/>
4.Thymeleaf <br/>
5.Bootstrap <br/>
6.RabbitMQ <br/>
7.zookeeper实现分布式锁-Curator <br/>

### 高并发优化手段
1.使用Google guava的RateLimiter来进行限流
<br/>
2.减库存时，在同一事务内，先"插入记录"，再"更新库存", 能有效减少行锁的作用时间.
<br/>
数据库更新操作，采用乐观锁，提高并发性 
<br/>
3.暴露秒杀接口，暴露信息，作为不常更新的热点数据，贮存到Redis里 
<br/>
4.前端静态文档部署到CDN, 缺少资金的公司可以选择动静分离 <br/>
动静分离:把静态资源（js,css，图片）直接部署放到nginx， 动态服务还在原有的tomcat/SpringBoot里。
<br/>
5.Java应用部署多个集群节点，之间使用nginx做负载均衡和反向代理，提高客户端的并发数
<br/>
6.RabbitMQ异步处理秒杀记录<br/>

### 秒杀过程
1.RateLimiter限流。 并发量大的时候，直接舍弃掉部分用户的请求 <br/>
2.Redis判断是否秒杀过。避免重复秒杀。如果没有秒杀过， <br/>
在Redis操作前分布式加锁
Redis秒杀（减库存，并记录已秒杀成功者的userPhone) <br/>
然后分布式解锁 <br/>
3.发送秒杀记录到RabbitMQ，并且马上返回结果到客户端 <br/>
4.监听RabbitMQ的队列消息, 一条条地读取消息后，操作数据库。插入秒杀记录和减库存。 <br/>
并手动ACK队列 <br/>
详情见源码文档 <br/>

<br/>
<b>TODO</b> <br/>
进一步的优化：等到CountDownLatch每积累20个，才去操作redis, 直接decrby 10 

### 未完待续
<b>演示地址</b><br/>
👉 [http://jseckill.appjishu.com](http://jseckill.appjishu.com) <br/>

 现在工作略忙，后面抽空完善技术文档<br/>
📌⭐⭐⭐❤❤❤ <br/>
<h3><b>GitHub地址，路过的帮忙点个星星star，谢谢😊</b></h3>
🐱 [https://github.com/liushaoming/jseckill](https://github.com/liushaoming/jseckill) 
<br/>
<br/>

有代码改进优化的建议的统一在Issues里面提
<br/>

### 联系作者

加QQ群讨论 
<br/>
![](doc/image/group-qrcode.png)

微信公众号

![](doc/image/public-account.jpg)

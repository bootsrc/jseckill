# jseckill

![license](https://img.shields.io/github/license/alibaba/dubbo.svg)

<br/>

| 📘 | 🛫 | 🏛  | 🐱 | 🛒 | 🚀 | 💡 | 🔨 | 💌 |
| :--------: | :---------: | :------: | :------: | :------: | :------: | :------: | :------: | :------: |
| [介绍](#介绍) | [演示](#演示) | [架构图](#架构图) | [技术栈](#技术栈) | [秒杀过程](#秒杀过程) | [Quick Start](#QuickStart) | [源码解析](#源码解析) | [做贡献](#做贡献) | [联系作者](#联系作者) |

## 介绍

<code>jseckill</code>:Java实现的秒杀网站，基于Spring Boot 2.X。 

<code>jseckill</code>:Seckill website implemented with Java, based on Spring Boot 2.X.


## 演示
**点击进入演示**：[http://jseckill.appjishu.com](http://jseckill.appjishu.com)

注意：提升输入手机号时，随便输入一个11位的数字即可，**不需要填自己的真实手机号**

效果图

![](doc/image/demo-1.jpg)  &nbsp;&nbsp; ![](doc/image/demo-2.jpg) 
<br/>
<br/>
![](doc/image/demo-3.jpg)

## 架构图
<br/><br/><br/><br/>
![](doc/image/arch-1.jpg)
<br/>
<br/>

## 技术栈
- Spring Boot 2.X
- MyBatis
- Redis, MySQL
- Thymeleaf + Bootstrap
- RabbitMQ
- Zookeeper, Apache Curator

## 秒杀过程
1.Google guava RateLimiter限流。 并发量大的时候，直接舍弃掉部分用户的请求 <br/>
2.Redis判断是否秒杀过。避免重复秒杀。如果没有秒杀过， <br/>
在Redis操作前分布式加锁
Redis秒杀（减库存，并记录已秒杀成功者的userPhone) <br/>
然后分布式解锁 <br/>
3.发送秒杀记录到RabbitMQ，并且马上返回结果到客户端 <br/>
4.监听RabbitMQ的队列消息, 一条条地读取消息后，操作数据库。插入秒杀记录和减库存。 <br/>
并手动ACK队列 <br/>
详情见源码文档 <br/>

## QuickStart
- clone源码

<code>git clone https://github.com/liushaoming/jseckill.git </code>

- 在Intelij IDEA/eclipse里导入根路径下的pom.xml，再导入文件夹jseckill-backend下面的pom.xml, 等待maven依赖下载完毕

- 修改application.properties里面的自己的Redis,MySQL,Zookeeper,RabbitMQ的连接配置

- 右键JseckillBackendApplication.java--run as--Java Application

开始Debug

## 源码解析    
👉 [进入源码解析](SOURCE-README.md)
### Java后端限流
使用Google guava的RateLimiter来进行限流 <br/>
例如：每秒钟只允许10个人进入秒杀步骤. (可能是拦截掉90%的用户请求，拦截后直接返回"很遗憾，没抢到") <br/>
AccessLimitServiceImpl.java代码 <br/>
```java
package com.liushaoming.jseckill.backend.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.liushaoming.jseckill.backend.service.AccessLimitService;
import org.springframework.stereotype.Service;

/**
 * 秒杀前的限流.
 * 使用了Google guava的RateLimiter
 */
@Service
public class AccessLimitServiceImpl implements AccessLimitService {
    /**
     * 每秒钟只发出10个令牌，拿到令牌的请求才可以进入秒杀过程
     */
    private RateLimiter seckillRateLimiter = RateLimiter.create(10);

    /**
     * 尝试获取令牌
     * @return
     */
    @Override
    public boolean tryAcquireSeckill() {
        return seckillRateLimiter.tryAcquire();
    }
}
```       
👉 [查看更多源码解析](SOURCE-README.md)


## 做贡献
欢迎提交代码发送<code>Pull Requests</code>, 有代码改进优化的建议的统一在Issues里面提。

喜欢本项目的，**请在GitHub右上角点**[star](https://github.com/liushaoming/jseckill/stargazers)

### 联系作者
| 开发者 | liushaoming |
| :--------: | :--------: |
| email | [liushaomingdev@163.com](mailto://liushaomingdev@163.com) |
| QQ | 944147540 |


加QQ群讨论 
<br/>
![](doc/image/group-qrcode.png)

微信公众号

![](doc/image/public-account.jpg)

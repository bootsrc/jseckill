# jseckill
电商秒杀程序, 乐观锁，Spring Boot.

## 技术栈
1.Spring Boot <br/>
2.MyBatis <br/>
3.Redis <br/>
4.Thymeleaf <br/>
5.Bootstrap <br/>

## 高并发优化手段
1.在同一事务内，先"插入记录"，再"更新库存", 能有效减少行锁的作用时间

2.数据库更新操作，采用乐观锁，提高并发性

3.暴露秒杀接口，暴露信息，作为不常更新的热点数据，贮存到Redis里 

4.前端静态文档部署到CDN, 缺少资金的公司可以选择动静分离
动静分离:把静态资源（js,css，图片）直接部署放到nginx， 动态服务还在原有的tomcat/SpringBoot里。

5.Java应用部署多个集群节点，之间使用nginx做负载均衡和反向代理，提高客户端的并发数

## 演示
演示地址<br/>
👉 [http://seckill.appjishu.com](http://seckill.appjishu.com)

## 未完待续
秒杀系统最能提升自己的高并发技术编程能力 <br/>
现在工作略忙，后面抽空完善技术文档。请**star此项目**，以持续关注。<br/>
📌⭐⭐⭐❤❤❤

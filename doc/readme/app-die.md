# jseckill运行了一段时间后，发现进程是活的端口也是通的，但是网页访问返回504 Gateway Time-out

## 现象
部署了一个spring boot应用， 发现访问ulr [http://jseckill.appjishu.com/seckill/list](http://jseckill.appjishu.com/seckill/list)

返回
```text
504 Gateway Time-out
nginx/1.14.0
```
如图

![](/doc/image/optimise/app-die.png)

通过ps命令，telnet命令发现进程是活的，端口27000也是通的。504 Gateway Time-out，说明进程内部处理处于卡死状态。


开始分析问题
ssh到服务器

首先tail xxx.log， 查看项目的日志，没发现任何报错。仅仅是日志在某个时间点后不再打印日志。

```shell
[root@iz8vb3nxwmck3z1ruwn8euz ~]# ps -ef|grep jseckill
root      6027  6010  0 12:07 pts/0    00:00:00 grep --color=auto jseckill
root     23910     1  0 4月22 ?       00:16:05 java -server -Xms256m -Xmx256m -Xmn64m -jar jseckill-backend.
```

说明pid=23910到jseckill进程是活的。
```shell
[root@iz8vb3nxwmck3z1ruwn8euz ~]# telnet localhost 27000
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
```
端口也是可用的

查看内存占用
```text
[root@iz8vb3nxwmck3z1ruwn8euz ~]# ps aux|grep jseckill
root      6063  0.0  0.0 112676   980 pts/0    S+   12:17   0:00 grep --color=auto jseckill
root     23910  0.0 18.8 2462676 355104 ?      Sl   4月22  16:06 java -server -Xms256m -Xmx256m -Xmn64m -jar jseckill-backend.jar
[root@iz8vb3nxwmck3z1ruwn8euz ~]# 
[root@iz8vb3nxwmck3z1ruwn8euz ~]# 
[root@iz8vb3nxwmck3z1ruwn8euz ~]# 
[root@iz8vb3nxwmck3z1ruwn8euz ~]# cat /proc/23910/status
Name:	java
Umask:	0022
State:	S (sleeping)
Tgid:	23910
Ngid:	0
Pid:	23910
PPid:	1
TracerPid:	0
Uid:	0	0	0	0
Gid:	0	0	0	0
FDSize:	4096
Groups:	0 
VmPeak:	 2462676 kB
VmSize:	 2462676 kB
VmLck:	       0 kB
VmPin:	       0 kB
VmHWM:	  366836 kB
VmRSS:	  355104 kB
RssAnon:	  341568 kB
RssFile:	   13536 kB
RssShmem:	       0 kB
VmData:	 2288832 kB
VmStk:	     132 kB
VmExe:	       4 kB
VmLib:	   17424 kB
VmPTE:	    1176 kB
VmSwap:	       0 kB
Threads:	134
SigQ:	1/7283
SigPnd:	0000000000000000
ShdPnd:	0000000000000000
SigBlk:	0000000000000000
SigIgn:	0000000000000003
SigCgt:	2000000181005ccc
CapInh:	0000000000000000
CapPrm:	0000001fffffffff
CapEff:	0000001fffffffff
CapBnd:	0000001fffffffff
CapAmb:	0000000000000000
Seccomp:	0
Cpus_allowed:	1
Cpus_allowed_list:	0
Mems_allowed:	00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000000,00000001
Mems_allowed_list:	0
voluntary_ctxt_switches:	1
nonvoluntary_ctxt_switches:	2
```
JVM启动参数是
```text
-Xms256m -Xmx256m -Xmn64m
```
这里的物理内存占用是<code>VmRSS:	  355104 kB</code> ,也就是355Mb,超过我们设置的最大堆内存256兆。

这是我的一个个人测试的小网站， 为什么会在运行十几天以后会出现这样的问题呢？

```text
[root@iz8vb3nxwmck3z1ruwn8euz ~]# top -p 23910

top - 12:31:03 up 228 days,  2:29,  2 users,  load average: 0.00, 0.01, 0.05
Tasks:   1 total,   0 running,   1 sleeping,   0 stopped,   0 zombie
%Cpu(s):  0.7 us,  0.0 sy,  0.0 ni, 99.3 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
KiB Mem :  1883492 total,    73108 free,   976080 used,   834304 buff/cache
KiB Swap:        0 total,        0 free,        0 used.   713220 avail Mem 

  PID USER      PR  NI    VIRT    RES    SHR S %CPU %MEM     TIME+ COMMAND                                                                  
23910 root      20   0 2462676 355104  13536 S  0.0 18.9  16:06.61 java
```
说明CPU占用0%, 内存占用达到了18.9%

```text
[root@iz8vb3nxwmck3z1ruwn8euz ~]# ps -mp 23910 -o THREAD,tid,time
USER     %CPU PRI SCNT WCHAN  USER SYSTEM   TID     TIME
root      0.0   -    - -         -      -     - 00:16:06
root      0.0  19    - futex_    -      - 23910 00:00:00
root      0.0  19    - futex_    -      - 23911 00:00:05
root      0.0  19    - futex_    -      - 23912 00:00:35
root      0.0  19    - futex_    -      - 23913 00:00:00
root      0.0  19    - futex_    -      - 23914 00:00:00
root      0.0  19    - futex_    -      - 23915 00:00:00
root      0.0  19    - futex_    -      - 23916 00:00:38
root      0.0  19    - futex_    -      - 23917 00:00:11
root      0.0  19    - futex_    -      - 23918 00:00:00
root      0.0  19    - futex_    -      - 23919 00:10:33
root      0.0  19    - futex_    -      - 23923 00:01:28
root      0.0  19    - futex_    -      - 23924 00:00:05
root      0.0  19    - futex_    -      - 23925 00:00:04
root      0.0  19    - futex_    -      - 23926 00:00:31
root      0.0  19    - futex_    -      - 23929 00:00:00
root      0.0  19    - futex_    -      - 23930 00:00:01
root      0.0  19    - ep_pol    -      - 23935 00:00:32
root      0.0  19    - futex_    -      - 23936 00:00:02
root      0.0  19    - futex_    -      - 23937 00:00:01
root      0.0  19    - futex_    -      - 23938 00:00:01
root      0.0  19    - futex_    -      - 23939 00:00:01
root      0.0  19    - futex_    -      - 23940 00:00:01
root      0.0  19    - futex_    -      - 23941 00:00:01
root      0.0  19    - futex_    -      - 23942 00:00:01
root      0.0  19    - futex_    -      - 23943 00:00:01
root      0.0  19    - futex_    -      - 23944 00:00:01
root      0.0  19    - futex_    -      - 23945 00:00:01
root      0.0  19    - ep_pol    -      - 23946 00:00:37
root      0.0  19    - inet_c    -      - 23947 00:00:00
root      0.0  19    - futex_    -      - 25933 00:00:00
root      0.0  19    - poll_s    -      - 26057 00:00:03
root      0.0  19    - poll_s    -      - 26058 00:00:08
root      0.0  19    - futex_    -      - 26059 00:00:04
root      0.0  19    - futex_    -      - 26060 00:00:04
root      0.0  19    - futex_    -      - 26061 00:00:00
root      0.0  19    - futex_    -      - 26176 00:00:00
root      0.0  19    - futex_    -      - 28410 00:00:00
root      0.0  19    - futex_    -      - 30412 00:00:00
root      0.0  19    - futex_    -      -  1615 00:00:00
root      0.0  19    - futex_    -      -  4431 00:00:00
root      0.0  19    - futex_    -      -  6560 00:00:00
root      0.0  19    - futex_    -      -  9029 00:00:00
root      0.0  19    - futex_    -      - 11915 00:00:00
root      0.0  19    - futex_    -      -  1772 00:00:00
root      0.0  19    - futex_    -      -  1777 00:00:00
root      0.0  19    - futex_    -      -  1778 00:00:00
root      0.0  19    - futex_    -      -  1779 00:00:00
root      0.0  19    - futex_    -      -  1788 00:00:00
root      0.0  19    - futex_    -      -  1789 00:00:00
root      0.0  19    - futex_    -      -  1793 00:00:00
root      0.0  19    - futex_    -      -  1794 00:00:00
root      0.0  19    - futex_    -      -  1802 00:00:00
root      0.0  19    - futex_    -      -  1830 00:00:00
root      0.0  19    - futex_    -      -  1835 00:00:00
root      0.0  19    - futex_    -      -  1836 00:00:00
root      0.0  19    - futex_    -      -  1874 00:00:00
root      0.0  19    - futex_    -      -  1921 00:00:00
root      0.0  19    - futex_    -      -  2858 00:00:00
root      0.0  19    - futex_    -      -  2860 00:00:00
root      0.0  19    - futex_    -      -  2879 00:00:00
root      0.0  19    - futex_    -      -  2913 00:00:00
root      0.0  19    - futex_    -      -  2953 00:00:00
root      0.0  19    - futex_    -      -  2955 00:00:00
root      0.0  19    - futex_    -      -  2956 00:00:00
root      0.0  19    - futex_    -      -  2957 00:00:00
root      0.0  19    - futex_    -      -  2990 00:00:00
root      0.0  19    - futex_    -      -  3006 00:00:00
root      0.0  19    - futex_    -      -  3007 00:00:00
root      0.0  19    - futex_    -      -  3117 00:00:00
root      0.0  19    - futex_    -      -  3126 00:00:00
root      0.0  19    - futex_    -      -  3239 00:00:00
root      0.0  19    - futex_    -      -  3293 00:00:00
root      0.0  19    - futex_    -      -  3297 00:00:00
root      0.0  19    - futex_    -      -  3314 00:00:00
root      0.0  19    - futex_    -      -  3379 00:00:00
root      0.0  19    - futex_    -      -  3380 00:00:00
root      0.0  19    - futex_    -      -  3381 00:00:00
root      0.0  19    - futex_    -      -  3386 00:00:00
root      0.0  19    - futex_    -      -  3401 00:00:00
root      0.0  19    - futex_    -      -  3402 00:00:00
root      0.0  19    - futex_    -      -  3403 00:00:00
root      0.0  19    - futex_    -      -  3405 00:00:00
root      0.0  19    - futex_    -      -  3415 00:00:00
root      0.0  19    - futex_    -      -  3421 00:00:00
root      0.0  19    - futex_    -      -  3422 00:00:00
root      0.0  19    - futex_    -      -  3467 00:00:00
root      0.0  19    - futex_    -      -  3486 00:00:00
root      0.0  19    - futex_    -      -  3490 00:00:00
root      0.0  19    - futex_    -      -  3506 00:00:00
root      0.0  19    - futex_    -      -  3507 00:00:00
root      0.0  19    - futex_    -      -  3527 00:00:00
root      0.0  19    - futex_    -      -  3528 00:00:00
root      0.0  19    - futex_    -      -  3529 00:00:00
root      0.0  19    - futex_    -      -  3600 00:00:00
root      0.0  19    - futex_    -      -  3601 00:00:00
root      0.0  19    - futex_    -      -  3616 00:00:00
root      0.0  19    - futex_    -      -  3620 00:00:00
root      0.0  19    - futex_    -      -  3621 00:00:00
root      0.0  19    - futex_    -      -  3622 00:00:00
root      0.0  19    - futex_    -      -  3623 00:00:00
root      0.0  19    - futex_    -      -  3624 00:00:00
root      0.0  19    - futex_    -      -  3646 00:00:00
root      0.0  19    - futex_    -      -  3647 00:00:00
root      0.0  19    - futex_    -      -  3662 00:00:00
root      0.0  19    - futex_    -      -  3667 00:00:00
root      0.0  19    - futex_    -      -  3679 00:00:00
root      0.0  19    - futex_    -      -  3739 00:00:00
root      0.0  19    - futex_    -      -  3740 00:00:00
root      0.0  19    - futex_    -      -  3910 00:00:00
root      0.0  19    - futex_    -      -  3916 00:00:00
root      0.0  19    - futex_    -      -  3945 00:00:00
root      0.0  19    - futex_    -      -  3946 00:00:00
root      0.0  19    - futex_    -      -  3947 00:00:00
root      0.0  19    - futex_    -      -  3998 00:00:00
root      0.0  19    - futex_    -      -  4090 00:00:00
root      0.0  19    - futex_    -      -  4098 00:00:00
root      0.0  19    - futex_    -      -  4216 00:00:00
root      0.0  19    - futex_    -      -  4675 00:00:00
root      0.0  19    - futex_    -      -  4676 00:00:00
root      0.0  19    - futex_    -      -  4708 00:00:00
root      0.0  19    - futex_    -      -  5056 00:00:00
root      0.0  19    - futex_    -      -  5058 00:00:00
root      0.0  19    - futex_    -      -  5713 00:00:00
root      0.0  19    - futex_    -      -  5720 00:00:00
root      0.0  19    - futex_    -      -  5721 00:00:00
root      0.0  19    - futex_    -      -  5748 00:00:00
root      0.0  19    - futex_    -      -  5749 00:00:00
root      0.0  19    - futex_    -      -  5832 00:00:00
root      0.0  19    - futex_    -      -  5862 00:00:00
root      0.0  19    - futex_    -      -  5917 00:00:00
root      0.0  19    - futex_    -      -  5918 00:00:00
root      0.0  19    - futex_    -      -  5974 00:00:00
root      0.0  19    - futex_    -      -  5975 00:00:00
root      0.0  19    - futex_    -      -  5979 00:00:00
```

发现这些线程对应的时间比较长，分别查一下  23919   23923

```text
[root@iz8vb3nxwmck3z1ruwn8euz ~]# printf "%x\n" 23919
5d6f
[root@iz8vb3nxwmck3z1ruwn8euz ~]# 
[root@iz8vb3nxwmck3z1ruwn8euz ~]# 
[root@iz8vb3nxwmck3z1ruwn8euz ~]# jstack 23910 |grep 5d6f -A 30
"VM Periodic Task Thread" os_prio=0 tid=0x00007fe7cc0dd800 nid=0x5d6f waiting on condition 

JNI global references: 1299


```

```text
[root@iz8vb3nxwmck3z1ruwn8euz ~]# printf "%x\n"   23923
5d73
[root@iz8vb3nxwmck3z1ruwn8euz ~]# jstack 23910 |grep 5d73 -A 30
"Catalina-utility-1" #11 prio=1 os_prio=0 tid=0x00007fe7cc896800 nid=0x5d73 waiting on condition [0x00007fe7d1239000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x00000000f4c8b920> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	at java.lang.Thread.run(Thread.java:748)

"Service Thread" #7 daemon prio=9 os_prio=0 tid=0x00007fe7cc0da800 nid=0x5d6e runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C1 CompilerThread1" #6 daemon prio=9 os_prio=0 tid=0x00007fe7cc0d7800 nid=0x5d6d waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread0" #5 daemon prio=9 os_prio=0 tid=0x00007fe7cc0d5800 nid=0x5d6c waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Signal Dispatcher" #4 daemon prio=9 os_prio=0 tid=0x00007fe7cc0d4000 nid=0x5d6b runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Finalizer" #3 daemon prio=8 os_prio=0 tid=0x00007fe7cc0a1000 nid=0x5d6a in Object.wait() [0x00007fe7d183f000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(Native Method)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
	- locked <0x00000000f403a608> (a java.lang.ref.ReferenceQueue$Lock)
```

查看young gc 和full gc (old gc)的次数和耗时
```text
[root@iz8vb3nxwmck3z1ruwn8euz stack]# jstat -gc 23910 5000
 S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT   
6528.0 6528.0  0.0   3232.5 52480.0   3731.4   196608.0   91447.1   65664.0 62871.9 7808.0 7290.0     69    0.774   3      0.265    1.038

```
由此可见
YGC     YGCT    FGC    FGCT 分别是ygc的次数，ygc消耗的总时间(单位为秒)， full gc次数和full gc总消耗的时间(单位为秒)

说明GC方面没有什么异常。

到了这里还是没发现什么问题。 

**这时候只能从线程堆栈日志里面去找答案了**

```shell
jstack 23910 >> threadstack.txt
```
这里把线程栈的日志追加到本地的一个文件里去了。 vim threadstack.txt发现里面有如下报错 
```text
"http-nio-27000-exec-2" #23 daemon prio=5 os_prio=0 tid=0x00007fe7cc7e6800 nid=0x5d81 waiting on condition [0x00007fe79ac06000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x00000000f5198220> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
        at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
        at org.apache.commons.pool2.impl.LinkedBlockingDeque.takeFirst(LinkedBlockingDeque.java:590)
        at org.apache.commons.pool2.impl.GenericObjectPool.borrowObject(GenericObjectPool.java:444)
        at org.apache.commons.pool2.impl.GenericObjectPool.borrowObject(GenericObjectPool.java:365)
        at redis.clients.util.Pool.getResource(Pool.java:49)
        at redis.clients.jedis.JedisPool.getResource(JedisPool.java:226)
        at com.liushaoming.jseckill.backend.dao.cache.RedisDAO.getAllGoods(RedisDAO.java:82)
        at com.liushaoming.jseckill.backend.dao.cache.RedisDAO$$FastClassBySpringCGLIB$$3ceb41f7.invoke(<generated>)
        at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
        at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:749)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
        at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:139)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
        at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:688)
        at com.liushaoming.jseckill.backend.dao.cache.RedisDAO$$EnhancerBySpringCGLIB$$38174f4b.getAllGoods(<generated>)
        at com.liushaoming.jseckill.backend.service.impl.SeckillServiceImpl.getSeckillList(SeckillServiceImpl.java:73)
        at com.liushaoming.jseckill.backend.service.impl.SeckillServiceImpl$$FastClassBySpringCGLIB$$85f44643.invoke(<generated>)

```
其中重要的一行是
```text
at com.liushaoming.jseckill.backend.dao.cache.RedisDAO.getAllGoods(RedisDAO.java:82)
```
找到代码的82行如下
```java
Jedis jedis = jedisPool.getResource();
```
在jedisPool.getResource()这里出了问题。
```text
at sun.misc.Unsafe.park(Native Method)
        - parking to wait for
        
        
org.apache.commons.pool2.impl.LinkedBlockingDeque.takeFirst
```
说明在这里阻塞出问题了。

查看下spring boot程序的配置文件
```text
# 连接超时时间（毫秒）
spring.redis.timeout=0

# 连接池最大连接数（使用负值表示没有限制）
spring.redis.jedis.pool.max-active=3000
# 连接池最大阻塞等待时间（使用负值表示没有限制）
spring.redis.jedis.pool.max-wait=-1
# 连接池中的最大空闲连接
spring.redis.jedis.pool.max-idle=8
# 连接池中的最小空闲连接
spring.redis.jedis.pool.min-idle=0
```

```java
    @Bean(name = "poolConfig")
    public JedisPoolConfig initJedisPoolConfig() {
        log.info("JedisPoolConfig注入开始:");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }
```
这里<code>poolConfig.setBlockWhenExhausted(true);</code> ,说明当线程池满了的时候(exhausted)的时候会阻塞jedis客户端的请求。

也就是jedisPool.getResource()会阻塞住， 具体阻塞多长时间由MaxWaitMillis来确定。 

所以，看出了问题出在一个参数的配置<code>spring.redis.jedis.pool.max-wait=-1</code> 改成自己需要的毫秒数
【连接池最大阻塞等待时间（使用负值表示没有限制）】

比如，改成2秒钟，<code>spring.redis.jedis.pool.max-wait=2000</code>

说明了问题的原因是Redis连接阻塞住了。 会导致网页访问超时。


* 另外的排错办法

  增加了一个http接口"/api/ping", 如何发现秒杀请求被卡死了，但是ping接口访问能直接返回"pong",说明整个应用程序是好的。只是秒杀的处理卡死在比如redis的处理上。这样进一步可以确定是redis的使用问题。

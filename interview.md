# Remark
假设谈话对象的身份角色,可以协助切换你的表达方式和态度  
所以,适当的假想可以减轻谈话时的怯场心理,增加表达的自信

## Q&A
LoadTimeWeaverAware
分库分表实现

## Interview
> 畅移科技 01.12 16：30
1.  主备上线切换实现  
作用: 故障机房切换 , 流量高峰动态上线  
实现: --

2. (F5+Nginx) + SpringCloud-Gateway + SpringBoot
3. JVM监控实现
4. Skywalking 监控内容
5. springboot admin

6. 服务器自动扩容

> 1药网 01.13 10：00

1. 垃圾收集算法
2. CMS & G1
3. Spring 循环依赖
4. 技术痛点 & 业务痛点

> 途虎养车 01.13 14：00
1. 管理心得
2. Eureka & Nacos 选型方案

> 酷家乐 01.14 15：00
1. cap 理论
2. 索引原则 , B+树和B树的区别
3. MVCC

4. 零钱找零算法
5. 毒药小老鼠
6. 字符串反转

> 躺平 01.25 19：00
1. 索引 rowid 查找行数据
2. 本地事务表
3. Nacos 与 Zookeeper 选型

4. 分库分表热切换
5. select for update

> 微盟 2021.02.07
1. 索引上的范围查找
2. 分库分表的数据倾斜
3. AQS 实现原理
4. Redis AOF 命令写入机制

5. Redis 为什么单线程
6. Sharding JDBC 实现原理
7. 高并发的限流问题

> 爱回收 03.02/15:00
1. 线程状态

> 中通
1. 线程切换开销
2. java8 新特性; Stream,Optional,ConsurrentHashMap 

> 阿里本地生活
1. notify 和 notifyAll 的区别
2. semaphore 和 线程池的区别
3. 聚集索引结构
4. TreeMap 有序实现
5. TopN 算法
6. CopyOnWrite

> 得物 03.04/19:00
1. Tomcat 实现原理
2. FullGC 启动时机
3. epoll 底层, 如何实现事件通知
4. rocketmq 延时消息底层实现
5. 事务消息底层
6. message key 查找实现
7. synchronized 和 reentrantlcok 实现
8. 线程池 corepool 设置
9. hystrix 限流实现
10. parallelStream 实现
11. https , tcp
12. feign 组件
13. cpu 负载查看
14. mysql user filesort use temporary
15. refreshscope 实现原理
16. eureka 多级缓存
17. 快排
18. 两数之和
19. 数据量大,内存不够怎么排序

## Redis 分布式锁可重入性实现
if setnx threadUUID
        threadlocal.put 
        dosomething
        threadlocal.remove
else get
    if threadlocal.value == threadUUID
        dosomething
    else 
        return 

分布式锁自旋 + 队列等待

## 意向
* 顺丰
* bilibili
* 得物
* 饿了么
* 快手
* 喜马拉雅

## 引申问题
1. 分库分表数据倾斜
2. Redis 热值发现

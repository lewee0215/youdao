## Q&A
LoadTimeWeaverAware

> 畅移科技 01.12 16：30
1.  主备上线切换实现  
作用: 故障机房切换 , 流量高峰动态上线  
实现: --

2. (F5+Nginx) + SpringCloud-Gateway + SpringBoot
3. JVM监控实现
4. Skywalking 监控内容
5. springboot admin

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

## 意向
* 顺丰
* bilibili
* 得物
* 饿了么
* 快手
* 喜马拉雅

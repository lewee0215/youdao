## 畅移科技 01.12 16：30
> 主备上线切换实现
作用: 故障机房切换 , 流量高峰动态上线
实现: 

(F5+Nginx) + SpringCloud-Gateway + SpringBoot

> JVM监控实现

> Skywalking 监控内容

> springboot admin

## 1药网 01.13 10：00

## 途虎养车 01.13 14：00

## 酷家乐 01.14 15：00

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
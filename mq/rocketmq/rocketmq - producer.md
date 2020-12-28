## RocketMQ Producer 

### producer.setVipChannelEnabled(true);
配置说明：是否启用vip netty通道以发送消息
默认值：-D com.rocketmq.sendMessageWithVIPChannel参数的值，若无则是true

broker的netty server会起两个通信服务。两个服务除了服务的端口号不一样，其他都一样。  
其中一个的端口（配置端口-2）作为vip通道，客户端可以启用本设置项把发送消息此vip通道

## RocketMQ Producer Group 
发送分布式事务消息时，如果Producer中途意外宕机，Broker会主动回调Producer Group内的任意一台机器来确认事务状态

## RocketMQ SendStatus：
send消息方法，只要不抛异常，就代表发送成功。但是发送成功会有多个状态，在sendResult里定义。
SEND_OK：消息发送成功
FLUSH_DISK_TIMEOUT：消息发送成功，但是服务器刷盘超时，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失
FLUSH_SLAVE_TIMEOUT：消息发送成功，但是服务器同步到Slave时超时，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失
SLAVE_NOT_AVAILABLE：消息发送成功，但是此时slave不可用，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失

### System busy & Broker busy
https://blog.csdn.net/prestigeding/article/details/109335880  

#### 1. PageCache 压力较大
判断 PageCache 是否忙的依据就是，在写入消息、向内存追加消息时加锁的时间，默认的判断标准是加锁时间超过 1s，就认为是 PageCache 压力大

#### 2. 发送线程池挤压的拒绝策略
在 RocketMQ 中处理消息发送的，是一个只有一个线程的线程池，内部会维护一个有界队列，默认长度为 1W。如果当前队列中挤压的数量超过 1w，执行线程池的拒绝策略，从而抛出 [too many requests and system thread pool busy] 错误

#### 3. Broker 端快速失败
默认情况下 Broker 端开启了快速失败机制，就是在 Broker 端还未发生 PageCache 繁忙（加锁超过 1s）的情况，但存在一些请求在消息发送队列中等待 200ms 的情况，RocketMQ 会不再继续排队，直接向客户端返回 System busy，但由于 RocketMQ 客户端目前对该错误没有进行重试处理，所以在解决这类问题的时候需要额外处理。

#### 解决方案
1. transientStorePoolEnable ， 配置文件添加：  
<code> 
transientStorePoolEnable=true  
</code>
消息先写入到堆外内存中，该内存由于启用了内存锁定机制，故消息的写入是接近直接操作内存，性能可以得到保证。  
消息进入到堆外内存后，后台会启动一个线程，一批一批将消息提交到 PageCache，即写消息时对 PageCache 的写操作由单条写入变成了批量写入，降低了对 PageCache 的压力  

2. 扩容
对集群中的 Topic 进行拆分，即将一部分 Topic 迁移到其他集群中，降低集群的负载




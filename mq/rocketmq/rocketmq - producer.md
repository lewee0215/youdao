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
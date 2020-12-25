# JMS && AMQP 协议  
AMQP中增加了Exchange和Binging的角色。生产者把消息发布到Exchange上，消息最终到达队列并被消费者接收，而Binding决定交换器的消息应该发送到哪个队列  

## JMS 规范  
https://baike.baidu.com/item/JMS/2836691?fr=aladdin  
JMS规范并不需要JMS供应商实现消息的优先级路线，但是它需要递送加快的消息优先于普通级别的消息  
JMS定义了从0到9的优先级路线级别，0是最低的优先级而9则是最高的。更特殊的是0到4是正常优先级的变化幅度，而5到9是加快的优先级的变化幅度

JMS有两种传递消息的方式。标记为NON_PERSISTENT的消息最多投递一次，而标记为PERSISTENT的消息将使用暂存后再转送的机理投递

（1）、同步消费（Synchronous）
在同步消费信息模式模式中，订阅者/接收方通过调用 receive（）方法来接收消息。在receive（）方法中，线程会阻塞直到消息到达或者到指定时间后消息仍未到达。

（2）、异步消费（Asynchronous）
使用异步方式接收消息的话，消息订阅者需注册一个消息监听者，类似于事件监听器，只要消息到达，JMS服务提供者会通过调用监听器的onMessage()递送消息。

### pub/sub
在发布者和订阅者之间存在时间依赖性。发布者需要建立一个订阅（subscription），以便客户能够订阅。订阅者必须保持持续的活动状态以接收消息，除非订阅者建立了持久的订阅。在那种情况下，在订阅者未连接时发布的消息将在订阅者重新连接时重新发布

## AMQP 协议

发布者发布消息时可以给消息指定各种消息属性（Message Meta-data）。有些属性有可能会被消息代理（Brokers）使用，然而其他的属性则是完全不透明的，它们只能被接收消息的应用所使用。

AMQP 模块包含了一个消息确认（Message Acknowledgements）机制：当一个消息从队列中投递给消费者后，不会立即从队列中删除，直到它收到来自消费者的确认回执（Acknowledgement）后，才完全从队列中删除

在某些情况下，例如当一个消息无法被成功路由时（无法从交换机分发到队列），消息或许会被返回给发布者并被丢弃。或者，如果消息代理执行了延期操作，消息会被放入一个所谓的死信队列中。此时，消息发布者可以选择某些参数来处理这些特殊情况

AMQP 提供了一个虚拟主机（virtual hosts - vhosts）的概念。这跟 Web servers 虚拟主机概念非常相似，这为 AMQP 实体提供了完全隔离的环境

## 消息类型
a)     同步消息

b)     异步消息

c)      单向消息

d)     顺序消息

e)     批量消息

f)      过滤消息

g)     事务消息


## ActiveMQ 消息阻塞、堆积、延迟
ActiveMQ 是一个完全支持JMS1.1和J2EE 1.4规范的 JMS Provider实现

### Q&A
ActiveMQ 顺序消息
ActiveMQ 持久化

## RabbitMQ 使用详解
RabbitMQ是AMQP（高级消息队列协议）的标准实现，用erlang语言开发

kafka和rabbitmq全面对比分析：
https://blog.csdn.net/myhes/article/details/83247108


## kafka 设计原则
Kafka主要特点就是基于Pull的模式来处理消息消费，追求高吞吐量，一开始的目的就是用于日志收集和传输。0.8版本开始支持复制，不支持事务，对消息的重复、丢失、错误没有严格要求，适合产生大量数据的互联网服务的数据收集业务。能够支持廉价的服务器上以每秒100k条数据的吞吐量。(有ack机制，可以保证不丢失，不能保证不重复。)

## RocketMQ 使用详解
RocketMQ 目前在阿里集团被广泛应用于交易、充值、流计算、消息推送、日志流式处理、binlog分发等场景，设计时参考了 Kafka

RocketMQ使用的消息原语是At Least Once，所以consumer可能多次收到同一个消息，此时务必做好幂等

Rocketmq相比于Rabbitmq、kafka具有主要优势特性有：
• 支持事务型消息（消息发送和DB操作保持两方的最终一致性，rabbitmq和kafka不支持）
• 支持结合rocketmq的多个系统之间数据最终一致性（多方事务，二方事务是前提）
• 支持18个级别的延迟消息（rabbitmq和kafka不支持）
• 支持指定次数和时间间隔的失败消息重发（kafka不支持，rabbitmq需要手动确认）
• 支持consumer端tag过滤，减少不必要的网络传输（rabbitmq和kafka不支持）
• 支持重复消费（rabbitmq不支持，kafka支持）

RocketMQ Send：

send消息方法，只要不抛异常，就代表发送成功。但是发送成功会有多个状态，在sendResult里定义。

SEND_OK：消息发送成功

FLUSH_DISK_TIMEOUT：消息发送成功，但是服务器刷盘超时，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失

FLUSH_SLAVE_TIMEOUT：消息发送成功，但是服务器同步到Slave时超时，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失

SLAVE_NOT_AVAILABLE：消息发送成功，但是此时slave不可用，消息已经进入服务器队列，只有此时服务器宕机，消息才会丢失


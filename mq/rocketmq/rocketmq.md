## RocketMQ 功能概览
Apache RocketMQ是一个分布式消息传递和流媒体平台，具有低延迟，高性能和可靠性，万亿级容量和灵活的可扩展性。它提供了多种功能:

发布/订阅消息模型和点对点
预定的消息传递
消息追溯性按时间或偏移量
记录流媒体的中心
大数据集成
可靠的FIFO和严格的有序消息传递在同一队列中
高效的推拉消费模式
单个队列中的百万级消息累积容量
多种消息传递协议，如JMS和OpenMessaging
灵活的分布式横向扩展部署架构
Lightning-fast批处理消息交换系统
各种消息过滤器机制，如SQL和Tag
Docker图像用于隔离测试和云隔离集群
功能丰富的管理仪表板，用于配置，指标和监控
访问控制列表
消息跟踪
上面都是官方列举出来了,还有如下特点:

支持Broker和Consumer端消息过滤
支持拉pull和推push两种消费模式,也就是上面说的推拉消费模式
支持单master节点,多master节点,多master节点多slave节点
消息失败重试机制,支持特定level的定时消息
新版本底层采用Netty

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


https://blog.csdn.net/prestigeding/article/details/78888290

前言

第1章　阅读源代码前的准备  1

1.1　获取和调试RocketMQ的源代码  1

1.1.1　Eclipse获取RocketMQ源码  2

1.1.2　Eclipse调试RocketMQ源码  9

1.1.3　IntelliJ IDEA获取RocketMQ源码  15

1.1.4　IntelliJ IDEA调试RocketMQ源码  20

1.2　RocketMQ源代码的目录结构  27

1.3　RocketMQ的设计理念和目标  28

1.3.1　设计理念  28

1.3.2　设计目标  28

第2章　RocketMQ路由中心NameServer  31

2.1　NameServer架构设计  31

2.2　NameServer启动流程  32

2.3　NameServer路由注册、故障剔除  36

2.3.1　路由元信息  36

2.3.2　路由注册  38

2.3.3　路由删除  43

2.3.4　路由发现  46

2.4　本章小结  47

第3章　RocketMQ消息发送  49

3.1　漫谈RocketMQ消息发送  49

3.2　认识RocketMQ消息  50

3.3　生产者启动流程  51

3.3.1　初识DefaultMQProducer消息发送者  51

3.3.2　消息生产者启动流程  54

3.4　消息发送基本流程  56

3.4.1　消息长度验证  56

3.4.2　查找主题路由信息  56

3.4.3　选择消息队列  60

3.4.4　消息发送  65

3.5　批量消息发送  71

3.6　本章小结  74

第4章　RocketMQ消息存储  75

4.1　存储概要设计  75

4.2　初识消息存储  76

4.3　消息发送存储流程  78

4.4　存储文件组织与内存映射  83

4.4.1　MappedFileQueue映射文件队列  84

4.4.2　MappedFile内存映射文件  87

4.4.3　TransientStorePool  93

4.5　RocketMQ存储文件  94

4.5.1　Commitlog文件  95

4.5.2　ConsumeQueue文件  97

4.5.3　Index索引文件  100

4.5.4　checkpoint文件  104

4.6　实时更新消息消费队列与索引文件  105

4.6.1　根据消息更新ConumeQueue  107

4.6.2　根据消息更新Index索引文件  108

4.7　消息队列与索引文件恢复  109

4.7.1　Broker正常停止文件恢复  112

4.7.2　Broker异常停止文件恢复  114

4.8　文件刷盘机制  115

4.8.1　Broker同步刷盘  116

4.8.2　Broker异步刷盘  119

4.9　过期文件删除机制  122

4.10　本章小结  126

第5章　RocketMQ消息消费  127

5.1　RocketMQ消息消费概述  127

5.2　消息消费者初探  128

5.3　消费者启动流程  130

5.4　消息拉取  133

5.4.1　PullMessageService实现机制  133

5.4.2　ProcessQueue实现机制  136

5.4.3　消息拉取基本流程  138

5.5　消息队列负载与重新分布机制  154

5.6　消息消费过程  162

5.6.1　消息消费  163

5.6.2　消息确认(ACK)  167

5.6.3　消费进度管理  171

5.7　定时消息机制  176

5.7.1　load方法  177

5.7.2　start方法  178

5.7.3　定时调度逻辑  179

5.8　消息过滤机制  181

5.9　顺序消息  186

5.9.1　消息队列负载  187

5.9.2　消息拉取  187

5.9.3　消息消费  188

5.9.4　消息队列锁实现  195

5.10　本章小结  196

第6章　消息过滤FilterServer  198

6.1　ClassFilter运行机制  198

6.2　FilterServer注册剖析  199

6.3　类过滤模式订阅机制  202

6.4　消息拉取  205

6.5　本章小结  206

第7章　RocketMQ主从同步(HA)机制  207

7.1　RocketMQ主从复制原理  207

7.1.1　HAService整体工作机制  208

7.1.2　AcceptSocketService实现原理  208

7.1.3　GroupTransferService实现原理  210

7.1.4　HAClient实现原理  211

7.1.5　HAConnection实现原理  214

7.2　RocketMQ读写分离机制  220

7.3　本章小结  223

第8章　RocketMQ事务消息  225

8.1　事务消息实现思想  225

8.2　事务消息发送流程  226

8.3　提交或回滚事务  232

8.4　事务消息回查事务状态  233

8.5　本章小结  240

第9章　RocketMQ实战  242

9.1　消息批量发送  242

9.2　消息发送队列自选择  243

9.3　消息过滤  243

9.3.1　TAG模式过滤  244

9.3.2　SQL表达模式过滤  244

9.3.3　类过滤模式  245

9.4　事务消息  247

9.5　Spring整合RocketMQ  250

9.6　Spring Cloud整合RocketMQ  251

9.7　RocketMQ监控与运维命令  258

9.7.1　RocktetMQ监控平台搭建  258

9.7.2　RocketMQ管理命令  261

9.8　应用场景分析  280

9.9　本章小结  281

附录A　参数说明  282
# RocketMQ 消息持久化
https://www.jianshu.com/p/2c904cc42d95 

Broker 文件存储结构：
https://www.cnblogs.com/toUpdating/p/10019671.html
![alt text](https://img2018.cnblogs.com/blog/1399187/201811/1399187-20181126215917123-1722887406.png "CommitLog中的存储格式")
字段名称 | 字段描述
:-- | :--
abort| RocketMQ 启动时生成，正常关闭时删除，如果启动时存在该文件，代表 RocketMQ 被异常关闭  
checkpoint| 文件检查点，存储 commitlog 、consumequeue、indexfile 最后一次刷盘时间或时间戳  
index| 消息索引文件存储目录  
consumequeue| 消息消费队列存储目录  
commitlog| 消息存储目录  
config| 运行时的配置信息，包含主席消息过滤信息、集群消费,模式消息消费进度、延迟消息队列拉取进度、消息消费组配置信息、topic配置属性等  

## CommitLog
https://zhuanlan.zhihu.com/p/92125985  
RocketMQ将所有消息存储在一起，以顺序IO的方式写入磁盘，充分利用了磁盘顺序写减少了IO争用提高数据存储的性能

CommitLog.this.topicQueueTable.put(key, queueOffset)，其中的key是 topic-queueId, queueOffset是当前这个key中的消息数，每增加一个消息增加一(不会自减)；

CommitLog的清理机制：  
● 按时间清理，rocketmq默认会清理3天前的commitLog文件；  
● 按磁盘水位清理：当磁盘使用量到达磁盘容量75%，开始清理最老的commitLog文件。

![alt text](https://pic4.zhimg.com/80/v2-cbf1c787be956417cf2427b23e643eef_1440w.jpg "CommitLog中的存储格式")

字段名称 | 字段描述
:-- | :--
msgLen |  4字节表示消息的长度，消息的长度是整个消息体所占用的字节数的大小  
magicCode | 4字节的魔数，是固定值，有MESSAGE_MAGIC_CODE和BLANK_MAGIC_CODE
bodyCRC | 4字节的CRC，是消息体的校验码，用于防止网络、硬件等故障导致数据与发送时不一样带来的问题
queueId | 4字节的queueId，表示消息发到了哪个MessageQueue(逻辑上相当于kakka的partition)
flag|4字节的flag，flag是创建Message对象时由生产者通过构造器设定的flag值
queueOffset8|字节的queueOffset，表示在queue中的偏移量
physicalPosition|8字节的physicalPosition，表示在存储文件中的偏移量
sysFlag|4字节sysFlag，是生产者相关的信息标识，具体生产逻辑可以看相关代码
msg born timestamp| 8字节消息创建时间
msg host|8字节消息生产者的host
store timestampe| 8字节消息存储时间
store host| 8字节消息存储的机器的host
reconsume times |4字节表示重复消费次数
prepare transaction offset | 8字节消息事务相关偏移量
body length | 4字节表示消息体的长度
msg body | 消息休，不是固定长度，和前面的4字节的消息体长度值相等
topic length | 1字节表示topic的长度，因此topc的长度最多不能超过127个字节，超过的话存储会出错（有前置校验）
Topic|存储topic，因为topic不是固定长度，所以这里所占的字节是不固定的，和前一个表示topic长度的字节的值相等
properties length |2字节properties的长度，properties是创建消息时添加到消息中的，因此，添加在消息中的poperties不能太多太大，所有的properties的kv对在拼接成string后，所占的字节数不能超过2^15-1
Properties|Properties的内容，也不是固定长度，和前面的2字节properties长度的值相同  

<br/>

## ConsumeQueue
ConsumerQueue相当于CommitLog的索引文件，消费者消费时会先从ConsumerQueue中查找消息的在commitLog中的offset，再去CommitLog中找元数据。
如果某个消息只在CommitLog中有数据，没在ConsumerQueue中， 则消费者无法消费，Rocktet的事务消息就是这个原理
Consumequeue类对应的是每个topic和queuId下面的所有文件，相当于字典的目录用来指定消息在消息的真正的物理文件commitLog上的位置
消息的起始物理偏移量physical offset(long 8字节)+消息大小size(int 4字节)+tagsCode(long 8字节)

● 每个topic下的每个queue都有一个对应的consumequeue文件。
● 文件默认存储路径：${user.home} \store\consumequeue\${topicName}\${queueId}\${fileName}
● 每个文件由30W条数据组成，每条数据的大小为20个字节，从而每个文件的默认大小为600万个字节（consume queue中存储单元是一个20字节定长的数据）是顺序写顺序读

ConsumeQueue中只存储路由到该queue中的消息在CommitLog中的offset，消息的大小以及消息所属的tag的hash（tagCode）
![alt text](https://pic4.zhimg.com/80/v2-14002dfc29ac5d12b0109c8f80ade4e7_1440w.jpg "CommitLog中的存储格式")

## Checkpoint 文件
记录 CommitLog，ConsumeQueue，IndexFile 的刷盘时间点，文件固定长度为 4k，其中只用该文件的前 24个字节
physicMsgTimestamp : commitlog 文件刷盘时间点(8字节)  
logicsMsgTimestamp ： 消息消费队列文件刷盘时间点(8字节) 
indexMsgTimestamp ： 索引文件刷盘时间点(8字节) 

## Index 索引文件
https://blog.csdn.net/Nuan_Feng/article/details/108328883
ConsumerQueue是通过偏移量offset去CommitLog文件中查找消息，但实际工作应用中，我们想查找某条具体的消息，并不知道offset值

index log索引文件使用的是hash存储机制, key通过 (topic+key)%槽位数得到,value为commitlog的物理偏移量phyOffset

![alt text](https://img-blog.csdnimg.cn/2020082820415171.png "CommitLog中的存储格式")

### IndexHead：
字段名称 | 字段描述
:-- | :--
beginTimestamp|该索引文件包含消息的最小存储时间  
endTimestamp|该索引文件包含消息的最大存储时间  
beginPhyoffset|该索引文件中包含消息的最小物理偏移量（commitlog 文件偏移量）  
endPhyoffset|该索引文件中包含消息的最大物理偏移量（commitlog 文件偏移量）  
hashSlotCount|hashslot个数，并不是 hash 槽使用的个数，在这里意义不大，  
indexCount|已使用的 Index 条目个数  
<br/>

### Hash 槽：
一个 IndexFile 默认包含 500W 个 Hash 槽，每个 Hash 槽存储的是落在该 Hash 槽的 hashcode 最新的 Index 的索引

### Index 条目列表

字段名称 | 字段描述
:-- | :--
hashcode|key 的 hashcode  
phyoffset|消息对应的物理偏移量  
timedif|该消息存储时间与第一条消息的时间戳的差值，小于 0 表示该消息无效  
preIndexNo|该条目的前一条记录的 Index 索引，hash 冲突时，根据该值构建链表结构  
<br/>
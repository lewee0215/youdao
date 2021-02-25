## Redis 单线程实现
https://blog.csdn.net/xlgen157387/article/details/79470556  
因为Redis是基于内存的操作，CPU不是Redis的瓶颈，Redis的瓶颈最有可能是机器内存的大小或者网络带宽

![](https://img-blog.csdn.net/20180307162652293?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDg3MDUxOA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
参考：https://redis.io/topics/faq

多线程使用的时候，可以充分利用起所有的cpu核心，比如说4个核心的cpu,开4个线程的时候，可以同时跑4个线程的运算任务，此时是最大效率，因为实际使用上CPU不是Redis的瓶颈，单线程则成为最佳选择

## 哨兵，集群，分布式锁，延时队列，位图，HyperLogLog，布隆过滤器，限流，GeoHash（附近的人）

SDS动态字符，链表，字典，跳跃表么？以及相关的底层衍生比如字典的渐进式hash，集合的升级降级么

String、Hash、List、Set、SortedSet
HyperLogLog、Geo、Pub/Sub

brpop & blpop # 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
brpoplpush # 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止


记录帖子的点赞数、评论数和点击数 (hash)。
记录用户的帖子 ID 列表 (排序)，便于快速显示用户的帖子列表 (zset)。
记录帖子的标题、摘要、作者和封面信息，用于列表页展示 (hash)。
记录帖子的点赞用户 ID 列表，评论 ID 列表，用于显示和去重计数 (zset)。
缓存近期热帖内容 (帖子内容空间占用比较大)，减少数据库压力 (hash)。
记录帖子的相关文章 ID，根据内容推荐相关帖子 (list)。
如果帖子 ID 是整数自增的，可以使用 Redis 来分配帖子 ID(计数器)。
收藏集和帖子之间的关系 (zset)。
记录热榜帖子 ID 列表，总热榜和分类热榜 (zset)。
缓存用户行为历史，进行恶意行为过滤 (zset,hash)。
数据推送去重Bloom filter
pv，uv统计

分布式锁

延时队列

位图bitmap

HyperLogLog

布隆过滤器

# Redis 一致性策略
http://doc.redisfans.com/topic/cluster-tutorial.html#redis-guarantee  
Redis 集群不保证数据的强一致性（strong consistency）： 在特定条件下， Redis 集群可能会丢失已经被执行过的写命令。

使用异步复制（asynchronous replication）是 Redis 集群可能会丢失写命令的其中一个原因。 考虑以下这个写命令的例子：

客户端向主节点 B 发送一条写命令。
主节点 B 执行写命令，并向客户端返回命令回复。
主节点 B 将刚刚执行的写命令复制给它的从节点 B1 、 B2 和 B3 

redis multi 和 lua 脚本执行的区别

redis 请求队列实现

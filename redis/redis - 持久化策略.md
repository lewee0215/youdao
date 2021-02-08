# Redis 持久化策略
https://baijiahao.baidu.com/s?id=1654694618189745916&wfr=spider&for=pc  

redis有两种持久化模式，第一种是SNAPSHOTTING模式，还是一种是AOF模式
在默认情况下，Redis的AOF持久化方式是每秒写一次磁盘（即执行fsync）

可以通过配置文件来指定它们中的一种，或者同时使用它们(不建议同时使用)，或者全部禁用

> 在架构良好的环境中，master通常使用AOF，slave使用snapshot  

主要原因是master需要首先确保数据完整性，它作为数据备份的第一选择；slave提供只读服务(目前slave只能提供读取服务)，它的主要目的就是快速响应客户端read请求；
如果你的redis运行在网络稳定性差/物理环境糟糕情况下，建议你master和slave均采取AOF，这个在master和slave角色切换时，可以减少“人工数据备份”/“人工引导数据恢复”的时间成本；
如果你的环境一切非常良好，且服务需要接收密集性的write操作，那么建议master采取snapshot，而slave采用AOF。

## RDB 快照持久化

RDB方式，是将redis某一时刻的数据持久化到磁盘中，是一种快照式的持久化方法。  

redis在进行数据持久化的过程中，会先将数据写入到一个临时文件中，待持久化过程都结束了，才会用这个临时文件替换上次持久化好的文件。正是这种特性，让我们可以随时来进行备份，因为快照文件总是完整可用的。

对于RDB方式，redis会单独创建（fork）一个子进程来进行持久化，而主进程是不会进行任何IO操作的，这样就确保了redis极高的性能。
如果需要进行大规模数据的恢复，且对于数据恢复的完整性不是非常敏感，那RDB方式要比AOF方式更加的高效。

虽然RDB有不少优点，但它的缺点也是不容忽视的。如果你对数据的完整性非常敏感，那么RDB方式就不太适合你，因为即使你每5分钟都持久化一次，当redis故障时，仍然会有近5分钟的数据丢失。所以，redis还提供了另一种持久化方式，那就是AOF。

```conf
#dbfilename：持久化数据存储在本地的文件
dbfilename dump.rdb

#dir：持久化数据存储在本地的路径，如果是在/redis/redis-3.0.6/src下启动的redis-cli，则数据会存储在当前src目录下
dir ./

##snapshot触发的时机，save <seconds> <changes>  
##如下为900秒后，至少有一个变更操作，才会snapshot  
##对于此值的设置，需要谨慎，评估系统的变更操作密集程度  
##可以通过“save “””来关闭snapshot功能  
#save时间，以下分别表示更改了1个key时间隔900s进行持久化存储；更改了10个key300s进行存储；更改10000个key60s进行存储。
save 900 1
save 300 10
save 60 10000

##当snapshot时出现错误无法继续时，是否阻塞客户端“变更操作”，“错误”可能因为磁盘已满/磁盘故障/OS级别异常等  
stop-writes-on-bgsave-error yes  

##是否启用rdb文件压缩，默认为“yes”，压缩往往意味着“额外的cpu消耗”，同时也意味这较小的文件尺寸以及较短的网络传输时间  
rdbcompression yes 
```
### dirty 计数器和lastsave 属性
https://www.cnblogs.com/ysocean/p/9114268.html

dirty 计数器记录距离上一次成功执行 save 命令或者 bgsave 命令之后，Redis服务器进行了多少次修改（包括写入、删除、更新等操作）。

lastsave 属性是一个时间戳，记录上一次成功执行 save 命令或者 bgsave 命令的时间。

当服务器成功执行一次修改操作，那么dirty 计数器就会加 1，而lastsave 属性记录上一次执行save或bgsave的时间，Redis 服务器还有一个周期性操作函数 severCron ,默认每隔 100 毫秒就会执行一次，该函数会遍历并检查 saveparams 数组中的所有保存条件，只要有一个条件被满足，那么就会执行 bgsave 命令。

执行完成之后，dirty 计数器更新为 0 ，lastsave 也更新为执行命令的完成时间

### RDB 相关命令
https://www.jianshu.com/p/d3ba7b8ad964  
·save命令：阻塞当前Redis服务器，Redis不能处理其他命令，直到RDB过程完成为止，对于内存比较大的实例会造成长时间阻塞，线上环境不建议使用
·bgsave命令：Redis进程执行fork操作创建子进程，RDB持久化过程由子 进程负责，完成后自动结束。阻塞只发生在fork阶段，一般时间很短

## bgsave 执行流程
https://blog.csdn.net/zmflying8177/article/details/103500645  

1. Redis创建子进程以后，根本不进行数据的copy，主进程与子线程是共享数据的。主进程继续对外提供读写服务。
2. 虽然不copy数据，但是kernel会把主进程中的所有内存页的权限都设为read-only，主进程和子进程访问数据的指针都指向同一内存地址。
3. 主进程发生写操作时，因为权限已经设置为read-only了，所以会触发页异常中断（page-fault）。在中断处理中，需要被写入的内存页面会复制一份，复制出来的旧数据交给子进程使用，然后主进程该干啥就干啥。

也就是说，在进行IO操作写盘的过程中（on write），对于没有改变的数据，主进程和子进程资源共享；  
只有在出现了需要变更的数据时（写脏的数据），才进行copy操作。  


在最理想的情况下，也就是生成RDB文件的过程中，一直没有写操作的话，就根本不会发生内存的额外占用。

当然，仍然需要合理配置Linux的内存分配策略。避免在写操作过于集中时，发生因为物理内存不足导致fork失败的情况

## AOF 持久化
aof是redis的一种记录数据库写操作的持久化方案，他会忠实的记录所有的写操作，并且以redis协议的格式存储在一个.aof文件中，在重启redis的时候，redis可以根据.aof文件的内容来恢复数据集。

https://www.jianshu.com/p/d3ba7b8ad964  
AOF的工作流程操作：命令写入 （append）、文件同步（sync）、文件重写（rewrite）、重启加载 （load）

```conf
##此选项为aof功能的开关，默认为“no”，可以通过“yes”来开启aof功能  
##只有在“yes”下，aof重写/文件同步等特性才会生效  
appendonly yes  

##指定aof文件名称  
appendfilename appendonly.aof  
##指定aof操作中文件同步策略，有三个合法值：always everysec no,默认为everysec  
appendfsync everysec  

# always：每一条aof记录都立即同步到文件，这是最安全的方式，也以为更多的磁盘操作和阻塞延迟，是IO开支较大。
# everysec：每秒同步一次，性能和安全都比较中庸的方式，也是redis推荐的方式。如果遇到物理服务器故障，有可能导致最近一秒内aof记录丢失(可能为部分丢失)。
# no：redis并不直接调用文件同步，而是交给操作系统来处理，操作系统可以根据buffer填充情况/通道空闲时间等择机触发同步；这是一种普通的文件操作方式。性能较好，在物理服务器故障时，数据丢失量会因OS配置有关。

##在aof-rewrite期间，appendfsync是否暂缓文件同步，"no"表示“不暂缓”，“yes”表示“暂缓”，默认为“no”  
no-appendfsync-on-rewrite no  

##aof文件rewrite触发的最小文件尺寸(mb,gb),只有大于此aof文件大于此尺寸是才会触发rewrite，默认“64mb”，建议“512mb”  
auto-aof-rewrite-min-size 64mb  

##相对于“上一次”rewrite，本次rewrite触发时aof文件应该增长的百分比。  
##每一次rewrite之后，redis都会记录下此时“新aof”文件的大小(例如A)，那么当aof文件增长到A*(1 + p)之后  
##触发下一次rewrite，每一次aof记录的添加，都会检测当前aof文件的尺寸。  
auto-aof-rewrite-percentage 100 
```
### AOF 相关命令
AOF重写过程可以手动触发和自动触发：  
·手动触发：直接调用bgrewriteaof命令

## AOF 命令执行流程
https://www.jianshu.com/p/d3ba7b8ad964
> 1.所有的写入命令会追加到aof_buf（缓冲区）中。

> 2.AOF缓冲区根据对应的策略向硬盘做同步操作。

AOF把命令追加到aof_buf中原因： Redis使用单线程响应命令，如 果每次写AOF文件命令都直接追加到硬盘，那么性能完全取决于当前硬盘负载。
先写入缓冲区aof_buf中，还有另一个好处，Redis可以提供多种缓冲区同步硬盘的策略，在性能和安全性方面做出平衡

> 3.随着AOF文件越来越大，需要定期对AOF文件进行重写，达到压缩的目的。

重写后的AOF文件可以变小,有如下原因：

1. 进程内已经超时的数据不再写入文件。

2. 旧的AOF文件含有无效命令，如del key1、hdel key2、srem keys、set a111、set a222等。  
重写使用进程内数据直接生成，这样新的AOF文件只保留最终数据的写入命令。

3. 多条写命令可以合并为一个，如：lpush list a、lpush list b、lpush list c可以转化为：lpush list a b c。为了防止单条命令过大造成客户端缓冲区溢 出，对于list、set、hash、zset等类型操作，以64个元素为界拆分为多条。

AOF重写降低了文件占用空间，除此之外，另一个目的是：更小的AOF 文件可以更快地被Redis加载

> 4.当Redis服务器重启时，可以加载AOF文件进行数据恢复

## AOF 文件进行系统恢复
1.备份被写坏的AOF文件  
2.运行redis-check-aof –fix进行修复  
3.用diff -u来看下两个文件的差异，确认问题点  
4.重启redis，加载修复后的AOF文件  

## AOF 文件格式
https://blog.csdn.net/laiconglin/article/details/6641844  

*<count>    // <count>表示该命令有2个参数  
$<len>     	// <len>表示第1个参数的长度  
<content>   // <content>表示第1个参数的内容  
$<len>     	// <len>表示第2个参数的长度  
<content>   // <content>表示第2个参数的内容  
$<len>     	// <len>表示第3个参数的长度  
<content>   // <content>表示第3个参数的内容  

文件格式示例:
*3          // 接下来的一条命令有3个参数  
$3         	// 第一条命令的长度为3  
SET       	// 第一个参数  
$5          // 第二条命令的长度为3  
mystr     	// 第二个参数  
$13         // 第三个参数的长度为13  
this is redis // 第三个参数  



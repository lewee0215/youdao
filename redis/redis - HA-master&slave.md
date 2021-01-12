# Redis - 主从架构 Master&Slave
当用Redis做Master-slave的高可用方案时，假如master宕机了，Redis本身(包括它的很多客户端)都没有实现自动进行主备切换  
\## 不仅主服务器可以有从服务器， 从服务器也可以有自己的从服务器， 多个从服务器之间可以构成一个图状结构

## 主从同步流程
https://blog.csdn.net/u010648555/article/details/79427606
1. 当设置好slave服务器后,slave会建立和master的连接，然后发送sync命令。
2. Master接到命令启动后台的存盘进程，同时将快照期间接收到的修改命令缓存至内存，在后台进程执行完毕之后，master将传送整个数据文件到slave,以完成一次完全同步。
3. 全量复制：而slave服务在接收到数据库文件数据后，将其存盘并加载到内存中。
4. 增量复制：当Salve完成数据快照的恢复后,Master继续将新的所有收集到的修改命令依次传给slave,完成同步。
5. 但是只要是重新连接master,一次完全同步（全量复制)将被自动执行

slaveof 命令执行流程
![alt text](https://img-blog.csdn.net/20170910152114918?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvc2sxOTkwNDg=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast "title")

### PSync
https://my.oschina.net/andylucc/blog/686892  
Redis在2.8版本提供了PSYNC命令来带代替SYNC命令，为Redis主从复制提供了部分复制的能力
```java
PSYNC <runid> <offset>
runid:主服务器ID
offset:从服务器最后接收命令的偏移量

// 每个Redis服务器都会有一个表明自己身份的ID。在PSYNC中发送的这个ID是指之前连接的Master的ID，如果没保存这个ID，PSYNC的命令会使用”PSYNC ? -1” 这种形式发送给Master，表示需要全量复制

// 如果runid和本机id不一致或者双方offset差距超过了复制积压缓冲区大小，那么就会返回 FULLRESYNC runid offset，Slave将runid保存起来，并进行完整同步
```

当Master进行命令传播时，不仅将命令发送给所有Slave，还会将命令写入到复制积压缓冲区(repl_backlog)里面
https://www.cnblogs.com/aquester/p/10978366.html  
REdis的主节点创建和维护一个环形缓冲复制队列（即repl_backlog）, 并且主节点只有一个repl_backlog，所有从节点共享

https://blog.csdn.net/sk199048/article/details/77922589  
1. 从服务器在与主服务器出现网络瞬断之后，从服务器会尝试再次与主服务器连接，一旦连接成功，Slave服务器就会把“runid”和“replication offset”发送出去。  
2. 主服务器接收到这样的同步请求后，首先会验证主服务器runid是否和自己的runid是否匹配，其次会检查“请求的偏移位置”是否存在于自己的缓冲区 repl_backlog 中，如果两者都满足的话，主服务器就会向从服务器发送增量内容

### Redis 主从复制问题点
* IO剧增
每次slave断开以后（无论是主动断开，还是网路故障）再连接master都要将master全部dump出来rdb，在aof，即同步的过程都要重新执行一遍；所以要记住多台slave不要一下都启动起来，否则master可能IO剧增（间隔1-2分）

* 复制延迟
由于所有的写操作都是先在Master上操作，然后同步更新到Slave上，所以从Master同步到Slave机器有一定的延迟，当系统很繁忙的时候，延迟问题会更加严重，Slave机器数量的增加也会使这个问题更加严重。

* 可用性不高
当有主节点发生异常情况，就会导致不能写入
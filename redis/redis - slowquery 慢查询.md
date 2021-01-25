# Redis慢查询日志
https://blog.csdn.net/tx542009/article/details/87856930  
Redis慢查询日志是一个记录超过指定执行时间的查询的系统。 这里的执行时间不包括IO操作，比如与客户端通信，发送回复等等，而只是实际执行命令所需的时间

## 慢查询的配置参数
slowlog-max-len: 慢查询日志的长度  
slowlog-log-slower-than: 超时时长（单位：微秒）  

```redis
127.0.0.1:6379> config get slowlog-max-len
1) "slowlog-max-len"
2) "128"
127.0.0.1:6379> config get slowlog-log-slower-than
1) "slowlog-log-slower-than"
2) "10000"
127.0.0.1:6379> 
```

### 》 slowlog-max-len
slowlog-max-len 是慢查询日志的长度。实际上，Redis使用了一个列表来存储慢查询日志。Redis 使用了一个List 实现了一个先进先出的队列。当 第三阶段 执行的命令符合慢查询设置的时间，那么这个命令就会被插入到这个队列当中。这个 List 是一个固定的长度，其次是保存在内存当中  

慢查询 是基于内存的，一旦重启后，里面的数据将会丢失

### 》 slowlog-log-slower-than
slowlog-log-slower-than就是那个预设阈值，它的单位是微秒（1秒=1000毫秒=1000000微秒），默认值10000。  
slowlog-log-slower-than 告诉Redis命令的执行时间超过多少微秒将会被记录。 请注意，使用负数将会关闭慢查询日志，而值为0将强制记录每一个命令。  
slowlog-log-slower-than=0，那么系统会记录所有的命令；如果slowlog-log-slower-than<0，那么对任何命令都不会记录


# Redis数据过期策略详解
https://www.cnblogs.com/xuliangxing/p/7151812.html
https://blog.csdn.net/zlfprogram/article/details/74626384

> 1.过期时间跟着key走，与值无关  

在Redis中，带有过期时间的key被称为『易失的』(volatile)。 过期时间可以通过使用 DEL命令来删除整个key来移除，或者被 SET和 GETSET命令覆写(overwrite)，这意味着，如果一个命令只是修改(alter)一个带过期时间的 key的值而不是用一个新的 key值来代替(replace)它的话，那么过期时间不会被改变。比如说，对一个 key执行 INCR命令，对一个列表进行 LPUSH命令，或者对一个哈希表执行 HSET命令，这类操作都不会修改 key本身的过期时间。

> 2.设置永久有效期 
使用PERSIST命令可以清除超时，使其变成一个永久的key。

Redis并没采用 定时删除 的策略，而是采用 定期删除 和 惰性删除 混合方式。
惰性删除：惰性删除策略不会在键过期的时候立马删除，而是当外部指令获取这个键的时候才会主动删除。处理过程为：接收get执行、判断是否过期（这里按过期判断）、执行删除操作、返回nil（空）。

*定时删除*： 在设置键的过期时间的时候创建一个定时器，当过期时间到的时候立马执行删除操作。不过这种处理方式是即时的，不管这个时间内有多少过期键，不管服务器现在的运行状况，都会立马执行，所以对CPU不是很友好。

*定期删除*： 定期删除是设置一个时间间隔，每个时间段都会检测是否有过期键，如果有执行删除操作。具体就是Redis每秒10次做的事情：   
1).测试随机的20个keys进行相关过期检测。   
2).删除所有已经过期的keys。   
3).如果有多于25%的keys过期，重复步奏1.   

这是一个平凡的概率算法，基本上的假设是，我们的样本是这个密钥控件，并且我们不断重复过期检测，直到过期的keys的百分比低于25%,这意味着，在任何给定的时刻，最多会清除1/4的过期keys。

## maxmemory-policy 六种方式
https://github.com/sohutv/cachecloud/wiki/6.%E5%B8%B8%E8%A7%81%E6%A6%82%E5%BF%B5%E5%92%8C%E9%97%AE%E9%A2%98  

1. volatile-lru：只对设置了过期时间的key进行LRU（默认值） 
2. allkeys-lru ： 删除lru算法的key   
3. volatile-random：随机删除即将过期key   
4. allkeys-random：随机删除   
5. volatile-ttl ： 删除即将过期的   
6. noeviction ： 永不过期，返回错误








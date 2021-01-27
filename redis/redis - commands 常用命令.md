## Redis Cluster redis-trib 命令
当我们有了六个正在运行中的 Redis 实例， 接下来我们需要使用这些实例来创建集群， 并为每个节点编写配置文件。
通过使用 Redis 集群命令行工具 redis-trib ， 编写节点配置文件的工作可以非常容易地完成： redis-trib 位于 Redis 源码的 src 文件夹中， 它是一个 Ruby 程序， 这个程序通过向实例发送特殊命令来完成创建新集群， 检查集群， 或者对集群进行重新分片（reshared）等工作。


## cluster meet
https://cloud.tencent.com/developer/section/1374001  
> CLUSTER MEET ip port  

参数说明：CLUSTER MEET 用于将启用了群集支持的不同 Redis 节点连接到工作群集  
返回值: OK如果命令成功。如果指定的地址或端口无效，则返回错误  
# Nacos Raft 一致性算法
https://www.cnblogs.com/lucky-yqy/p/14001329.html   
Raft 算法不是强一致性算法，是最终一致性算法 （基于Paxos算法的变种有：ZAB、Raft）

## 日志复制
1. 所有的写请求都交给领导者，将请求操作写入日志，标记该状态为未提交状态。
2. 为了提交该日志，领导者就会将日志以心跳形式发送给其他跟随者，只要满足过半的跟随者可以写入该数据，则直接通知其他节点同步该数据，这个过程称为日志复制

## Nacos - Raft 源码
https://blog.csdn.net/liyanan21/article/details/89320872
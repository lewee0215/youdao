# Redis Sentinel 协议
https://blog.csdn.net/weixin_38071106/article/details/88033764  

## Sentinel的选举流程
Sentinel集群正常运行的时候每个节点epoch相同，当需要故障转移的时候会在集群中选出Leader执行故障转移操作。Sentinel采用了Raft协议实现了Sentinel间选举Leader的算法  

Sentinel集群运行过程中故障转移完成，所有Sentinel又会恢复平等。Leader仅仅是故障转移操作出现的角色

1. 某个Sentinel认定master客观下线的节点后，该Sentinel会先看看自己有没有投过票，如果自己已经投过票给其他Sentinel了，在2倍故障转移的超时时间自己就不会成为Leader。相当于它是一个Follower。
2. 如果该Sentinel还没投过票，那么它就成为Candidate。
3. 和Raft协议描述的一样，成为Candidate，Sentinel需要完成几件事情
1）更新故障转移状态为start
2）当前epoch加1，相当于进入一个新term，在Sentinel中epoch就是Raft协议中的term。
3）更新自己的超时时间为当前时间随机加上一段时间，随机时间为1s内的随机毫秒数。
4）向其他节点发送is-master-down-by-addr命令请求投票。命令会带上自己的epoch。
5）给自己投一票，在Sentinel中，投票的方式是把自己master结构体里的leader和leader_epoch改成投给的Sentinel和它的epoch。
4. 其他Sentinel会收到Candidate的is-master-down-by-addr命令。如果Sentinel当前epoch和Candidate传给他的epoch一样，说明他已经把自己master结构体里的leader和leader_epoch改成其他Candidate，相当于把票投给了其他Candidate。投过票给别的Sentinel后，在当前epoch内自己就只能成为Follower。
5. Candidate会不断的统计自己的票数，直到他发现认同他成为Leader的票数超过一半而且超过它配置的quorum（quorum可以参考《redis sentinel设计与实现》）。Sentinel比Raft协议增加了quorum，这样一个Sentinel能否当选Leader还取决于它配置的quorum。
6. 如果在一个选举时间内，Candidate没有获得超过一半且超过它配置的quorum的票数，自己的这次选举就失败了。
7. 如果在一个epoch内，没有一个Candidate获得更多的票数。那么等待超过2倍故障转移的超时时间后，Candidate增加epoch重新投票。
8. 如果某个Candidate获得超过一半且超过它配置的quorum的票数，那么它就成为了Leader。
9. 与Raft协议不同，Leader并不会把自己成为Leader的消息发给其他Sentinel。其他Sentinel等待Leader从slave选出master后，检测到新的master正常工作后，就会去掉客观下线的标识，从而不需要进入故障转移流程
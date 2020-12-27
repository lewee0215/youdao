## Rebalance的平衡粒度
https://www.jianshu.com/p/6c2cb0d2bfcd  
Rebalance是针对Topic+ConsumerGroup进行Rebalance的

创建comsumer过程中会订阅topic（包括%retry%consumerGroup），Rebalance就是要这些Topic下的所有messageQueue按照一定的规则分发给consumerGroup下的consumer进行消费  


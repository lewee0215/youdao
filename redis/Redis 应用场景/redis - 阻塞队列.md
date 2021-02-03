# Redis - 阻塞队列
https://www.cnblogs.com/vcmq/p/13509269.html

## 1. LPUSH + 自旋 RPOP
使用LPUSH生产消息，然后 while(true) 中通过RPOP消费消息

## 2. LPUSH + BRPOP
通过 LPUSH生产消息，然后通过 BRPOP 进行阻塞地等待并消费消息  
如果取出了消息然后处理失败，这个被取出的消息就将丢失

## 3. LPUSH + BRPOPLPUSH
通过 LPUSH 生产消息，然后通过 BRPOPLPUSH阻塞地等待 list 新消息到来，有了新消息才开始消费，同时将消息备份到另外一个 list 当中
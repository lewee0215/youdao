# Redis 跳跃表（skiplist）是一种随机化的数据结构

https://mp.weixin.qq.com/s/NOsXdrMrWwq4NTm180a6vw
有序列表 zset 类似于 Java 中的 SortedSet 和 HashMap 的结合体,内部实现就依赖了一种叫做 「跳跃列表」 的数据结构

### 多层链表
多层链表可以通过空间换取类似二分查找的效果

### 跳跃表 skiplist
skiplist 不要求上下相邻两层链表之间的节点个数有严格的对应关系，而是 <font color='yellow'>为每个节点随机出一个层数(level) </font>

直观上期望的目标是 50% 的概率被分配到 Level 1，25% 的概率被分配到 Level 2，12.5% 的概率被分配到 Level 3，以此类推...有 2-63 的概率被分配到最顶层，因为这里每一层的晋升率都是 50%

Redis 跳跃表默认允许最大的层数是 32

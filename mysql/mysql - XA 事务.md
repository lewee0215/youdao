# Mysql分布式事务XA
https://blog.csdn.net/soonfly/article/details/70677138  

资源管理器（resource manager）：用来管理系统资源，是通向事务资源的途径。数据库就是一种资源管理器。资源管理还应该具有管理事务提交或回滚的能力。
事务管理器（transaction manager）：事务管理器是分布式事务的核心管理者。事务管理器与每个资源管理器（resource
manager）进行通信，协调并完成事务的处理。事务的各个分支由唯一命名进行标识

## 分布式事务原理：分段式提交
分布式事务通常采用2PC协议，全称Two Phase Commitment Protocol。该协议主要为了解决在分布式数据库场景下，所有节点间数据一致性的问题。分布式事务通过2PC协议将提交分成两个阶段：
prepare: 即所有的参与者准备执行事务并锁住需要的资源。参与者ready时，向transaction manager报告已准备就绪。
commit/rollback: 当transaction manager确认所有参与者都ready后，向所有参与者发送commit命令

## Mysql 外部XA & 内部XA
* 外部XA用于跨多MySQL实例的分布式事务，需要应用层作为协调者，通俗的说就是比如我们在PHP中写代码，那么PHP书写的逻辑就是协调者。应用层负责决定提交还是回滚，崩溃时的悬挂事务。MySQL数据库外部XA可以用在分布式数据库代理层，实现对MySQL数据库的分布式事务支持，例如开源的代理工具：网易的DDB，淘宝的TDDL等等。
* 内部XA事务用于同一实例下跨多引擎事务，由Binlog作为协调者，比如在一个存储引擎提交时，需要将提交信息写入二进制日志，这就是一个分布式内部XA事务，只不过二进制日志的参与者是MySQL本身。Binlog作为内部XA的协调者，在binlog中出现的内部xid，在crash recover时，由binlog负责提交。(这是因为，binlog不进行prepare，只进行commit，因此在binlog中出现的内部xid，一定能够保证其在底层各存储引擎中已经完成prepare)

# JTA(Java Transaction API)
https://blog.csdn.net/jaryle/article/details/88638780

springboot 官方提供了 Atomikos or Bitronix的解决思路
https://blog.csdn.net/csdn785029689/article/details/100455409
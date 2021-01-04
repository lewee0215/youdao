# RocketMQ WAL 模式
目前MQ的方案中都是基于WAL的方式实现的（RocketMQ、Kafka），日志文件会被过期删除，一般会保留最近一段时间的数据

# WAL: Write-Ahead Logging 预写日志系统
https://blog.csdn.net/qq_14855971/article/details/105852637
数据库中一种高效的日志算法，对于非内存数据库而言，磁盘I/O操作是数据库效率的一大瓶颈。在相同的数据量下，采用WAL日志的数据库系统在事务提交时，磁盘写操作只有传统的回滚日志的一半左右，大大提高了数据库磁盘I/O操作的效率，从而提高了数据库的性能

## mysql
mysql 的 WAL，大家可能都比较熟悉。mysql 通过 redo、undo 日志实现 WAL。  
redo log 称为重做日志，每当有操作时，在数据变更之前将操作写入 redo log，这样当发生掉电之类的情况时系统可以在重启后继续操作。  
undo log 称为撤销日志，当一些变更执行到一半无法完成时，可以根据撤销日志恢复到变更之间的状态。mysql 中用 redo log 来在系统 Crash 重启之类的情况时修复数据（事务的持久性），而 undo log 来保证事务的原子性

## sqlite wal & rollback journal
https://blog.csdn.net/weixin_33946020/article/details/87951511
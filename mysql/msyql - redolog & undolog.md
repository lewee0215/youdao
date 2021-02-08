# MySQL事务日志(redo log和undo log)
https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html    
innodb事务日志包括redo log和undo log。redo log是重做日志，提供前滚操作，undo log是回滚日志，提供回滚操作  

## LSN(Log Sequence Number)
https://blog.csdn.net/weixin_44055234/article/details/108824173  
LSN实际上就是InnoDB使用的一个版本标记的计数，它是一个单调递增的值。  
数据页和redo log都有各自的LSN。我们可以根据数据页中的LSN值和redo log中LSN的值判断需要恢复的redo log的位置和大小

根据LSN，可以获取到几个有用的信息：
1.数据页的版本信息。
2.写入的日志总量，通过LSN开始号码和结束号码可以计算出写入的日志量。
3.可知道检查点的位置。

https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html  
LSN不仅存在于redo log中，还存在于数据页中，在每个数据页的头部，有一个fil_page_lsn记录了当前页最终的LSN值是多少。通过数据页中的LSN值和redo log中的LSN值比较，如果页中的LSN值小于redo log中LSN值，则表示数据丢失了一部分，这时候可以通过redo log的记录来恢复到redo log中记录的LSN值时的状态

## Redo Log与BinLog 区别
1. BinLog 二进制日志先于redo log被记录
2. BinLog 二进制日志记录操作的方法是逻辑性的语句。即便它是基于行格式的记录方式，其本质也还是逻辑的SQL设置，如该行记录的每列的值是多少。而redo log是在物理格式上的日志，它记录的是数据库中每个页的修改
3. BinLog 二进制日志只在提交的时候一次性写入，所以二进制日志中的记录方式和提交顺序有关，且一次提交对应一次记录。而redo log中是记录的物理页的修改，redo log文件中同一个事务可能多次记录，最后一个提交的事务记录会覆盖所有未提交的事务记录。例如事务T1，可能在redo log中记录了 T1-1,T1-2,T1-3，T1* 共4个操作，其中 T1* 表示最后提交时的日志记录，所以对应的数据页最终状态是 T1* 对应的操作结果。而且redo log是并发写入的，不同事务之间的不同版本的记录会穿插写入到redo log文件中，例如可能redo log的记录方式如下： T1-1,T1-2,T2-1,T2-2,T2*,T1-3,T1* 
4. 事务日志记录的是物理页的情况，它具有幂等性，因此记录日志的方式极其简练。幂等性的意思是多次操作前后状态是一样的，例如新插入一行后又删除该行，前后状态没有变化。而二进制日志记录的是所有影响数据的操作，记录的内容较多。例如插入一行记录一次，删除该行又记录一次

## innodb_flush_log_at_trx_commit 
https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html  
MySQL支持用户自定义在commit时如何将log buffer中的日志刷log file中。这种控制通过变量 innodb_flush_log_at_trx_commit 的值来决定。该变量有3种值：0、1、2，默认为1。但注意，这个变量只是控制commit动作是否刷新log buffer到磁盘。

![](https://images2018.cnblogs.com/blog/733013/201805/733013-20180508104623183-690986409.png)

当设置为0的时候，事务提交时不会将log buffer中日志写入到os buffer，而是每秒写入os buffer并调用fsync()写入到log file on disk中。也就是说设置为0时是(大约)每秒刷新写入到磁盘中的，当系统崩溃，会丢失1秒钟的数据。

当设置为1的时候，事务每次提交都会将log buffer中的日志写入os buffer并调用fsync()刷到log file on disk中。这种方式即使系统崩溃也不会丢失任何数据，但是因为每次提交都写入磁盘，IO的性能较差。  

当设置为2的时候，每次提交都仅写入到os buffer，然后是每秒调用fsync()将os buffer中的日志写入到log file on disk

## undo log
https://www.cnblogs.com/f-ck-need-u/archive/2018/05/08/9010872.html
undo log有两个作用：提供回滚和多个行版本控制(MVCC)  

在数据修改的时候，不仅记录了redo，还记录了相对应的undo，如果因为某些原因导致事务失败或回滚了，可以借助该undo进行回滚

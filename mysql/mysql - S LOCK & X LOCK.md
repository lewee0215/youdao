# Mysql共享锁和排他锁
https://blog.csdn.net/diaobatian/article/details/90603887  

InnoDB引擎默认的修改数据语句，update,delete,insert都会自动给涉及到的数据加上排他锁，select语句默认不会加任何锁类型

如果加排他锁可以使用select ...for update语句，加共享锁可以使用select ... lock in share mode语句

## 共享锁又称为读锁(S LOCK)
共享锁就是多个事务对于同一数据可以共享一把锁，都能访问到数据，但是只能读不能修改

## 排他锁又称为写锁(X LOCK)
一个事务在一行数据加上排他锁后，其他事务不能再在其上加其他的锁

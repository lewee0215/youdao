# Mysql explain 执行计划
https://www.cnblogs.com/bulushengse/p/12703789.html

![](https://img2020.cnblogs.com/blog/1587882/202004/1587882-20200415101157416-644798487.png)

## 执行计划字段含义：  
https://www.cnblogs.com/klvchen/p/10137117.html  

select_type ：操作类型  
table : 正在访问的表名  
type ：查询检索方式  
possible_keys : 可能使用的索引  
key : 真实使用的  
key_len : MySQL中使用索引字节长度  
rows : mysql 预估为了找到所需的行而要读取的行数  
extra : 附加信息

## type 参数说明
https://www.cnblogs.com/bulushengse/p/12703789.html  
type结果值从好到坏依次是：  
system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL

| 参数    | 说明    | 
| :-        | :-        | 
| ALL|全表扫描|
| index | 另一种形式的全表扫描，只不过他的扫描方式是按照索引的顺序 |
| range | 有范围的索引扫描，相对于index的全表扫描，他有范围限制，因此要优于index |
| ref |  查找条件列使用了索引而且不为主键和unique。其实，意思就是虽然使用了索引，但该索引列的值并不唯一，有重复。这样即使使用索引快速查找到了第一条数据，仍然不能停止，要进行目标值附近的小范围扫描。但它的好处是它并不需要扫全表，因为索引是有序的，即便有重复值，也是在一个非常小的范围内扫描。|
|EQ_REF|搜索时使用primary key 或 unique类型|
| const | 常量，表最多有一个匹配行,因为仅有一行,在这行的列值可被优化器剩余部分认为是常数,const表很快,因为它们只读取一次。 |
|SYSTEM|系统，表仅有一行(=系统表)。这是const联接类型的一个特例|
<br/>

## extra 参数说明
https://www.cnblogs.com/klvchen/p/10137117.html  

| 参数    | 说明    | 
| :-        | :-        | 
|Using index|此值表示mysql将使用覆盖索引，以避免访问表|
|Using where|	mysql 将在存储引擎检索行后再进行过滤，许多where条件里涉及索引中的列，当(并且如果)它读取索引时，就能被存储引擎检验，因此不是所有带where子句的查询都会显示“Using where”。有时“Using where”的出现就是一个暗示：查询可受益于不同的索引|
|Using temporary|mysql 对查询结果排序时会使用临时表|
|Using filesort|mysql会对结果使用一个外部索引排序，而不是按索引次序从表里读取行。mysql有两种文件排序算法，这两种排序方式都可以在内存或者磁盘上完成，explain不会告诉你mysql将使用哪一种文件排序，也不会告诉你排序会在内存里还是磁盘上完成|
|Range checked for each record(index map: N)|没有好用的索引，新的索引将在联接的每一行上重新估算，N是显示在possible_keys列中索引的位图，并且是冗余的|

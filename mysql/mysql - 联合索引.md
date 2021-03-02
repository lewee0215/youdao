# Mysql - 联合索引
https://www.codebye.com/the-concrete-realization-principle-of-compound-index-d.html  

假如是单列，就按这列数据进行排序
假如是多列，就按多列数据排序，例如有（1,1） （2,2） （2,1） （1,2）
那在索引中的叶子节点的数据顺序就是（1,1）（1,2）（2,1）（2,2）
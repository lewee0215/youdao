
# 微博 Feed 流架构及实现原理
https://blog.csdn.net/joeyon1985/article/details/43054885

# 用户维度
1. 在线非在线：非在线粉丝登录后再根据关注列表拉取微博订阅列表
2. 活跃非活跃：就是一个事件不会推给所有关注者，只会推给活跃的用户
3. 当天是否登陆过

# Pull 模式 - 读扩散
每个人有自己产生的feeds队列，打开自己的首页时，按照自己的关注列表和屏蔽规则，去读取其他用户的feeds队列，然后汇总、排序

数据结构 ： Following  
优点：实现最简单，关注列表 敏感性最高  
缺点：性能最差，而且差的很稳定

https://blog.csdn.net/z50L2O08e2u4afToR9A/article/details/80544968  
在拉模式中，用户A获取“由别人发布的feed组成的主页”的过程及其复杂，此时需要：
1. 获取A的关注列表
2. 获取所关注列表中，所有用户发布的feed
3. 对消息进行rank排序（假设按照发布时间排序），分页取出对应的一页feeds

## 拉模式(pull)-改进（时间分区拉模式）
https://blog.csdn.net/sedrtse/article/details/78296112  
feeds的存储上，使用按照时间进行分区存储。分为最近时间段（比如最近一个小时），近期的，比较长时期等等



# Push 模式 - 写扩散
每个人有自己的产生的feeds队列和 待读取的feeds队列。每个用户产生动态时，压入到关注者的 待读取feeds队列，压入前，需要根据屏蔽规则来判断下是否要压入

数据结构 ： Following + Follewed  
优点：每个用户页面打开的速度是最快的，性能最高  
缺点：关注列表变化时，敏感度略低，但读取队列的时候，再根据规则过滤一遍，也没啥太大问题

https://blog.csdn.net/z50L2O08e2u4afToR9A/article/details/80544968  
在推模式（写扩散）中，发布一条feed的流程会更复杂一点。例如B新发布了一条msg12：
1. 在B的发布feed存储里加入消息12
2. 查询B全部粉丝AD
3. 在粉丝AD的接收feed存储里也加入消息12

## L1-Main-HA 缓存架构
https://blog.csdn.net/weixin_45583158/article/details/100143067

## weibo 架构进化
https://blog.csdn.net/coolham/article/details/6583615  


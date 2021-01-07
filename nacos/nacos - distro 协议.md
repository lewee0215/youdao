# Distro 协议流程
https://www.cnblogs.com/longge2005/archive/2004/01/13/13954856.html
Distro用于处理ephemeral类型数据

1. nacos启动首先从其他远程节点同步全部数据
2. nacos每个节点是平等的都可以处理写入请求，同时把新数据同步到其他节点
3. 每个节点只负责部分数据，定时发送自己负责数据校验值到其他节点来保持数据一致性
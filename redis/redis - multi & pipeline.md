# Redis MULTI & PIPELINE
https://blog.csdn.net/qmhball/article/details/79074421
Pipeline 将一组Redis命令进行组装，通过一次RTT传输给Redis，再将这组Redis命令的执行结果按顺序返回给客户端
https://dongshao.blog.csdn.net/article/details/106060154

## 命令执行 - 原生模式和pipeline模式
![](https://img-blog.csdnimg.cn/20190826143331251.png)

原生模式的每个操作都是原子操作；pipeline模式的操作时非原子性的。
为了保证pipeline的批量操作的原子性，可以通过Redis提供的简单事务控制命令multi和exec进行控制




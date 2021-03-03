# Java8 - Stream API
https://blog.csdn.net/justloveyou_/article/details/79562574

Stream 提供串行和并行两种模式进行汇聚操作，并发模式能够充分利用多核处理器的优势，使用fork/join并行方式来拆分任务和加速处理过程

Stream不是集合元素，它不是数据结构并不保存数据，它是有关算法和计算的，它更像一个高级版本的Iterator

## ParallelStream
https://blog.csdn.net/lgq2016/article/details/107159256/  
ParallelStream默认使用了fork-join框架，其默认线程数是CPU核心数

```java
// 1. 全局设置修改默认的多线程数量
// 因为所有使用并行流parallerStream的地方都是使用同一个Fork-Join线程池，而Fork-Join线程数默认仅为cpu的核心数
System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "64");

// 2. 自定义 ForkJoinPool
ForkJoinPool forkJoinPool = new ForkJoinPool(3);  
forkJoinPool.submit(() -> {  
    firstRange.parallelStream().forEach((number) -> {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) { }
    });
});
```

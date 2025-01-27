# JVM-内存模型（jmm）
https://www.jianshu.com/p/76959115d486
Java内存模型规定所有的变量都是存在主存当中（类似于前面说的物理内存），每个线程都有自己的工作内存（类似于前面的高速缓存）。线程对变量的所有操作都必须在工作内存中进行，而不能直接对主存进行操作。并且每个线程不能访问其他线程的工作内存

# JVM - 内存类型
http://blog.csdn.net/u011080472/article/details/51337422
主内存（堆内存）：保存了所有的变量。
共享变量：如果一个变量被多个线程使用，那么这个变量会在每个线程的工作内存中保有一个副本，这种变量就是共享变量。
工作内存（栈内存）：每个线程都有自己的工作内存，线程独享，保存了线程用到了变量的副本（主内存共享变量的一份拷贝）。工作内存负责与线程交互，也负责与主内存交互。

JMM对共享内存的操作做出了如下两条规定：
http://blog.csdn.net/ning_xian_hong/article/details/45242701
1。线程对共享内存的所有操作都必须在自己的工作内存中进行，不能直接从主内存中读写；
2。不同线程无法直接访问其他线程工作内存中的变量，因此共享变量的值传递需要通过主内存完成。

导致共享变量在进程间不可见的原因有以下几个：
1。线程交叉执行
2。指令重排序
3。共享变量更新后的值没有在工作内存和主内存间及时更新
线程交叉执行：主要指线程调度。
指令重排序：为了发挥CPU性能，指令执行顺序可能与书写的不同，分为编译器优化的重排序(编译器优化)，指令集并行重排序（处理器优化），内存系统的重排序（处理器优化）。
共享变量更新：如果想让线程A对共享变量的修改被线程B看到，需要以下步骤：把线程A的工作内存中更新过的变量刷新到主内存中，再将主内存中最新的共享变量的值刷新到线程B的工作内存中。如果更新不及时，则会导致共享变量的不可见，数据不准确，线程不安全。

https://hanchao.blog.csdn.net/article/details/79575491
![](https://fanyi.baidu.com/favicon.ico)

# 指令重排
https://blog.csdn.net/u011080472/article/details/51337422
在执行程序时为了提高性能，编译器和处理器经常会对指令进行重排序
* 编译器优化的重排序。编译器在不改变单线程程序语义放入前提下，可以重新安排语句的执行顺序。
* 指令级并行的重排序。现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性，处理器可以改变语句对应机器指令的执行顺序。
* 内存系统的重排序。由于处理器使用缓存和读写缓冲区，这使得加载和存储操作看上去可能是在乱序执行

# Happens-Before原则
https://blog.csdn.net/hanchao5272/article/details/79575491  
如果一个操作 happens-before 第二个操作，则第一个操作对第二个操作是可见的，并且一定发生在第二个操作之前

* 线程内部规则：在同一个线程内，前面操作的执行结果对后面的操作是可见的。
* 同步规则：如果一个操作x与另一个操作y在同步代码块/方法中，那么操作x的执行结果对操作y可见。
* 传递规则：如果操作x的执行结果对操作y可见，操作y的执行结果对操作z可见，则操作x的执行结果对操作z可见。
* 对象锁规则：如果线程1解锁了对象锁a，接着线程2锁定了a，那么，线程1解锁a之前的写操作的执行结果都对线程2可见。
* volatile变量规则：如果线程1写入了volatile变量v，接着线程2读取了v，那么，线程1写入v及之前的写操作的执行结果都对线程2可见。
* 线程start原则：如果线程t在start()之前进行了一系列操作，接着进行了start()操作，那么线程t在start()之前的所有操作的执行结果对start()之后的所有操作都是可见的。
* 线程join规则：线程t1写入的所有变量，在任意其它线程t2调用t1.join()成功返回后，都对t2可见

# Java - Synchronized 关键字和锁升级
https://blog.csdn.net/tongdanping/article/details/79647337  
*注意：为了避免无用的自旋，轻量级锁一旦膨胀为重量级锁就不会再降级为轻量级锁了；偏向锁升级为轻量级锁也不能再降级为偏向锁

![](https://img-blog.csdn.net/20180930170014163?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTMzODA2OTQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
 
## 线程状态及状态转换
https://blog.csdn.net/u013380694/article/details/82911610   
当多个线程同时请求某个对象监视器时，对象监视器会设置几种状态用来区分请求的线程：

* Contention List：所有请求锁的线程将被首先放置到该竞争队列。
* Entry List：Contention List中那些有资格成为候选人的线程被移到Entry List。
* Wait Set：那些调用wait方法被阻塞的线程被放置到Wait Set。
* OnDeck：任何时刻最多只能有一个线程正在竞争锁，该线程称为OnDeck。
* Owner：获得锁的线程称为Owner。
* !Owner：释放锁的线程

## ContentionList虚拟队列
ContentionList并不是一个真正的Queue，而只是一个虚拟队列，原因在于ContentionList是由Node及其next指针逻辑构成，并不存在一个Queue的数据结构

![](https://img-blog.csdn.net/20180930170135161?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTMzODA2OTQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

ContentionList是一个后进先出（LIFO）的队列，每次新加Node时都会在队头进行，通过CAS改变第一个节点的的指针为新增节点，同时设置新增节点的next指向后续节点，而取得操作则发生在队尾。显然，该结构其实是个Lock-Free的队列

因为只有Owner线程才能从队尾取元素，也即线程出列操作无争用，当然也就避免了CAS的ABA问题

## EntryList
https://blog.csdn.net/u013380694/article/details/82911610  
EntryList与ContentionList逻辑上同属等待队列，ContentionList会被线程并发访问，为了降低对ContentionList队尾的争用，而建立EntryList。Owner线程在unlock时会从ContentionList中迁移线程到EntryList，并会指定EntryList中的某个线程（一般为Head）为Ready（OnDeck）线程。Owner线程并不是把锁传递给OnDeck线程，只是把竞争锁的权利交给OnDeck，OnDeck线程需要重新竞争锁。这样做虽然牺牲了一定的公平性，但极大的提高了整体吞吐量，在Hotspot中把OnDeck的选择行为称之为“竞争切换”

## WaitSet队列
如果Owner线程被wait方法阻塞，则转移到WaitSet队列；如果在某个时刻被notify/notifyAll唤醒，则再次转移到EntryList

## Java 对象内存信息
https://blog.csdn.net/tongdanping/article/details/79647337  
对象是存放在堆内存中的，对象大致可以分为三个部分，分别是对象头、实例变量和填充字节
* 对象头的主要是由 MarkWord 和 Klass Point (类型指针) 组成  
Klass Point是是对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例  
Mark Word用于存储对象自身的运行时数据。如果对象是数组对象，那么对象头占用3个字宽（Word），如果对象是非数组对象，那么对象头占用2个字宽。（1word = 2 Byte = 16 bit）
* 实例变量存储的是对象的属性信息，包括父类的属性信息，按照4字节对齐
* 填充字符，因为虚拟机要求对象字节必须是8字节的整数倍，填充字符就是用于凑齐这个整数倍的

***Java 对象头 MarkWord 信息***  
https://blog.csdn.net/qq_26542493/article/details/90938070  
Java对象处于5种不同状态时，Mark Word中64个位的表现形式  
![alt text](https://img-blog.csdn.net/20180322153316377 "title")

| 标志位   | 说明   | 
| :-        | :-         |
| hashcode      | 31位的对象标识hashCode，采用延迟加载技术。调用方法System.identityHashCode()计算，并会将结果写到该对象头中。当对象加锁后（偏向、轻量级、重量级），MarkWord的字节没有足够的空间保存hashCode，因此该值会移动到管程Monitor中       | 
|thread|持有偏向锁的线程ID|
|epoch|偏向锁的时间戳|
|age|4位的Java对象年龄。在GC中，如果对象在Survivor区复制一次，年龄增加1。当对象达到设定的阈值时，将会晋升到老年代。默认情况下，并行GC的年龄阈值为15，并发GC的年龄阈值为6。由于age只有4位，所以最大值为15，这就是-XX:MaxTenuringThreshold选项最大值为15的原因|
|ptr_to_lock_record|轻量级锁状态下，指向栈中锁记录的指针|
|ptr_to_heavyweight_monitor|重量级锁状态下，指向对象监视器Monitor的指针|
| biased_lock        |对象是否启用偏向锁标记，只占1个二进制位。为1时表示对象启用偏向锁，为0时表示对象没有偏向锁。lock和biased_lock共同表示对象处于什么锁状态       | 
| lock        | 2位的锁状态标记位，由于希望用尽可能少的二进制位表示尽可能多的信息，所以设置了lock标记       | 

## 偏向锁
https://blog.csdn.net/qq838642798/article/details/64439761  
偏向锁是指为了在线程竞争不激烈的情况下，减少加锁及解锁的性能损耗（轻量级锁涉及多次CAS操作）在Mark Word中有保存这上次使用这个对象锁的线程ID信息，如果这个线程再次请求这个对象锁，那么只需要读取该对象上的Mark Word的偏向锁信息（也就是线程id）跟线程本身的id进行对比，如果是同一个id就直接认为该id获得锁成功，而不需要在进行真正的加解锁操作

1. 当线程1访问代码块并获取锁对象时，会在java对象头和栈帧中记录偏向的锁的threadID
2. 偏向锁不会主动释放锁，因此以后线程1再次获取锁的时候，需要比较当前线程的threadID和Java对象头中的threadID是否一致
3. 如果一致（还是线程1获取锁对象），则无需使用CAS来加锁、解锁；
4. 如果不一致（其他线程，如线程2要竞争锁对象，而偏向锁不会主动释放因此还是存储的线程1的threadID），那么需要查看Java对象头中记录的线程1是否存活，如果没有存活，那么锁对象被重置为无锁状态
5. 如果存活，那么立刻查找该线程（线程1）的栈帧信息，如果还是需要继续持有这个锁对象，那么暂停当前线程1，撤销偏向锁，升级为轻量级锁，如果线程1 不再使用该锁对象，那么将锁对象状态设为无锁状态，重新偏向新的线程

## 轻量级锁
https://blog.csdn.net/qq838642798/article/details/64439761  
所谓轻量级锁是比偏向锁更耗资源的锁,实现机制是,线程在竞争轻量级锁前,在线程的栈内存中分配一段空间作为锁记录空间(就是轻量级锁对应的对象的对象头的字段的拷贝),拷贝好后,线程通过CAS去竞争这个对象锁，试图把对象的对象头子段改成指向所记录空间，如果成功则说明获取轻量级锁成功，如果失败，则进入自旋取试着获取锁。如果自旋到一定次数还是不能获取到锁，则进入重量级锁

1. 线程1获取轻量级锁时会先把锁对象的对象头MarkWord复制一份到线程1的栈帧中创建的用于存储锁记录的空间（称为DisplacedMarkWord），然后使用CAS把对象头中的内容替换为线程1存储的锁记录（<font color='yellow'>DisplacedMarkWord</font>）的地址；
2. 如果在线程1复制对象头的同时（在线程1CAS之前），线程2也准备获取锁，复制了对象头到线程2的锁记录空间中，但是在线程2CAS的时候，发现线程1已经把对象头换了，线程2的CAS失败，那么线程2就尝试使用自旋锁来等待线程1释放锁

### 轻量级锁升级条件
* 轻量级锁替换失败到达一定次数（默认为10）后，轻量级锁升级为重量级锁
* 如果线程2自旋期间，有线程3也需要访问同步方法，则立刻由轻量级锁膨胀为重量级锁
* java1.6中，引入了自适应自旋锁，自适应意味着自旋 的次数不是固定不变的，而是根据前一次在同一个锁上自 旋的时间以及锁的拥有者的状态来决定

## 重量级锁
https://blog.csdn.net/qq838642798/article/details/64439761  
所谓的重量级锁，其实就是最原始和最开始java实现的阻塞锁。在JVM中又叫对象监视器。这时的锁对象的对象头字段指向的是一个互斥量，所有线程竞争重量级锁，竞争失败的线程进入阻塞状态（操作系统层面），并且在锁对象的一个等待池中等待被唤醒，被唤醒后的线程再次去竞争锁资源

synchronized 代码块是 monitorenter/monitorexit 指令实现的

## 锁消除
Java虚拟机在JIT编译时(又称即时编译)，通过对运行上下文的扫描，经过逃逸分析，去除不可能存在共享资源竞争的锁，通过这种方式消除没有必要的锁，可以节省毫无意义的请求锁时间
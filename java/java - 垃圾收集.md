# 对象引用判断
https://blog.csdn.net/sunjin9418/article/details/79603651

> 1> 引用计数算法

给对象中添加一个引用计数器，每当有一个地方引用它时，计数器就加1，当引用失效时，计数器就减1；任何时刻计数器都为0的对象是不可能在被使用的。

\## 引用计数算法无法对象之间循环引用的问题

> 2> GC Roots可达性分析算法

通过“GC Roots”对象作为起始点，从这些节点向下搜索，搜索走过的路径称为引用链（Reference Chain）,当一个对象到GC Roots没有任何引用链相连时，则证明此对象是没有被使用的

在Java中，下面几种对象可以作为GC Roots：
1. 虚拟机栈（栈帧中的本地变量表）中引用的对象；
2. 方法区中类静态属性引用的对象；
3. 方法区中常量引用的对象；
4. 本地方法栈中JNI（即Native方法）引用的对象；

# 垃圾收集算法
## 标记-清除算法
* 标记: 判定垃圾对象的过程就是标记过程
* 清除：清理标记为垃圾的对象

算法有两个不足：
1. 标记和清除的效率不高
2. 空间问题，标记清除后会产生大量不连续的内存碎片，空间碎片太多的话可能导致以后分配大块内存时失败的问题，这样就会触发另一次垃圾收集操作

## 复制算法
将可用内存按照容量分为大小相等的两部分，每次只使用其中的一块。当一块的内存用完了，就将还存活的对象复制到另一块，然后再把已经使用过的内存空间一次性清理掉

HotSpot虚拟机默认的Eden和Survivor1 , Survivor2 比例是8:1:1

## 标记-整理算法
标记-整理（Mark-Compact）算法，标记过程和标记-清除算法一样，但后续步骤不是直接对可回收对象进行清理，而是整理存活的对象，将存活的对象都向一端移动，然后直接清理掉边界外的内存

## 分代收集算法
现在的虚拟机都使用“分代收集”算法，算法只是根据对象的存活周期的不同将内存划分。  
一般把Java堆空间分为新生代和老年代，这样就可以根据各个年代的特点采用最适合的收集算法。

在新生代，每次垃圾收集都会有大量的对象死去，只有少量存活，这样就可以选择复制算法，只需复制少量存活的对象就可以完成垃圾收集。在老年代中，对象的存活率高、没有额外的空间对它进行分配担保，就必须采用标记-清除或标记-整理算法来进行回收

# 分代的收集器 - 7种
https://www.cnblogs.com/swordfall/p/10734403.html  
新生代收集器：Serial、ParNew、Parallel Scavenge  
老年代收集器：Serial Old、Parallel Old、CMS  
整堆收集器：  G1  

如果两个收集器之间存在连线，就说明它们可以搭配使用

![alt text](https://img2018.cnblogs.com/blog/1217276/201904/1217276-20190419123650113-2038401125.png "title")

可用组合 : Serial/Serial Old、Serial/CMS、ParNew/Serial Old、ParNew/CMS、Parallel Scavenge/Serial Old、Parallel Scanvenge/Parallel Old、G1

垃圾收集语义：  
* 并行（Parallel）：指多条垃圾收集线程并行工作，但此时用户线程仍然处于等待状态。
* 并发（Concurrent）：指用户线程与垃圾收集线程同时执行（但不一定是并行的，可能会交替执行），用户程序在继续运行，而垃圾收集程序运行于另一个CPU上

## JVM 收集器配置参数
https://www.cnblogs.com/heqiyoujing/p/11361307.html  
jdk1.7 默认垃圾收集器Parallel Scavenge（新生代）+Parallel Old（老年代）  
jdk1.8 默认垃圾收集器Parallel Scavenge（新生代）+Parallel Old（老年代）  
jdk1.9 默认垃圾收集器G1  

| -XX:参数配置                   | 收集器        |
| :-                            | :-:           |
| -XX:+UseSerialGC              | Serial + Serial Old            |
| -XX:+UseParNewGC              | ParNew + Serial Old       |
| -XX:+UseConcMarkSweepGC       | ParNew + CMS       |
| -XX:+UseParallelGC            | Parallel Scavenge + Parallel Old        |
| -XX:+UseParallelOldGC         | Parallel Scavenge + Parallel Old        |
<br/>

## Serial收集器 - 复制算法（新生代）
一个单线程的收集器，但它的”单线程“的意义并不仅仅说明它只会使用一个CPU或一条收集线程去完成垃圾收集工作，更重要的是在它进行垃圾收集时，必须暂停其他所有的工作线程，直到它收集结束

Serial/Serial Old收集器的运行过程：

![alt text](https://img2018.cnblogs.com/blog/1217276/201904/1217276-20190419125749048-1944774651.png "title")

## ParNew - 复制算法（新生代）
是Serial收集器的多线程版本，除了使用多条线程进行垃圾收集之外，其余行为包括Serial收集器可用的所有控制参数（例如：-XX：SurvivorRatio、-XX：PretenureSizeThreshold、-XX：HandlePromotionFailure等）、收集算法、Stop The World、对象分配规则、回收策略等都与Serial收集器完全一样

ParNew/Serial Old收集器的工作过程:

![alt text](https://img2018.cnblogs.com/blog/1217276/201904/1217276-20190419132509209-425788073.png "title")

## Parallel Scanvenge收集器 - 复制算法（新生代）
Parallel Scavenge收集器的目标则是达到一个可控制的吞吐量（Throughput）。所谓吞吐量就是CPU用于运行用户代码的时间与CPU总消耗时间的比值，即吞吐量 = 运行用户代码时间 /（运行用户代码时间 + 垃圾收集时间），虚拟机总共运行了100分钟，其中垃圾收集花掉1分钟，那吞吐量就是99%  

## Serial Old收集器 - 标记/整理 (老年代)

## Parallel Old收集器 - 标记/整理 (老年代)

## CMS收集器 - 标记/清除  (老年代)
CMS（Concurrent Mark Sweep）收集器是一种以获取最短回收停顿时间为目标的收集器  
 在JDK1.5中使用CMS来收集老年代的时候，新生代只能选择ParNew或者Serial收集器中的一个  

![alt text](https://img2018.cnblogs.com/blog/1217276/201904/1217276-20190420113715103-1317893404.png "title")

CMS 收集器主要步骤：
1. 初始标记（CMS initial mark）- <font color='yellow'>Stop The World </font>  
标记一下GC Roots能直接关联到的对象

2. 并发标记（CMS concurrent mark） 
进行GC Roots Tracing

3. 重新标记（CMS remark）- <font color='yellow'>Stop The World </font>  
修正并发标记期间因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录

4. 并发清除（CMS concurrent sweep）

CMS 收集器缺点：
* CMS收集器对CPU资源非常敏感。CMS默认启动的回收线程数是（CPU数量+3）/4，当CPU个数大于4时，垃圾收集线程使用不少于25%的CPU资源，当CPU个数不足时，CMS对用户程序的影响很大；
* CMS收集器无法处理浮动垃圾，可能出现“Concurrent Mode Failure”失败而导致另一次Full GC
* CMS使用标记-清除算法，会产生内存碎片；

## G1 收集器 - 标记/整理 （全代）
https://blog.csdn.net/sunjin9418/article/details/79603651  

![alt text](https://img2018.cnblogs.com/blog/1217276/201904/1217276-20190420144927628-1735440321.png "title")

G1收集器的运作步骤：
1. 初始标记（Initial Marking）
2. 并发标记（Concurrent Marking）
3. 最终标记（Final Marking）
4. 筛选回收（Live Data Counting and Evacuation）

G1 收集器优点：
1. 并行与并发：有些收集器需要停顿的过程G1仍然可以通过并发的方式让用户程序继续执行；
2. 分代收集：可以不使用其他收集器配合管理整个Java堆；
3. 空间整合：使用标记-整理算法，不产生内存碎片；
4. 可预测的停顿：G1除了降低停顿外，还能建立可预测的停顿时间模型


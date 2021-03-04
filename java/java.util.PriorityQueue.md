# java.util.PriorityQueue
https://www.cnblogs.com/williamjie/p/9478150.html  
PriorityQueue是从JDK1.5开始提供的新的数据结构接口，它是一种基于优先级堆的极大优先级队列

优先级队列每次从队列中取出的是具有最高优先权的元素。

如果不提供Comparator的话，优先队列中元素默认按自然顺序排列，也就是数字默认是小的在队列头，字符串则按字典序排列（参阅 Comparable），也可以根据 Comparator 来指定，这取决于使用哪种构造方法。优先级队列不允许 null 元素。依靠自然排序的优先级队列还不允许插入不可比较的对象（这样做可能导致 ClassCastException）

## 实现原理
https://www.cnblogs.com/williamjie/p/9478150.html
PriorityQueue对元素采用的是堆排序，头是按指定排序方式的最小元素。
堆排序只能保证根是最大（最小），整个堆并不是有序的。方法iterator()中提供的迭代器并不保证以有序的方式遍历优先级队列中的元素

## add()和offer()
https://blog.csdn.net/qq_43460335/article/details/107097483  
add(E e)和offer(E e)的语义相同，都是向优先队列中插入元素，只是Queue接口规定二者对插入失败时的处理不同，前者在插入失败时抛出异常，后则则会返回false  

![](https://img-blog.csdnimg.cn/20200703075521352.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNDYwMzM1,size_16,color_FFFFFF,t_70)


## element()和peek()
element()和peek()的语义完全相同，都是获取但不删除队首元素，也就是队列中权值最小的那个元素，二者唯一的区别是当方法失败时前者抛出异常，后者返回null

![](https://img-blog.csdnimg.cn/20200703080001918.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNDYwMzM1,size_16,color_FFFFFF,t_70)

## remove()和poll()
remove()和poll()方法的语义也完全相同，都是获取并删除队首元素，区别是当方法失败时前者抛出异常，后者返回null。由于删除操作会改变队列的结构，为维护小顶堆的性质，需要进行必要的调整

![](https://img-blog.csdnimg.cn/20200703080148807.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzNDYwMzM1,size_16,color_FFFFFF,t_70)

# java 4种引用类型 - 强软弱虚
https://www.cnblogs.com/zedosu/p/6632249.html

![](https://pic3.zhimg.com/65b7abe9bf2fcd249c789024d95bb67a_b.jpg)

## 强引用
强引用是使用最普遍的引用：Object o=new Object(); 特点：不会被GC
将对象的引用显示地置为null：o=null; // 帮助垃圾收集器回收此对象

## 软引用 SoftReference
软引用用来描述一些还有用但是并非必须的对象，在Java中用java.lang.ref.SoftReference类来表示。对于软引用关联着的对象，只有在内存不足的时候JVM才会回收该对象。因此，这一点可以很好地用来解决OOM的问题，并且这个特性很适合用来实现缓存：比如网页缓存、图片缓存等
```java
/* 创建强引用对象 */
SoftReference<User> soft = new SoftReference<User>(user);

/* 把user对象置空，然后再从强引用中取值【注：要先存在引用中再置空，注意顺序啊】 */
user = null;

// 把对象实例保存到SoftReference中，然后将对象置空，再从SoftReference中取值时，即使显示的调用System.gc();方法，该对象实例也不会被回收(除非发生内存溢出，该对象才会被回收)
```

## 弱引用 WeakReference
弱引用与软引用的区别在于：只具有弱引用的对象拥有更短暂的生命周期。在垃圾回收器线程扫描它所管辖的内存区域的过程中，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存。不过，由于垃圾回收器是一个优先级很低的线程，因此不一定会很快发现那些只具有弱引用的对象
```java
/*把对象放在弱引用中*/
WeakReference<User> weak = new WeakReference<User>(user);
```

## 虚引用 PhantomReference 
虚引用也称为幻影引用：一个对象是都有虚引用的存在都不会对生存时间都构成影响，也无法通过虚引用来获取对一个对象的真实引用。唯一的用处：能在对象被GC时收到系统通知，JAVA中用PhantomReference来实现虚引用

<font color='yellow'>
虚引用必须要和 ReferenceQueue 引用队列一起使用
</font>

## 引用队列（ReferenceQueue）
https://www.cnblogs.com/liyutian/p/9690974.html  
引用队列可以与软引用、弱引用以及虚引用一起配合使用，当垃圾回收器准备回收一个对象时，如果发现它还有引用，那么就会在回收对象之前，把这个引用加入到与之关联的引用队列中去。程序可以通过判断引用队列中是否已经加入了引用，来判断被引用的对象是否将要被垃圾回收，这样就可以在对象被回收之前采取一些必要的措施。

与软引用、弱引用不同，虚引用必须和引用队列一起使用

## ThreadLocal 内存泄漏原因之 WeakReference
ThreadLocal的原理是操作Thread内部的一个ThreadLocalMap，这个Map的Entry继承了WeakReference,设值完成后map中是(WeakReference,value)这样的数据结构。

java中的弱引用在内存不足的时候会被回收掉，回收之后变成(null,value)的形式，key被收回掉了
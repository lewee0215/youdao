# BlockingQueue 阻塞队列
```java
public interface BlockingQueue<E> extends Queue<E> {
   
    // 忽略容器的容量大小限制添加一个元素
    // 如果需要加入容量判断， 建议使用 {@link #offer(Object) offer}.
    boolean add(E e);

    // 该方法返回true或false，不会阻塞，直接返回 
    // 当队列已满，直接返回false
    boolean offer(E e);

    // 该方法没有返回值，当队列已满时，会阻塞当前线程
    void put(E e) throws InterruptedException;

    // 该方法返回true或false，当队列已满时，会阻塞给定时间，添加操作成功返回true，否则返回false
    boolean offer(E e, long timeout, TimeUnit unit)throws InterruptedException;

    // 当队列为空，取出线程阻塞
    E take() throws InterruptedException;

    // 该方法取出元素时，如果队列为空，则阻塞给定的时间
    E poll(long timeout, TimeUnit unit) throws InterruptedException;

    int remainingCapacity();

    // 从队列中删除指定元素值的节点
    boolean remove(Object o);

    public boolean contains(Object o);

    int drainTo(Collection<? super E> c);

    int drainTo(Collection<? super E> c, int maxElements);
}
```
## SynchronousQueue
https://www.cnblogs.com/hongdada/p/6147834.html  

SynchronousQueue 是无界的，是一种无缓冲的等待队列  

SynchronousQueue 在某次添加元素后必须等待其他线程取走后才能继续添加；可以认为SynchronousQueue是一个缓存值为1的阻塞队列  

但是 isEmpty()方法永远返回是true，remainingCapacity() 方法永远返回是0，remove()和removeAll() 方法永远返回是false，iterator()方法永远返回空，peek()方法永远返回null

## ArrayBlockingQueue
https://blog.csdn.net/sinat_36553913/article/details/79529197

1. 一个由数组支持的有界队列，此队列按 **FIFO（先进先出）** 原则对元素进行排序。
2. 新元素插入到队列的尾部，队列获取操作则是从队列头部开始获得元素
3. “有界缓存区”，一旦创建，就不能在增加其容量
4. 在向已满队列中添加元素会导致操作阻塞，从空队列中提取元素也将导致阻塞
5. 此类支持对等待的生产者线程和使用者线程进行排序的可选公平策略。默认情况下，不保证是这种排序的。然而通过将公平性（fairness）设置为true，而构造的队列允许按照FIFO顺序访问线程。公平性通常会降低吞吐量，但也减少了可变性和避免了“不平衡性”

```java
public class ArrayBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {
    /** The queued items */
    final Object[] items;

    /** items index for next take, poll, peek or remove */
    int takeIndex;

    /** items index for next put, offer, or add */
    int putIndex;

    /** Number of elements in the queue */
    int count;

    // takeIndex 和 putIndex 根据数组长度循环变化
    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.items = new Object[capacity];

        // 在创建时，必须指定容量capacity,在默认时，采用非公平的策略
        lock = new ReentrantLock(fair);
        notEmpty = lock.newCondition();
        notFull =  lock.newCondition();
    }
}
```

## LinkedBlockingQueue
https://blog.csdn.net/sinat_36553913/article/details/79533606

1. LinkedBlockingQueue是一个基于已链接节点的，范围任意的blocking queue
2. 此队列按FIFO（先进先出）排序元素
3. 新元素插入到队列的尾部，并且队列获取操作会获得位于队列头部的元素
4. 链接队列的吞吐量通常要高于基于数组的对列（ArrayBlockingQueue）,但是在大多数并发应用程序中，其可预知的性能要低
5. 可选的容量范围构造方法参数作为防止队列过度扩展的一种方法，如果未指定容量，则等于Integer.MAX_VALUE，除非插入节点会使队列超出容量，否则每次插入后会动态地创建链接节点

```java
// ReentrantLock 默认使用非公平方式
private final ReentrantLock takeLock = new ReentrantLock();
private final ReentrantLock putLock = new ReentrantLock();

// LinkedBlockingQueue 节点内部类Node
static class Node<E> {
    E item;

    /**
        * One of:
        * - the real successor Node
        * - this Node, meaning the successor is head.next
        * - null, meaning there is no successor (this is the last node)
        */
    Node<E> next; //指向下个节点

    Node(E x) { item = x; }
}

peek()
// 该方法只返回队头元素的值，并不能将节点从队列中删除
```

## ArrayBlockingQueue && LinkedBlockingQueue 不同点：
https://blog.csdn.net/qq_35181209/article/details/77435144  

1、队列中的锁机制不同
    ArrayBlockingQueue中的锁是没有分离的，即生产和消费用的是同一个锁；
    LinkedBlockingQueue中的锁是分离的，即生产用的是putLock，消费是takeLock


2、底层实现机制不一样
    ArrayBlockingQueue中维护数组结构；
    LinkedBlockingQueue中维护链表结构

3、队列大小初始化方式不同
    ArrayBlockingQueue是有界的，必须指定队列的大小；

    LinkedBlockingQueue是无界的，可以不指定队列的大小，但是默认是Integer.MAX_VALUE。当然也可以指定队列大小，从而成为有界的。

4、统计队列中元素个数不一样
    ArrayBlockingQueue中维护一个普通变量int；
    LinkedBlockingQueue中定义一个AtomicInteger对象。

5、在生产或消费时操作不同
    ArrayBlockingQueue基于数组，在生产和消费的时候，是直接将枚举对象插入或移除的，不会产生或销毁任何额外的对象实例；

    LinkedBlockingQueue基于链表，在生产和消费的时候，需要把枚举对象转换为Node<E>进行插入或移除，会生成一个额外的Node对象，这在长时间内需要高效并发地处理大批量数据的系统中，其对于GC的影响还是存在一定的区别。

6、执行clear方法
    LinkedBlockingQueue中会加上两把锁
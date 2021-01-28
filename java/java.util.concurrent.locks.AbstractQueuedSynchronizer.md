# AQS原理
https://blog.csdn.net/mulinsen77/article/details/84583716  

AQS：AbstractQuenedSynchronizer抽象的队列式同步器。是除了java自带的synchronized关键字之外的锁机制  

<font color='yellow'>
自旋锁、互斥锁、读锁写锁、条件产量、信号量、栅栏都是AQS的衍生物
</font>

如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的工作线程，并将共享资源设置为锁定状态  

如果被请求的共享资源被占用，那么就需要一套线程阻塞等待以及被唤醒时锁分配的机制，这个机制AQS是用CLH队列锁实现的，即将暂时获取不到锁的线程加入到队列中  

CLH（Craig，Landin，and Hagersten）队列是一个虚拟的双向队列，虚拟的双向队列即不存在队列实例，仅存在节点之间的关联关系。
AQS是将每一条请求共享资源的线程封装成一个CLH锁队列的一个结点（Node），来实现锁的分配

https://blog.csdn.net/m_xiaoer/article/details/73459444  
AQS维护了一个state和一个FIFO线程等待队列，多线程竞争被阻塞的时候会进入该队列 

![alt text](https://img-blog.csdn.net/20170619171517217 "title")


## state就是共享资源，其访问方式有如下三种：
https://blog.csdn.net/striveb/article/details/86761900  
getState(); setState(); compareAndSetState();

```java
private transient volatile Node head;

private transient volatile Node tail;

// 表示加锁的状态，初始状态值为0
// 在互斥锁中它表示着线程是否已经获取了锁，0未获取，1已经获取了，大于1表示重入数
private volatile int state;

//返回同步状态的当前值
protected final int getState() {  
        return state;
}
 // 设置同步状态的值
protected final void setState(int newState) { 
        state = newState;
}
//原子地（CAS操作）将同步状态值设置为给定值update如果当前同步状态的值等于expect（期望值）
protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}

static final class Node {
    /** Marker to indicate a node is waiting in shared mode */
    static final Node SHARED = new Node();

    /** Marker to indicate a node is waiting in exclusive mode */
    static final Node EXCLUSIVE = null;

    /** waitStatus value to indicate thread has cancelled */
    static final int CANCELLED =  1;
    /** waitStatus value to indicate successor's thread needs unparking */
    static final int SIGNAL    = -1;
    /** waitStatus value to indicate thread is waiting on condition */
    static final int CONDITION = -2;
    /**
        * waitStatus value to indicate the next acquireShared should
        * unconditionally propagate
        */
    static final int PROPAGATE = -3;

    volatile int waitStatus;

    volatile Node prev;

    volatile Node next;

    volatile Thread thread;

    Node nextWaiter;

    /**
        * Returns true if node is waiting in shared mode.
        */
    final boolean isShared() {
        return nextWaiter == SHARED;
    }

    Node(Thread thread, Node mode) {     // Used by addWaiter
        this.nextWaiter = mode;
        this.thread = thread;
    }

    Node(Thread thread, int waitStatus) { // Used by Condition
        this.waitStatus = waitStatus;
        this.thread = thread;
    }
}
```

## 独占Exclusive 和 共享Share
https://blog.csdn.net/striveb/article/details/86761900  

AQS 定义了两种资源共享方式：  
1. Exclusive：独占，只有一个线程能执行，如ReentrantLock
2. Share：共享，多个线程可以同时执行，如Semaphore、CountDownLatch、ReadWriteLock，CyclicBarrier
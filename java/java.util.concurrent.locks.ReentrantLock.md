# ReentrantLock之公平与非公平模式
https://blog.csdn.net/qq447995687/article/details/101382823  
ReentrantLock是通过一个静态内部内Sync实现的，Sync继承自AbstractQueuedSynchronizer，并实现了其抽象方法。  
AbstractQueuedSynchronizer是java的一个抽象的同步器框架，大多数的java同步工具类都是由其实现。而NonfairSync和FairSync通过继承Sync实现锁的功能

```java
//  默认使用的是非公平模式
public ReentrantLock() {
    sync = new NonfairSync();
}

// 通过有参的构造函数可以指定使用公平还是非公平模式
public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

## NonfairSync的非公平性实现
```java
static final class NonfairSync extends Sync {
    private static final long serialVersionUID = 7316153563782823691L;

    /**
        * Performs lock.  Try immediate barge, backing up to normal
        * acquire on failure.
        */
        //加锁方法
    final void lock() {
        //cas，如果当前state=0则将state替换为1
        if (compareAndSetState(0, 1))
            //替换成功，则表示获得锁，同时将当exclusiveOwnerThread设置为当前线程，之后会通过该值是否是当前线程来确定是否重入
            setExclusiveOwnerThread(Thread.currentThread());
        else
            //替换失败，表示获取锁失败，此时通过acquire(1)进一步获取锁
            acquire(1);
    }

    protected final boolean tryAcquire(int acquires) {
        return nonfairTryAcquire(acquires);
    }

    final boolean nonfairTryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            if (compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        }
        else if (current == getExclusiveOwnerThread()) {
            int nextc = c + acquires;
            if (nextc < 0) // overflow
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        return false;
    }

    public final void acquire(int arg) {
        if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
    }
 }
```

## FairSync 的公平性实现
```java
static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            // FairSync 并没有像NonfairSync在lock方法中进行cas替换state获取锁，而是通过acquire(1)调用tryAcquire(int acquires) 方法获取锁
            acquire(1);
        }

        // FairSync 的 tryAcquire(int acquires) 比 NonfairSync的tryAcquire(int acquires) 多了 ！hasQueuedPredecessors() 的判断 ，而hasQueuedPredecessors()就体现了公共性 
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        public final void acquire(int arg) {
            if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
                selfInterrupt();
        }
    }
}
```

### hasQueuedPredecessors()
方法代表的意思是在同步队列中是否有比当前线程等待更久的线程。如果返回true，表示有线程在同步队列中且先于自身等待锁，则当前线程不能继续获取锁

```java
public final boolean hasQueuedPredecessors() {
    // The correctness of this depends on head being initialized
    // before tail and on head.next being accurate if the current
    // thread is first in queue.
    Node t = tail; // Read fields in reverse initialization order
    Node h = head;
    Node s;
    return h != t &&
        ((s = h.next) == null || s.thread != Thread.currentThread());
}
```


# java.util.concurrent.ConcurrentHashMap
https://www.jianshu.com/p/865c813f2726

## JDK1.7
https://www.cnblogs.com/guanghe/p/11239227.html  
ConcurrentHashMap是由Segment数组结构和HashEntry数组结构组成。

Segment是一种可重入锁ReentrantLock，在ConcurrentHashMap里扮演锁的角色，HashEntry则用于存储键值对数据。

一个ConcurrentHashMap里包含一个Segment数组，Segment的结构和HashMap类似，是一种数组和链表结构， 一个Segment里包含一个HashEntry数组，每个HashEntry是一个链表结构的元素， 每个Segment守护者一个HashEntry数组里的元素,当对HashEntry数组的数据进行修改时，必须首先获得它对应的Segment锁

## JDK1.8
https://www.jianshu.com/p/865c813f2726

JDK1.8的实现已经摒弃了Segment的概念，而是直接用Node数组+链表+红黑树的数据结构来实现，并发控制使用Synchronized和CAS来操作，整个看起来就像是优化过且线程安全的HashMap

https://blog.csdn.net/weixin_43185598/article/details/87938882  
默认初期长度为16，当往map中继续添加元素的时候，通过hash值跟数组长度取与来决定放在数组的哪个位置，如果出现放在同一个位置的时候，优先以链表的形式存放，在同一个位置的个数又达到了8个以上，如果数组的长度还小于64的时候，则会扩容数组。如果数组的长度大于等于64了的话，在会将该节点的链表转换成树


## ConcurrentHashMap 高低位HashCode
https://www.cnblogs.com/augustrush/p/13195141.html  
![alt text](https://img2020.cnblogs.com/blog/1600176/202006/1600176-20200626152301127-594134949.png "title")

![alt text](https://img2020.cnblogs.com/blog/1600176/202006/1600176-20200626152330839-628443655.png "title")

如果我们不做刚才移位异或运算，那么在计算槽位时将丢失高区特征  
但是细想当两个哈希码很接近时，那么这高区的一点点差异就可能导致一次哈希碰撞，所以这也是将性能做到极致的一种体现

```java
/**
    * The array of bins. Lazily initialized upon first insertion.
    * Size is always a power of two. Accessed directly by iterators.
    */
transient volatile Node<K,V>[] table;
static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash

// 获取数组中槽点位置 Node
// Node<K,V>[] tab = table ; n = tab.length
int hash = spread(key.hashCode());
Node<K,V> f = tabAt(tab, i = (n - 1) & hash)

static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
    return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
}

//>>> : 表示无符号右移，也叫逻辑右移，即若该数为正，则高位补 0，而若该数为负数，则右移后高位同样补 0
static final int spread(int h) {
    // 高16位与低16位异或，变相保留高位的比特位
    return (h ^ (h >>> 16)) & HASH_BITS;
}
```

## ConcurrentHashMap.put()
https://blog.csdn.net/weixin_43185598/article/details/87938882  
```java
    public V put(K key, V value) {
        return putVal(key, value, false);
    }

    /** Implementation for put and putIfAbsent */
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        int hash = spread(key.hashCode());
        int binCount = 0;

        // 自旋操作,并发情况下当槽点为空的时候,如果CAS操作失败，则进入else 流程
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;

            // 当Table 为空的时候初始化
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();

            // 如果索引位置的值为空,CAS 操作设置新值
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;   // no lock when adding to empty bin
            }
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;

                // 锁住当前槽点
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                // 判断链表是否需要转化为红黑树
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }
```
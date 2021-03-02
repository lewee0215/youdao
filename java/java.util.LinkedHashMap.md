# LinkedHashMap
https://blog.csdn.net/varyall/article/details/82319443  
LinkedHashMap 保存了记录的插入顺序，在用Iterator遍历LinkedHashMap时，先得到的记录肯定是先插入的

## LinkedHashMap#put()方法
在LinkedHashMap类使用的仍然是父类HashMap的put方法，所以插入节点对象的流程基本一致。不同的是，LinkedHashMap重写了afterNodeInsertion和afterNodeAccess方法
```java
void afterNodeAccess(Node<K,V> e) { // move node to last
    LinkedHashMap.Entry<K,V> last;
    if (accessOrder && (last = tail) != e) {
        LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.after = null;
        if (b == null)
            head = a;
        else
            b.after = a;
        if (a != null)
            a.before = b;
        else
            last = b;
        if (last == null)
            head = p;
        else {
            p.before = last;
            last.after = p;
        }
        tail = p;
        ++modCount;
    }
}
```

## LinkedHashMap#remove()方法
LinkedHashMap重写了afterNodeRemoval方法，用于在删除节点的时候，调整双链表的结构
```java
void afterNodeRemoval(Node<K,V> e) { // unlink
    LinkedHashMap.Entry<K,V> p =
        (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
    p.before = p.after = null;
    if (b == null)
        head = a;
    else
        b.after = a;
    if (a == null)
        tail = b;
    else
        a.before = b;
}
```
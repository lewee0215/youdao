# CAS 基本用法
https://blog.csdn.net/Hsuxu/article/details/9467651  
CAS 操作包含三个操作数 —— 内存位置（V）、预期原值（A）和新值(B)  
当且仅当预期值A和内存值V相同时，将内存值V修改为B，否则什么都不做
```java
public static void main(String[] args) {
    //主内存中 atomicInteger 的初始值为 5
    AtomicInteger atomicInteger = new AtomicInteger(5);

    // 如果初始值是5，那么将初始值修改为1024，然后得修改后的值
    System.out.println(atomicInteger.compareAndSet(5, 1024) + "主内存中的最终值为: " + atomicInteger.get());

    System.out.println(atomicInteger.compareAndSet(5, 2048) + "主内存中的最终值为: " + atomicInteger.get());
}

/**
 * this：当前对象
 * valueOffSet：当前对象的内存地址偏移量，就是this的内存地址
*/
public final boolean compareAndSet(int expect, int update) {   
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
}
```
JVM 中 CAS 通过调用JNI的代码实现的,借助 C 来调用CPU底层指令实现的

## CAS缺点
https://blog.csdn.net/hsuxu/article/details/9467651  
CAS虽然很高效的解决原子操作，但是CAS仍然存在三大问题。ABA问题，循环时间长开销大和只能保证一个共享变量的原子操作

> 1> ABA问题   

因为CAS需要在操作值的时候检查下值有没有发生变化，如果没有发生变化则更新，但是如果一个值原来是A，变成了B，又变成了A，那么使用CAS进行检查时会发现它的值没有发生变化，但是实际上却变化了。ABA问题的解决思路就是使用版本号。在变量前面追加上版本号，每次变量更新的时候把版本号加一，那么A－B－A 就会变成1A-2B－3A。  

从Java1.5开始JDK的atomic包里提供了一个类AtomicStampedReference来解决ABA问题。这个类的compareAndSet方法作用是首先检查当前引用是否等于预期引用，并且当前标志是否等于预期标志，如果全部相等，则以原子方式将该引用和该标志的值设置为给定的更新值。  

关于ABA问题参考文档: http://blog.hesey.net/2011/09/resolve-aba-by-atomicstampedreference.html

> 2> 循环时间长开销大  

自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销
```java
// CountDownLatch 核心方法
protected boolean tryReleaseShared(int releases) {
    // Decrement count; signal when transition to zero
    for (;;) {
        int c = getState();
        if (c == 0)
            return false;
        int nextc = c-1;
        if (compareAndSetState(c, nextc))
            return nextc == 0;
    }
}
```
如果JVM能支持处理器提供的pause指令那么效率会有一定的提升  
pause指令有两个作用，第一它可以延迟流水线执行指令（de-pipeline）,使CPU不会消耗过多的执行资源，延迟的时间取决于具体实现的版本，在一些处理器上延迟时间是零。第二它可以避免在退出循环的时候因内存顺序冲突（memory order violation）而引起CPU流水线被清空（CPU pipeline flush），从而提高CPU的执行效率。

> 3> 只能保证一个共享变量的原子操作  

当对一个共享变量执行操作时，我们可以使用循环CAS的方式来保证原子操作，但是对多个共享变量操作时，循环CAS就无法保证操作的原子性，这个时候就可以用锁，或者有一个取巧的办法，就是把多个共享变量合并成一个共享变量来操作。比如有两个共享变量i＝2,j=a，合并一下ij=2a，然后用CAS来操作ij。从Java1.5开始JDK提供了AtomicReference类来保证引用对象之间的原子性，你可以把多个变量放在一个对象里来进行CAS操作

### AtomicReference
https://blog.csdn.net/qq_37385585/article/details/112778251  
将 Value 和 Stamp 包装成为 Pair 对象进行整体比较 

```java
private static void testStamped() {
    AtomicStampedReference<Integer> stampedReference = new AtomicStampedReference<>(5, 1);
    boolean result = true;
    
    result = stampedReference.compareAndSet(5,10,1,2);
    System.out.println(MessageFormat.format("Reference result:{0};value:{1};version:{2}",result, stampedReference.getReference(),stampedReference.getStamp()));
    
    result = stampedReference.compareAndSet(10,5,2,3);
    System.out.println(MessageFormat.format("Reference result:{0};value:{1};version:{2}",result,  stampedReference.getReference(),stampedReference.getStamp()));
    
    result = stampedReference.compareAndSet(5, 100, 1, 2);
    System.out.println(MessageFormat.format("Reference result:{0};value:{1};version:{2}",result,  stampedReference.getReference(),stampedReference.getStamp()));
}


/**
 * expectedReference the expected value of the reference
 * newReference the new value for the reference
 * expectedStamp the expected value of the stamp
 * newStamp the new value for the stamp
 */
public boolean compareAndSet(V   expectedReference,V   newReference,int expectedStamp,int newStamp) {
    Pair<V> current = pair;
    return
        expectedReference == current.reference &&
        expectedStamp == current.stamp &&
        ((newReference == current.reference &&
            newStamp == current.stamp) ||
            casPair(current, Pair.of(newReference, newStamp)));
}

private boolean casPair(Pair<V> cmp, Pair<V> val) {
    return UNSAFE.compareAndSwapObject(this, pairOffset, cmp, val);
}
```
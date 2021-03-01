# Java Thread 线程基础类
https://blog.csdn.net/u012068483/article/details/106879730

Thread.class 线程 6 种状态枚举：
https://blog.csdn.net/m0_37779570/article/details/84938476
```Java
public enum State {
    NEW,        /*** 新建：线程对象被创建后，就进入了新建状态*/ 

    RUNNABLE,   /*** 可运行的：复合状态。它包括两个子状态：READY和RUNNING; 线程对象被创建后，其它线程调用了该对象的start()方法，从而来启动该线程。随时可能被CPU调度执行*/ 

    BLOCKED,    /*** 阻塞： 一个线程发起一个阻塞式I/O（Blocking I/O）操作后，或者申请一个又其他线程持有的独占资源（比如锁）时，相应的线程会处于该状态*/

    WAITING,    /*** 等待: 能够让线程处于WAITING状态的方法有：Object.wait(),Thread.join()和LockSupport.park(Object)*/

    TIMED_WAITING,  /*** 有限等待*/
    
    TERMINATED; /*** 终止：已经执行结束的线程处于该状态*/
}
```

* sleep(time)  
使一个正在运行的线程处于睡眠状态，不会释放锁资源，  
如果有synchronized同步块，其他线程仍然不能访问共享数据  

* wait()、notify()、notifyAll()  

    - wait：使一个线程处于等待状态，释放CPU和锁资源  
Causes the current thread to wait until either another thread invokes  
The current thread must own this object's monitor  
<br>
    - notify：唤醒线程  
Wakes up a single thread that is waiting on this object's monitor  
<br>
\## wait()、notify()、notifyAll() 三个方法必须在 <font color='red'> synchronized</font> 语句块内使用,wait 和 notify必须配套使用，即必须使用同一把锁调用  
<br>
\## wait 与 sleep 的不同之处在于：wait会释放对象的"锁标志"

```Java
synchronized (obj) {
    while (<condition does not hold>)
        obj.wait(timeout);
    ... // Perform action appropriate to condition     /*  */
}
```
notify() 方法随机唤醒对象的等待池中的一个线程，进入锁池；notifyAll() 唤醒对象的等待池中的所有线程，进入锁池

* yield()  
yield方法仅释放CPU执行权，锁仍然占用，yield()方法不会释放锁，从而让其他具有相同优先级的等待线程获取执行权，线程会被放入就绪队列，会在短时间内再次执行

* Join()
调用线程等待该线程完成后，才能继续向下运行

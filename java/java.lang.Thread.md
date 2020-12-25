# Java Thread 线程基础类
https://blog.csdn.net/u012068483/article/details/106879730

Thread.class 线程 6 种状态枚举：
```Java
public enum State {
    NEW,        /*** 新建*/
    RUNNABLE,   /*** 可运行的*/
    BLOCKED,    /*** 阻塞*/
    WAITING,    /*** 等待*/
    TIMED_WAITING,  /*** 有限等待*/
    TERMINATED; /*** 终止*/
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


* yield()  
yield方法仅释放CPU执行权，锁仍然占用，yield()方法不会释放锁，从而让其他具有相同优先级的等待线程获取执行权，线程会被放入就绪队列，会在短时间内再次执行

* Join()
调用线程等待该线程完成后，才能继续向下运行

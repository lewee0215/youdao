# ThreadPoolExecutor
https://www.cnblogs.com/dolphin0520/p/3932921.html

```java
public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,
    BlockingQueue<Runnable> workQueue,ThreadFactory threadFactory,RejectedExecutionHandler handler);
```
| 参数             | 名称            | 使用说明   |
| :-               | -:             | :-:       |
| corePoolSize     | 核心池的大小    | 当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中        |
| maximumPoolSize  | 线程池最大线程数 | 表示在线程池中最多能创建多少个线程       |
| keepAliveTime    | 存活时间        | --       |
| unit             | 时间单位        | --       |
| workQueue        | 阻塞队列        | 一般使用LinkedBlockingQueue和SynchronousQueue       |
| threadFactory    | 线程工厂        | 主要用来创建线程        |
| handler          | 拒绝策略        | --       |


## 线程池线程执行流程
https://www.cnblogs.com/dolphin0520/p/3932921.html  

1. 默认情况下，线程池中并没有任何线程，而是等待有任务到来才创建线程去执行任务，除非调用了prestartAllCoreThreads()或者prestartCoreThread()方法，从这2个方法的名字就可以看出，是预创建线程的意思，即在没有任务到来之前就创建corePoolSize个线程或者一个线程

2. 如果当前线程池中的线程数目小于corePoolSize，则每来一个任务，就会创建一个线程去执行这个任务；

3. 如果当前线程池中的线程数目>=corePoolSize，则每来一个任务，会尝试将其添加到任务缓存队列当中，若添加成功，则该任务会等待空闲线程将其取出去执行；若添加失败（一般来说是任务缓存队列已满），则会尝试创建新的线程去执行这个任务；

4. 如果当前线程池中的线程数目达到maximumPoolSize，则会采取任务拒绝策略进行处理；

5. 如果线程池中的线程数量大于 corePoolSize时，如果某线程空闲时间超过keepAliveTime，线程将被终止，直至线程池中的线程数目不大于corePoolSize；如果允许为核心池中的线程设置存活时间，那么核心池中的线程空闲时间超过keepAliveTime，线程也会被终止

## 线程池阻塞队列 
https://www.cnblogs.com/dolphin0520/p/3932921.html  
workQueue的类型为BlockingQueue<Runnable>，通常可以取下面三种类型：
1. ArrayBlockingQueue：基于数组的先进先出队列，此队列创建时必须指定大小；
2. LinkedBlockingQueue：基于链表的先进先出队列，如果创建时没有指定此队列大小，则默认为Integer.MAX_VALUE；
3. synchronousQueue：这个队列比较特殊，它不会保存提交的任务，而是将直接新建一个线程来执行新来的任务

## 4 种拒绝策略
https://www.cnblogs.com/dolphin0520/p/3932921.html  

ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。 
ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。 
ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务 

## Executors
https://blog.csdn.net/weixin_40304387/article/details/80508236
提供了一系列工厂方法用于创建线程池，返回的线程池都实现了ExecutorService接口

```java
public class Executors {
    // 默认拒绝策略
    // RejectedExecutionHandler defaultHandler = new AbortPolicy()

    // 创建固定数目线程的线程池
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }

    // 创建一个可缓存的线程池，调用execute 将重用以前构造的线程（如果线程可用）。如果没有可用的线程，则创建一个新线程并添加到池中。终止并从缓存中移除那些已有 60 秒钟未被使用的线程
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }

    // 创建一个单线程化的Executor
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }

    // 创建一个支持定时及周期性的任务执行的线程池，多数情况下可用来替代Timer类
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * java.util.concurrent.ScheduledThreadPoolExecutor
    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
              new DelayedWorkQueue());
    }
    */
}
```

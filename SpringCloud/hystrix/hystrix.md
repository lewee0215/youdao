# Hystrix熔断机制原理

![](https://img2020.cnblogs.com/blog/1908853/202005/1908853-20200519172553534-848024592.png)

hystrix的工作流程：  
https://www.cnblogs.com/littlewhiterabbit/p/12918142.html  

1. 将远程服务调用逻辑封装进一个HystrixCommand。
2. 对于每次服务调用可以使用同步或异步机制，对应执行execute()或queue()。
3. 判断熔断器(circuit-breaker)是否打开或者半打开状态，如果打开跳到步骤8，进行回退策略，如果关闭进入步骤4。
4. 判断线程池/队列/信号量（使用了舱壁隔离模式）是否跑满，如果跑满进入回退步骤8，否则继续后续步骤5。
5. run方法中执行了实际的服务调用。 
   * a. 服务调用发生超时时，进入步骤8。
6. 判断run方法中的代码是否执行成功。 
   * a. 执行成功返回结果。   
   * b. 执行中出现错误则进入步骤8。
7. 所有的运行状态(成功，失败，拒绝，超时)上报给熔断器，用于统计从而影响熔断器状态。
8. 进入getFallback()回退逻辑。 
   * a. 没有实现getFallback()回退逻辑的调用将直接抛出异常。 
   * b. 回退逻辑调用成功直接返回。 
   * c. 回退逻辑调用失败抛出异常。
9. 返回执行成功结果

## 隔离机制
https://www.jianshu.com/p/684b04b6c454 

### 基于线程池的隔离
线程池隔离模式，会根据服务划分出独立的线程池，系统资源的线程并发数是有限的  

### 基于信号量的隔离
https://www.jianshu.com/p/fc19f6ed6d0d   
HystrixCommand和HystrixObservableCommand在两个地方支持信号量：  
1. 失败回退逻辑：当 Hystrix 需要执行失败回退逻辑时，其在调用线程（Tomcat 线程）中使用信号量
2. 执行命令时：如果设置了 Hystrix 命令的execution.isolation.strategy属性为SEMAPHORE，则 Hystrix 会使用信号量而不是线程池来控制调用线程调用依赖服务的并发度

设置 Hystrix 命令的execution.isolation.strategy属性为SEMAPHORE，则 Hystrix 会使用信号量而不是线程池来控制调用线程调用依赖服务的并发度

基于信号量的隔离方式非常地简单，其核心就是使用共用变量semaphore进行原子操作，控制线程的并发量，当并发量达到一定量级时，服务禁止调用

## 请求缓存

## 请求合并
如果线程或队列（非线程池模式下是信号量）已满，将不会执行命令，而是直接执行fallback
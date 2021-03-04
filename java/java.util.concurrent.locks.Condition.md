# Java锁- lock中使用多条件 condition
https://blog.csdn.net/chenchaofuck1/article/details/51592429  
Lock 替代了 synchronized 方法和语句的使用，Condition 替代了 Object 通信方法的使用  

## Condition 特性
1. 在Condition中，用await()替换wait()，用signal()替换notify()，用signalAll()替换notifyAll()  

2. Condition可以为多个线程间建立不同的Condition  
使用synchronized/wait()只有一个阻塞队列，notifyAll会唤起所有阻塞队列下的线程，  
使用lock/condition，可以实现多个阻塞队列，signalAll只会唤起某个阻塞队列下的阻塞线程

## ArrayBlockingQueue 使用 condition
```java
public ArrayBlockingQueue(int capacity, boolean fair) {
 
    if (capacity <= 0)
      throw new IllegalArgumentException();
    //创建数组  
    this.items = new Object[capacity];

    //创建锁和阻塞条件
    lock = new ReentrantLock(fair);  
    notEmpty = lock.newCondition();
    notFull = lock.newCondition();
}

//添加元素的方法
public void put(E e) throws InterruptedException {
    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == items.length)
            notFull.await(); //如果队列满就进行阻塞
        enqueue(e); //如果队列不满就入队
    } finally {
        lock.unlock();
    }
}
```

## Condition 实现生产者/消费者模型
https://blog.csdn.net/chenchaofuck1/article/details/51592429
```java
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Buffer {
	private  final Lock lock;
	private  final Condition notFull;
	private  final Condition notEmpty;
	private int	maxSize;
	private List<Date> storage;
	Buffer(int size){
		//使用锁lock，并且创建两个condition，相当于两个阻塞队列
		lock=new ReentrantLock();
		notFull=lock.newCondition();
		notEmpty=lock.newCondition();
		maxSize=size;
		storage=new LinkedList<>();
	}
	public void put()  {
		lock.lock();
		try {   
			while (storage.size() ==maxSize ){//如果队列满了
				System.out.print(Thread.currentThread().getName()+": wait \n");;
				notFull.await();//阻塞生产线程
			}
			storage.add(new Date());
			System.out.print(Thread.currentThread().getName()+": put:"+storage.size()+ "\n");
			Thread.sleep(1000);			
			notEmpty.signalAll();//唤醒消费线程
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{	
			lock.unlock();
		}
	}

	public	void take() {		
		lock.lock();
		try {  
			while (storage.size() ==0 ){//如果队列满了
				System.out.print(Thread.currentThread().getName()+": wait \n");;
				notEmpty.await();//阻塞消费线程
			}
			Date d=((LinkedList<Date>)storage).poll();
			System.out.print(Thread.currentThread().getName()+": take:"+storage.size()+ "\n");
			Thread.sleep(1000);			
			notFull.signalAll();//唤醒生产线程
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			lock.unlock();
		}
	} 
}

class Producer implements Runnable{
	private Buffer buffer;
	Producer(Buffer b){
		buffer=b;
	}
	@Override
	public void run() {
		while(true){
			buffer.put();
		}
	}	
}
class Consumer implements Runnable{
	private Buffer buffer;
	Consumer(Buffer b){
		buffer=b;
	}
	@Override
	public void run() {
		while(true){
			buffer.take();
		}
	}	
}
public class Main{
	public static void main(String[] arg){
		Buffer buffer=new Buffer(10);
		Producer producer=new Producer(buffer);
		Consumer consumer=new Consumer(buffer);
		for(int i=0;i<3;i++){
			new Thread(producer,"producer-"+i).start();
		}
		for(int i=0;i<3;i++){
			new Thread(consumer,"consumer-"+i).start();
		}
	}
}
```




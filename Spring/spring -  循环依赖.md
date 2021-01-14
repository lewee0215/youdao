# Spring 循环依赖
https://blog.csdn.net/u010853261/article/details/77940767
循环依赖其实就是循环引用，也就是两个或则两个以上的bean互相持有对方，最终形成闭环  

Spring中循环依赖场景有：
（1）构造器的循环依赖
（2）field属性的循环依赖

# SpringBean 生命周期
1. createBeanInstance：实例化，其实也就是调用对象的构造方法实例化对象
2. populateBean：填充属性，这一步主要是多bean的依赖属性进行填充
3. initializeBean：调用spring xml中的init 方法  

从单例bean初始化步骤我们可以知道，循环依赖主要发生在第一、第二部。也就是构造器循环依赖和field循环依赖

# Spring 循环依赖解决方案
Spring为了解决单例的循环依赖问题，使用了三级缓存
```java
// 已经创建完成的单例对象cache
/** Cache of singleton objects: bean name --> bean instance */
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(256);

// 单例对象工厂的cache
/** Cache of singleton factories: bean name --> ObjectFactory */
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<String, ObjectFactory<?>>(16);

// 提前曝光的单例对象的Cache
/** Cache of early singleton objects: bean name --> bean instance */
private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>(16);

// 创建过程，获取单例对象的方法
// org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#getSingleton
protected Object getSingleton(String beanName, boolean allowEarlyReference) {

    // 1. 首先从一级缓存singletonObjects中获取已经创建完成的对象
    Object singletonObject = this.singletonObjects.get(beanName);
    if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
        synchronized (this.singletonObjects) {

            // 2. 从二级缓存earlySingletonObjects中获取
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (singletonObject == null && allowEarlyReference) {
                ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                if (singletonFactory != null) {

                    // 3. 从三级缓存singletonFactory.getObject()获取
                    // 如果获取成功则从singletonFactories中移除，并放入earlySingletonObjects中。其实也就是从三级缓存移动到了二级缓存
                    singletonObject = singletonFactory.getObject();
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.singletonFactories.remove(beanName);
                }
            }
        }
    }
    return (singletonObject != NULL_OBJECT ? singletonObject : null);
}
```

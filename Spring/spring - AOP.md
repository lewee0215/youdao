# AOP - CGLIB
https://blog.csdn.net/yhl_jxy/article/details/80633194

```java
import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;
public class Client {
    public static void main(String[] args) {
        // 代理类class文件存入本地磁盘方便我们反编译查看源码
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\code");
        // 通过CGLIB动态代理获取代理对象的过程
        Enhancer enhancer = new Enhancer();
        // 设置enhancer对象的父类
        enhancer.setSuperclass(HelloService.class);
        // 设置enhancer的回调对象
        enhancer.setCallback(new MyMethodInterceptor());
        // 创建代理对象
        HelloService proxy= (HelloService)enhancer.create();
        // 通过代理对象调用目标方法
        proxy.sayHello();
    }
}

public class HelloService {
	public HelloService() {
        System.out.println("HelloService构造");
    }
 
    /**
     * 该方法不能被子类覆盖,Cglib是无法代理final修饰的方法的
     */
    final public String sayOthers(String name) {
        System.out.println("HelloService:sayOthers>>"+name);
        return null;
    }
 
    public void sayHello() {
        System.out.println("HelloService:sayHello");
    }

}

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import java.lang.reflect.Method;
public class MyMethodInterceptor implements MethodInterceptor{
 
    /**
     * sub：cglib生成的代理对象
     * method：被代理对象方法
     * objects：方法入参
     * methodProxy: 代理方法
     */
    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        System.out.println("======插入前置通知======");
        Object object = methodProxy.invokeSuper(proxy, args);
        System.out.println("======插入后者通知======");
        return object;
    }
}

```
## 子类代理字节码
https://blog.csdn.net/yhl_jxy/article/details/80633194  
```java
public final void sayHello() {
    MethodInterceptor var10000 = this.CGLIB$CALLBACK_0;
    if (var10000 == null) {
        CGLIB$BIND_CALLBACKS(this);
        var10000 = this.CGLIB$CALLBACK_0;
    }

    if (var10000 != null) {
        var10000.intercept(this, CGLIB$sayHello$0$Method, CGLIB$emptyArgs, CGLIB$sayHello$0$Proxy);
    } else {
        // 调用父类原始方法
        super.sayHello();
    }
}
```
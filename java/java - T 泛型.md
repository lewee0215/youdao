# Java <T> 泛型实现原理
https://blog.csdn.net/qq838642798/article/details/75948592
泛型提供了编译时类型安全检测机制，该机制允许程序员在编译时检测到非法的类

在编译成字节码时编译器把所有T类型替换成基础类Object  
当我们真正用到T a这个成员时，编译器在编译时会在代码前面加上类型转换检查指令checkcast，把 a 转化成如 String 类型。然后再调用String类型的相关方法，这种编译器私自添加向下类型转换机制便称为泛型的类型擦除机制

## 类型擦除
Java 中的泛型基本上都是在编译器这个层次来实现的。在生成的Java 字节代码中是不包含泛型中的类型信息的。使用泛型的时候加上的类型参数，会被编译器在编译的时候去掉。这个过程就称为类型擦除。

如在代码中定义的List<Object>和List<String>等类型，在编译之后都会变成List。JVM看到的只是List，而由泛型附加的类型信息对JVM来说是不可见的。

类型擦除的基本过程也比较简单，首先是找到用来替换类型参数的具体类。这个具体类一般是Object。如果指定了类型参数的上界的话，则使用这个上界。把代码中的类型参数都替换成具体的类

## checkcast 指令
```java
/**
 * 代码正常运行
 */
public class Test3 {
    public static void main(String args[]) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
        ArrayList<Integer> array=new ArrayList<Integer>();    
        array.add(1); //这样调用add方法只能存储整形，因为泛型类型的实例为Integer    
        array.getClass().getMethod("add", Object.class).invoke(array, "asd");    
        for (int i=0;i<array.size();i++) { 
            System.out.println(array.get(i));
        }  
    }
}

/**
 * 代码异常运行, checkcast类型转换检查，由于asd是string, 强制转integer报错
 */
public class Test3 {
    public static void main(String args[]) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
        ArrayList<Integer> array=new ArrayList<Integer>();    
        array.add(1); //这样调用add方法只能存储整形，因为泛型类型的实例为Integer    
        array.getClass().getMethod("add", Object.class).invoke(array, "asd");    
        for (int i=0;i<array.size();i++) { 
            Integer a=array.get(i);
            System.out.println(a);
        }  
    }
}
```
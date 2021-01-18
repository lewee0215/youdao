# Java <T> 泛型实现原理
https://blog.csdn.net/qq838642798/article/details/75948592

在编译成字节码时编译器把所有T类型替换成基础类Object  
当我们真正用到T a这个成员时，编译器在编译时会在代码前面加上类型转换检查指令checkcast，把 a 转化成如 String 类型。然后再调用String类型的相关方法，这种编译器私自添加向下类型转换机制便称为泛型的类型擦除机制

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
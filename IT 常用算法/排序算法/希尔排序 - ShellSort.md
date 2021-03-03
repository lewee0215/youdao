# 希尔排序
https://www.cnblogs.com/luomeng/p/10592830.html  
希尔排序(Shell’s Sort)是插入排序的一种，是直接插入排序算法的一种更高版本的改进版本

![](https://img2018.cnblogs.com/blog/1469176/201903/1469176-20190325113805986-2099566413.png)

## 排序过程
1. 把记录按步长gap分组，对每组记录采用直接插入排序方法进行排序；
2. 随着步长逐渐减小，所分成的组包含的记录越来越多；当步长值减小到1时，整个数据合成一组，构成一组有序记录，完成排序；

```java
/**
 * 希尔排序演示
 * @author Lvan
 */
public class ShellSort {
    public static void main(String[] args) {
        int[] arr = {5, 1, 7, 3, 1, 6, 9, 4};
        shellSort(arr);

        for (int i : arr) {
            System.out.print(i + "\t");
        }
    }

    private static void shellSort(int[] arr) {
        //step:步长
        for (int step = arr.length / 2; step > 0; step /= 2) {
            //对一个步长区间进行比较 [step,arr.length)
            for (int i = step; i < arr.length; i++) {
                int value = arr[i];
                int j;

                //对步长区间中具体的元素进行比较
                for (j = i - step; j >= 0 && arr[j] > value; j -= step) {
                    //j为左区间的取值，j+step为右区间与左区间的对应值。
                    arr[j + step] = arr[j]; 
                }
                //此时step为一个负数，[j + step]为左区间上的初始交换值
                arr[j + step] = value;  
            }
        }
    }
}
```
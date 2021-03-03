# 快速排序
https://cloud.tencent.com/developer/article/1693982  
快速排序（Quicksort）是对冒泡排序的一种改进

![](https://upload-images.jianshu.io/upload_images/2463290-5bd80beb2b448d53.gif?imageMogr2/auto-orient/strip|imageView2/2/w/811/format/webp)

## 排序过程
https://www.cnblogs.com/aademeng/articles/11129553.html  
①以第一个关键字 K 1 为控制字，将 [K 1 ,K 2 ,…,K n ] 分成两个子区，使左区所有关键字小于等于 K 1 ，右区所有关键字大于等于 K 1 ，最后控制字居两个子区中间的适当位置。在子区内数据尚处于无序状态。  

②把左区作为一个整体，用①的步骤进行处理，右区进行相同的处理。（即递归）  

③重复第①、②步，直到左区处理完毕

```java
public static void quickSort(int[] arr){
    qsort(arr, 0, arr.length-1);
}
private static void qsort(int[] arr, int low, int high){
    if (low < high){
        int pivot=partition(arr, low, high);        //将数组分为两部分
        qsort(arr, low, pivot-1);                   //递归排序左子数组
        qsort(arr, pivot+1, high);                  //递归排序右子数组
    }
}
private static int partition(int[] arr, int low, int high){
    int pivot = arr[low];     //枢轴记录
    while (low<high){
        while (low<high && arr[high]>=pivot) --high;
        arr[low]=arr[high];             //交换比枢轴小的记录到左端
        while (low<high && arr[low]<=pivot) ++low;
        arr[high] = arr[low];           //交换比枢轴小的记录到右端
    }
    //扫描完成，枢轴到位
    arr[low] = pivot;
    //返回的是枢轴的位置
    return low;
}
```
# 计数排序
![](https://upload-images.jianshu.io/upload_images/2463290-00a375c4635d3629.gif?imageMogr2/auto-orient/strip|imageView2/2/w/1012/format/webp)

计数排序是一个非基于比较的线性时间排序算法，是一种稳定排序的算法。  
基本思想是对于给定的输入序列中的每一个元素 x，确定该序列中值小于 x 的元素的个数。一旦确定了这一信息，就可以将 x 直接存放到最终的输出序列的正确位置上

## 排序过程
https://www.jianshu.com/p/a2f47d9037f4

> 待排序数据：array = {7,6,8,6,2,3,7,8,1,0}

1. 选出数组的最大值max =8 ，min=0，创建一个(max-min+1) = 9 长度的数组countArray，将所有元素初始化为0

![](https://upload-images.jianshu.io/upload_images/10933846-2f0ab04ce8e4c415.png?imageMogr2/auto-orient/strip|imageView2/2/w/1024/format/webp)

![](https://upload-images.jianshu.io/upload_images/10933846-8e65d9c2f8d6d4c5.png?imageMogr2/auto-orient/strip|imageView2/2/w/1054/format/webp)

下标Index表示元素值, countArray[Index] 表示出现次数

2. 对countArray进行循环，对每一个元素countArray[i] = countArray[i] + countArray[i-1]，目的是统计每一个元素前面有多少个小于它的元素

![](https://upload-images.jianshu.io/upload_images/10933846-434ff5521f5c25f8.png)  

3.复制array数组存到temp中，循环temp，将temp中i位置的的元素放到array中的 --countArray[temp[i]] 位置

```java
array = {7,6,8,6,2,3,7,8,1,0}
temp = {7,6,8,6,2,3,7,8,1,0}

countArray = {1,2,3,4,4,4,6,8,10}

// 循环temp
for i = 0; 
temp[i] = 7;  // 数值
countArray[temp[i]]=countArray[7]=8  // 定位

// 相同的数字，出现一次，就在对应的下标位置上+1
```
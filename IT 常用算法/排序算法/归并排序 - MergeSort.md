# 归并排序
![](https://upload-images.jianshu.io/upload_images/2463290-55f1bb4a59bdf501.gif?imageMogr2/auto-orient/strip|imageView2/2/w/811/format/webp)

# 归并算法工作原理
https://www.cnblogs.com/of-fanruice/p/7678801.html  
分而治之(divide - conquer);每个递归过程涉及三个步骤  
1. 分解: 把待排序的 n 个元素的序列分解成两个子序列, 每个子序列包括 n/2 个元素.
2. 治理: 对每个子序列分别调用归并排序MergeSort, 进行递归操作
3. 合并: 合并两个排好序的子序列,生成排序结果

![](https://images2017.cnblogs.com/blog/1216886/201710/1216886-20171016205026677-2055745920.jpg)

注: 核心思想为 合并两个有序数组
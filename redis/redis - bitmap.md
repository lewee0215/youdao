# Redis Bitmaps
https://blog.csdn.net/u011957758/article/details/74783347

bitmap就是通过最小的单位bit来进行(0|1)设置，表示某个元素对应的值或者状态  
redis中bitmap按 string 来存储, <font color='yellow'>8bit表示一个ascll字符</font> ,大小被限制在512MB之内,所以最大是2^32位,如果超过2^32内需要分片存储

## Bitmaps Commands
### setbit key offset value
> 说明：给一个指定key的值得第offset位 赋值为value, 返回值为 oldvalue

### getbit key offset value
> 说明：返回一个指定key的二进制信息; 当偏移量 OFFSET 比字符串值的长度大，或者 key 不存在时，返回 0 

### bitcount key start end
> 说明：返回一个指定key中位的值为1的个数(是以byte为单位不是bit)  
<font color='#dcdcaa'>
\## bitcount key 0 0 : 获取第一个 byte 字节中 1 的数量，注意是字节，第一个字节也就是1,2,3,4,5,6,7,8 共计 8 个 bit 位的数值
</font>

### bitOp
> 说明：对不同的二进制存储数据进行位运算(AND、OR、NOT、XOR)  
<font color='#dcdcaa'>
\## 除了 NOT 操作之外，其他操作都可以接受一个或多个 key 作为输入
</font>

## Bitmaps 操作 String
```sh
# ASCII 对照表
# a=0110 0001  A = 0100 0001

> set hello A
"OK"
> bitset hello 2 1
"0"
> get hello
"a"
```  

## Bitmaps 压缩算法

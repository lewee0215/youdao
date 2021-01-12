# Redis Geo
https://blog.csdn.net/K_Ohaha/article/details/101731565

> ### GEOADD key longitude latitude member [longitude latitude member …]  
> 将指定的地理空间位置（纬度、经度、名称）添加到指定key中。  
> 数据将存储到sorted set，为了方便使用 GEORADIUS 或 GEORADIUSBYMEMBER 命令，可以使用zrem 进行数据的删除

有效的经度从-180度到 180度  
有效的纬度从-85.05112878度 到 85.05112878度

> ### GEODIST key member1 member2 [unit]
> 返回两个给定位置之间的距离。
如果两个位置之间的其中一个不存在，那么命令返回空值  
> unit [m,km,mi(英里),ft(英尺)]

如果用户没有显示指定单位参数，默认使用米作为单位。
GEODIST命令在计算距离时会假设地球为完美球形，极限情况下，这一假设最大会造成0.5%的误差

> ### GEOHASH key member [member …]
> 返回一个或多个位置元素的Geohash表示

Redis使用Geohash技术的变体表示元素的位置，位置使用52位整数进行编码  
GEOHASH 命令返回11个字符的Geohash字符串，和内部的52位表示方法相比没有精度的损失

1. 它可以移除右边的字符以缩短长度，这只会导致精度的损失，但仍指向同一区域
2. 它可以在heohash.org网站使用，地址是http://geohash.org/
3. 前缀相似的字符串指向的位置离得很近，但这不代表前缀不同的字符串就离得很远

> ### GEOPOS key member [member …]
> 返回指定key中的指定位置信息
 

> ### GEORADIUS key longitude latitude radiusm|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH][COUNT count] [ASC|DESC] [STORE key][STOREDIST key]
> 以<font color='yellow'> 指定经纬度 </font>为中心，返回键包含的位置元素与中心距离不超过最大距离的所有位置元素  

    WITHDIST：在返回位置元素的同时，将位置元素与中心的距离也一并返回，单位与用户给定距离的单位一直  
    WITHCOORD：将位置元素的经度和纬度也一并返回  
    WITHHASH：以52位有符号整数的形式，返回位置元素经过原始geohash编码的有序集合分值。这个选项主要用于底层应用或调试  

命令默认返回结果未排序，可以指定ASC或DESC按距离排序  
COUNT表示指定返回元素的数量，如果不指定则返回全部符合的元素

> ### GEORADIUSBYMEMBER key member radiusm|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH][COUNT count] [ASC|DESC] [STORE key][STOREDIST key]
> 以<font color='yellow'> 指定key </font>为中心，返回键包含的位置元素与中心距离不超过最大距离的所有位置元素

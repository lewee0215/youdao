# Redis - Lua
https://dongshao.blog.csdn.net/article/details/106065655
* Lua脚本在Redis中是原子执行的，执行过程中间不会插入其他命令
* Lua脚本可以帮助开发和运维人员创造出自己定制的命令，并可以将这些命令常驻在Redis内存中，实现复用的效果
* Lua脚本可以将多条命令一次性打包，有效地减少网络开销

# lua_scripts 脚本字典 
https://blog.csdn.net/qq_16605855/article/details/83305942  

## SCRIPT LOAD
https://www.cnblogs.com/loveLands/articles/11826947.html  
SCRIPT LOAD 指令用于将客户端提供的 lua 脚本传递到服务器而不执行，但是会得到脚本的唯一 ID，这个唯一 ID 是用来唯一标识服务器缓存的这段 lua 脚本，它是由 Redis 使用 sha1 算法揉捏脚本内容而得到的一个很长的字符串。有了这个唯一 ID，后面客户端就可以通过 EVALSHA 指令反复执行这个脚本

# lua 脚本的伪客户端
https://www.cnblogs.com/xing901022/p/4872279.html  
lua_client伪客户端在服务器运行的整个生命期中会一直存在，只有服务器被关闭时，这 个客户端才会被关闭
```c++
struct redisServer {
    // https://blog.csdn.net/qq_41453285/article/details/103319004
    redisClient *lua_client;
    // ...
};
```


# lua_scripts 脚本字典 
https://blog.csdn.net/qq_16605855/article/details/83305942

## SCRIPT LOAD
https://www.cnblogs.com/loveLands/articles/11826947.html
SCRIPT LOAD 指令用于将客户端提供的 lua 脚本传递到服务器而不执行，但是会得到脚本的唯一 ID，这个唯一 ID 是用来唯一标识服务器缓存的这段 lua 脚本，它是由 Redis 使用 sha1 算法揉捏脚本内容而得到的一个很长的字符串。有了这个唯一 ID，后面客户端就可以通过 EVALSHA 指令反复执行这个脚本

# lua 脚本的伪客户端
https://www.cnblogs.com/xing901022/p/4872279.html


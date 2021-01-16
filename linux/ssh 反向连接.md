 Linux - SSH 反向连接
 https://blog.csdn.net/qq_21768483/article/details/78817891  

SSH 参数说明
https://www.cnblogs.com/bubaya/p/7828817.html  
| SSH 参数   | 说明    | 
| :-:        | :-       | 
| -f        | 后台运行       | 
| -C        | 允许数据压缩        | 
| -N        | 不实际连接而是做port forwarding        | 
| -R        | 做反向ssh        | 
| -L        | 将本地机(客户机)的某个端口转发到远端指定机器的指定端口        | 
 
## -R 使用详解
https://blog.csdn.net/qq_36119192/article/details/90733897  
```s
-R [bind_address:]port:host:hostport

## 本地端口转发,本地监听16379端口，将16379端口的流量都转发给6379端口
ssh -fCNL *:16379:localhost:6379 localhost 

## 反向连接, 将远程机器的 2222号端口的流量都转发给本地机器的8888端口
$ ssh -NfR 3333:localhost:22 username@servername -p 22

3333: remote port, 远程服务器的代理端口
servername: 远程服务器IP地址
```
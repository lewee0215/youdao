# Java - 5种IO模型  
https://www.jianshu.com/p/5257b540c3e5  

## Blocking IO
使用BIO socket编程时候调用socket.read()方法时整体的执行流程：  
1. 程序调用socket.read()，这个方法会调用一个native read()方法，最终是 OS执行read
2. OS得到read指令，命令网卡读取数据。
3. 网卡读取数据完成后，将数据传递给内核。
4. 内核把读取的数据拷贝的用户空间。
5. 程序解除阻塞，完成read函数

![](https://img-blog.csdnimg.cn/20210204212633826.gif)

在BIO中我们称作的阻塞IO，也就是阻塞在2个地方：
1. OS等待数据报准备好。
2. 将数据从内核空间拷贝到用户空间

## Non-Blocking IO
https://blog.csdn.net/historyasamirror/article/details/5778378  
当用户进程发出read操作时，如果kernel中的数据还没有准备好，那么它并不会block用户进程，而是立刻返回一个error

用户进程其实是需要不断的主动询问kernel数据是否准备完毕

![](https://img-blog.csdnimg.cn/20210204212635345.gif)

## IO multiplexing
IO Multiplex即IO多路复用，不同的操作系统有不同的实现：
如 Windows：selector ； Linux：epoll ；Mac：kqueue

select/epoll的好处就在于单个process就可以同时处理多个网络连接的IO。它的基本原理就是select/epoll这个function会不断的轮询所负责的所有socket，当某个socket有数据到达了，就通知用户进程

![](https://img-blog.csdnimg.cn/20210204212634290.gif)

## Signal driven IO

## Asynchronous IO
![](https://img-blog.csdnimg.cn/20210204212634315.gif)
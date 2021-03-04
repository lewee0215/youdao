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

## IO multiplexing = （event driven IO）
IO Multiplex即IO多路复用，不同的操作系统有不同的实现：
* Windows：selector 通过轮询FD_SETSIZE来问每一个socket的状态变化
* Linux：epoll 把socket与事件绑定在一起，当监听到socket变化时，回调相应的处理
* Mac：kqueue

https://blog.csdn.net/historyasamirror/article/details/5778378  
select/epoll的好处就在于单个process就可以同时处理多个网络连接的IO。
它的基本原理就是select/epoll这个function会不断的轮询所负责的所有socket，当某个socket有数据到达了，就通知用户进程

![](https://img-blog.csdnimg.cn/20210204212634290.gif)

## Signal driven IO

![](https://upload-images.jianshu.io/upload_images/11345047-ee919b818091fe7c.png)

## Asynchronous IO
在内核角度，当它受到一个asynchronous read之后，首先它会立刻返回，所以不会对用户进程产生任何block

kernel会等待数据准备完成，然后将数据拷贝到用户内存，当这一切都完成之后，kernel会给用户进程发送一个signal，告诉它read操作完成
![](https://img-blog.csdnimg.cn/20210204212634315.gif)
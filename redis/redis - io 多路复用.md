# IO多路复用的三种实现方法
https://blog.csdn.net/zhang_shuai_2011/article/details/7675797  
select，poll，epoll本质上都是同步I/O
1. select ----->windows linux unix（苹果内核）
2. poll ----->linux unix
3. epoll ----->linux unix

## select实现
1、使用copy_from_user从用户空间拷贝fd_set到内核空间

2、注册回调函数__pollwait

3、遍历所有fd，调用其对应的poll方法（对于socket，这个poll方法是sock_poll，sock_poll根据情况会调用到tcp_poll,udp_poll或者datagram_poll）

4、以tcp_poll为例，其核心实现就是__pollwait，也就是上面注册的回调函数。

5、__pollwait的主要工作就是把current（当前进程）挂到设备的等待队列中，不同的设备有不同的等待队列，对于tcp_poll来说，其等待队列是sk->sk_sleep（注意把进程挂到等待队列中并不代表进程已经睡眠了）。在设备收到一条消息（网络设备）或填写完文件数据（磁盘设备）后，会唤醒设备等待队列上睡眠的进程，这时current便被唤醒了。

6、poll方法返回时会返回一个描述读写操作是否就绪的mask掩码，根据这个mask掩码给fd_set赋值。

7、如果遍历完所有的fd，还没有返回一个可读写的mask掩码，则会调用schedule_timeout是调用select的进程（也就是current）进入睡眠。当设备驱动发生自身资源可读写后，会唤醒其等待队列上睡眠的进程。如果超过一定的超时时间（schedule_timeout指定），还是没人唤醒，则调用select的进程会重新被唤醒获得CPU，进而重新遍历fd，判断有没有就绪的fd。

8、把fd_set从内核空间拷贝到用户空间

## poll实现
poll的实现和select非常相似，只是描述fd集合的方式不同，poll使用pollfd结构而不是select的fd_set结构

## epoll实现
select和poll都只提供了一个函数——select或者poll函数。  
epoll提供了三个函数，epoll_create,epoll_ctl和epoll_wait，epoll_create是创建一个epoll句柄；epoll_ctl是注册要监听的事件类型；epoll_wait则是等待事件的产生
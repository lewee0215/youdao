RocketMQ 零拷贝原理
http://www.bubuko.com/infodetail-287396.html
零拷贝是指CPU不需要在应用内存和内核内存之间拷贝数据消耗CPU资源

OS 传统的数据拷贝(file->socket)
https://blog.csdn.net/happygan520/article/details/105429429
1. 读取系统调用导致上下文从用户模式切换到内核模式。DMA引擎执行从磁盘读取文件内容并将其存储到内核地址空间缓冲区中
2. 将数据从内核缓冲区复制到用户缓冲区，然后读取的系统调用返回。调用返回导致上下文从内核切换回用户模式
3. 写系统调用导致上下文从用户模式切换到内核模式,将用户缓冲区数据放入内核地址空间中的套接字专门缓冲区
4. write系统调用返回，DMA引擎将数据从内核缓冲区传递到Socket协议引擎

mmap
在 Linux 中，减少拷贝次数的一种方法是调用 mmap() 来代替调用 read
1. mmap系统调用使DMA引擎将文件内容复制到内核缓冲区中。然后与用户进程共享缓冲区，而无需在内核和用户内存空间之间执行任何复制
2. 写系统调用使内核将数据从原始内核缓冲区复制到与套接字关联的内核缓冲区中
3. DMA引擎将数据从内核套接字缓冲区传递到协议引擎

使用mmap + write方法时存在隐患：
当您在内存中映射文件时，然后调用write方法，但另一个进程将同一个文件截断时，总线错误信号SIGBUS将中断您的写入系统调用，因为您执行了错误的内存访问。该信号的默认行为是杀死进程并转储内核，这不是网络服务器最理想的操作

sendfile
为了简化用户接口，同时还要继续保留 mmap()/write() 技术的优点：减少 CPU 的复制次数，Linux 在版本 2.1 中引入了 sendfile() 这个系统调用
1. sendfile系统调用使DMA引擎将文件内容复制到内核缓冲区中。然后，数据被内核复制到与套接字关联的内核缓冲区中
2. 第三份复制发生在DMA引擎将数据从内核套接字缓冲区传递到协议引擎时

用户文件描述符表
每个进程中都有一个用户文件描述符表，表项指向一个全局的文件表中的某个表项，文件表表项有一个指向内存inode的**指针**，每个inode唯一标识一个文件。如果同时有多个进程打开同一文件，他们的用户文件描述符表项指向不同的文件表项，但是这些文件表项会指向同一个inode

page cache
**内核会为每个文件单独维护一个page cache**，用户进程对于文件的大多数读写操作会直接作用到page cache上，内核会选择在适当的时候将page cache中的内容写到磁盘上(一般会定时或者手工fsync控制回写（例如elasticsearch会定时5s从cache里刷到segment file日志文件），这样可以大大减少磁盘的访问次数，从而提高性能。
Page cache是linux内核文件访问过程中很重要的数据结构，page cache中会保存用户进程访问过得该文件的内容，这些内容以页为单位保存在内存中，当用户需要访问文件中的某个偏移量上的数据时，**内核会以偏移量为索引，找到相应的内存页**，如果该页没有读入内存，则需要访问磁盘读取数据。为了提高页得查询速度同时节省page cache数据结构占用的内存，linux内核使用树来保存page cache中的页
JAVA 使用零拷贝
文件通道FileChannel(java.nio)
FileChannel fileChannel = new RandomAccessFile(new File("data.txt"), "rw").getChannel()
- 全双工通道，可以同时读和写，采用内存缓冲区ByteBuffer且是线程安全的
一般情况FileChannel在一次写入4kb的整数倍数时，才能发挥出实际的性能，益于FileChannel采用了ByteBuffer这样的内存缓冲区。这样可以精准控制写入磁盘的大小，这是普通IO无法实现
- FileChannel是直接把ByteBuffer的数据直接写入磁盘？
ByteBuffer 中的数据和磁盘中的数据还隔了一层，这一层便是 PageCache，是用户内存和磁盘之间的一层缓存。
我们可以认为 filechannel.write 写入 PageCache 便是完成了落盘操作，但实际上，操作系统最终帮我们完成了 PageCache 到磁盘的最终写入， FileChannel 提供了一个 force() 方法，用于通知操作系统进行及时的刷盘，
使用FileChannel时同样经历磁盘->PageCache->用户内存三个阶段

内存映射MMAP(java.nio)
MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, position, fileSize)
- mmap 把文件映射到虚拟内存，省去了从PageCache复制到用户缓存的过程

FileChannel.transferTo()
由native方法transferTo0()来实现，它依赖底层操作系统的支持。在UNIX和Linux系统中，调用这个方法将会引起**sendfile()**系统调用。
File file = new File("test.zip");
RandomAccessFile raf = new RandomAccessFile(file, "rw");
FileChannel fileChannel = raf.getChannel();
SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("", 1234));
// 直接使用了transferTo()进行通道间的数据传输
fileChannel.transferTo(0, fileChannel.size(), socketChannel);



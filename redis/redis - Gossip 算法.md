# Gossip 算法
https://www.cnblogs.com/kaleidoscope/p/9630316.html  
gossip协议是p2p方式的通信协议。通过节点之间不断交换信息，一段时间后所有节点都会知道整个集群完整的信息  

## 通信过程
https://blog.csdn.net/u013565163/article/details/108752363
1. 每一个节点有两个TCP端口：一个client访问的端口，一个节点间通信端口，通信端口号等于client访问端口加10000
2. 每个节点在固定周期内通过特定规则选择几个节点发送ping消息。
3. 接受到ping消息的节点会用Pong消息作为响应

```java
// gossip协议消息-消息头
typedef struct {
    char sig[4];        // 消息标识 RCmb
    uint32_t totlen;    // 消息的总长度
    uint16_t ver;       // 协议版本 当前是 1
    uint16_t port;      // 基础端口号 client与server之间通信的端口
    uint16_t type;      // 消息类型
    uint16_t count;     // 如果是ping,pong表示消息体中的节点数
    uint64_t currentEpoch;  //当前发送节点的配置纪元
    uint64_t configEpoch;   // 主节点/从节点的主节点配置纪元
    uint64_t offset;   // 复制偏移量
    char sender[CLUSTER_NAMELEN];// 当前发送节点的nodeId
    unsigned char myslots[CLUSTER_SLOTS/8]; // 当前节点负责的槽信息
    char slaveof[CLUSTER_NAMELEN]; //如果发送节点是从节点，记录对应主节点的nodeId
    char myip[NET_IP_STR_LEN];   // 当前节点的ip
    char notused1[34];  /// 
    uint16_t cport;      //集群节点间通信端口
    uint16_t flags;      // 发送节点标识 区分主从、是否下线
    unsigned char state; // 发送节点所处的结群状态
    unsigned char mflags[3]; // 消息标识
    union clusterMsgData data; // 消息体
} clusterMsg;

#define CLUSTERMSG_TYPE_PING 0          /* Ping */
#define CLUSTERMSG_TYPE_PONG 1          /* Pong (reply to Ping) */
#define CLUSTERMSG_TYPE_MEET 2          /* Meet "let's join" message */
#define CLUSTERMSG_TYPE_FAIL 3          /* Mark node xxx as failing */

//gossip协议消息消息体
union clusterMsgData {
    /* PING, MEET and PONG */
    struct {
        // 数组类型携带多个节点的信息
        clusterMsgDataGossip gossip[1];
    } ping;

    // 失败节点信息
    struct {
        clusterMsgDataFail about;
    } fail;

    /* PUBLISH */
    struct {
        clusterMsgDataPublish msg;
    } publish;

    /* UPDATE */
    struct {
        clusterMsgDataUpdate nodecfg;
    } update;
};
```

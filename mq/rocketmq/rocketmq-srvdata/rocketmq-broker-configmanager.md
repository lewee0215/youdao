1. 加载topic配置${user.home}/store/config/topics.json
```java
{
  "dataVersion":{
    "counter":2,
    "timestatmp":1393729865073
  },
  "topicConfigTable":{
    //根据consumer的group生成的重试topic
    "%RETRY% group_name":{
      "perm":6,
      "readQueueNums":1,
      "topicFilterType":"SINGLE_TAG",
      "topicName":"%RETRY%group_name",
      "writeQueueNums":1
    },
　　"TopicTest":{
      "perm":6,  // 100读权限 , 10写权限   6是110读写权限
      "readQueueNums":8,
      "topicFilterType":"SINGLE_TAG",
      "topicName":"TopicTest",
      "writeQueueNums":8
    }
  }
}
``` 

2.加载消费进度偏移量  ${user.home}/store/config/consumerOffset.json
```java
{
  "offsetTable":{
    //重试队列消费进度为零
    "%RETRY% group_name@group_name":{0:0}, 
    // 分组名group_name消费topic为TopicTest的进度为：
    // 队列 queue=0  消费进度23
    // 队列 queue=2  消费进度为22  等等…

    "TopicTest@ group_name":{0:23,1:23,2:22,3:22,4:21,5:18,6:18,7:18} 
  } 
}
```
 
3. 加载消费者订阅关系 ${user.home}/store/config/subscriptionGroup.json
```java
{
  "dataVersion":{
    "counter":1,
    "timestatmp":1393641744664
  },
  "group_name":{
    "brokerId":0,  //0代表这台broker机器为master，若要设为slave值大于0
    "consumeBroadcastEnable":true,
    "consumeEnable":true,
    "consumeFromMinEnable":true,
    "groupName":"group_name",
    "retryMaxTimes":5,
    "retryQueueNums":1,
    "whichBrokerWhenConsumeSlowly":1 
  }
}
```
 
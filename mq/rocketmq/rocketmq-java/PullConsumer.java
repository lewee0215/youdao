package com.itutorgroup.tutorabc.pointsmall.config.rocketmq.consumer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PullConsumer {
	private final static Logger log = LoggerFactory.getLogger(PushConsumer.class);
	
    private static boolean runFlag = true;
    public static void main(String[] args) throws Exception {
        DefaultLitePullConsumer consumer = new DefaultLitePullConsumer("PullConsumerGroupName");
        consumer.setNamesrvAddr("127.0.0.1:9876");
        //要消费的topic，可使用tag进行简单过滤
        consumer.subscribe("TestTopic", "*");
        //一次最大消费的条数
        consumer.setPullBatchSize(100);
        //无消息时，最大阻塞时间。默认5000 单位ms
        consumer.setPollTimeoutMillis(5000);
        consumer.start();
        while (runFlag){
            try {
                //拉取消息，无消息时会阻塞 
                List<MessageExt> msgs = consumer.poll();
                if (CollectionUtils.isEmpty(msgs)){
                    continue;
                }
                //业务处理
                msgs.forEach(msg-> log.info(new String(msg.getBody())));
                //同步消费位置。不执行该方法，应用重启会存在重复消费。
                //consumer.offsetForTimestamp(messageQueue, timestamp);
                consumer.commitSync();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        consumer.shutdown();
    }
}
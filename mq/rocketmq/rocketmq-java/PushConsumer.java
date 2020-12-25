package com.itutorgroup.tutorabc.pointsmall.config.rocketmq.consumer;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class PushConsumer {
	private final static Logger log = LoggerFactory.getLogger(PushConsumer.class);
	
    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("PushConsumerGroupName");
        consumer.setNamesrvAddr("127.0.0.1:9876");
        //一个GroupName第一次消费时的位置
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setConsumeThreadMin(20);
        consumer.setConsumeThreadMax(20);
        //要消费的topic，可使用tag进行简单过滤
        consumer.subscribe("TestTopic", "*");
        //一次最大消费的条数
        consumer.setConsumeMessageBatchMaxSize(100);
        //消费模式，广播或者集群，默认集群。
        consumer.setMessageModel(MessageModel.CLUSTERING);
        //在同一jvm中 需要启动两个同一GroupName的情况需要这个参数不一样。
        consumer.setInstanceName("InstanceName");
        
        //配置消息监听
        //consumer.registerMessageListener(new MessageConcurrentlyConsumer());
        consumer.registerMessageListener(new MessageOrderlyConsumer());
        
        consumer.start();
        System.out.println("Consumer Started.");
        
        Thread.sleep(5*60*1000);
        consumer.shutdown();
    }
    
	public static class MessageConcurrentlyConsumer implements MessageListenerConcurrently {

		@Override
		public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
			if (CollectionUtils.isEmpty(msgs)) {
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			}

			MessageExt message = msgs.get(0);
			try {
				// 逐条消费
				String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
				System.err.println("Message Consumer: Handle New Message: messageId: " + message.getMsgId() + ",topic: "
						+ message.getTopic() + ",tags: " + message.getTags() + ",messageBody: " + messageBody);

				// 模拟耗时操作2分钟，大于设置的消费超时时间
				Thread.sleep(1000L * 60 * 2);
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			} catch (Exception e) {
				log.error("Consume Message Error!!", e);
				return ConsumeConcurrentlyStatus.RECONSUME_LATER;
			}
		}

	}
	
	public static class MessageOrderlyConsumer implements MessageListenerOrderly {
		@Override
		public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
			if (CollectionUtils.isEmpty(msgs)) {
				return ConsumeOrderlyStatus.SUCCESS;
			}

			MessageExt message = msgs.get(0);
			try {
				// 逐条消费
				String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
				System.err.println("Message Consumer: Handle New Message: messageId: " + message.getMsgId() + ",topic: "
						+ message.getTopic() + ",tags: " + message.getTags() + ",messageBody: " + messageBody);

				// 模拟耗时操作2分钟，大于设置的消费超时时间
				Thread.sleep(1000L * 60 * 2);
				return ConsumeOrderlyStatus.SUCCESS;
			} catch (Exception e) {
				log.error("Consume Message Error!!", e);
				return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
			}
		}

	}
}

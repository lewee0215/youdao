package com.itutorgroup.tutorabc.pointsmall.config.rocketmq.consumer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.trace.TraceBean;
import org.apache.rocketmq.client.trace.TraceContext;
import org.apache.rocketmq.client.trace.TraceType;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class Producer {
	private final static Logger logger = LoggerFactory.getLogger(Producer.class);
	
	public static void main(String[] args) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		String nameSrv= "172.16.235.244:9876";
		String topic = "vjr-points-mall-topic";
		String message = "hello";
		String tags = "order";
		sendSync(nameSrv,topic,message,tags);
	}
	
	private static void sendSync(String nameSrv,String topic ,String message,String tags) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
		// public DefaultMQProducer(final String producerGroup, boolean enableMsgTrace, final String customizedTraceTopic)
		DefaultMQProducer producer = new DefaultMQProducer("sync"/*,true*/);
		
		producer.getDefaultMQProducerImpl().registerSendMessageHook(new TraceSendMessageHook());
		producer.setNamesrvAddr(nameSrv);
		producer.setRetryTimesWhenSendFailed(3);
		producer.setRetryTimesWhenSendAsyncFailed(3);
		producer.start();
		
		for (int i=0;i<50;i++ ) {
			Message topicMessage = new Message();
			topicMessage.setTopic(topic);
			topicMessage.setTags(tags);
			topicMessage.setBody(message.getBytes());
			topicMessage.setKeys(UUID.randomUUID().toString());
			//SendResult result = producer.send(topicMessage);
			SendResult result = producer.send(topicMessage/*,5000L*/); //设置发送的超时时间
			
			SendStatus sendStatus = result.getSendStatus();
			/* 消息发送时间
			SendStatus.SEND_OK
			SendStatus.FLUSH_DISK_TIMEOUT
			SendStatus.FLUSH_SLAVE_TIMEOUT
			SendStatus.SLAVE_NOT_AVAILABLE
			*/
			logger.info("PointsmallEventProducer|message:{};tags:{},result:{};ID:{};properties:{};",message,tags,result.getSendStatus(),result.getMsgId(),topicMessage.getProperties());
			
		}

		producer.shutdown();
	}
	
	private static void sendAsync(String nameSrv,String topic ,String message,String tags) throws MQClientException, RemotingException, InterruptedException {
        //生产者实例化
		// public DefaultMQProducer(final String producerGroup, boolean enableMsgTrace, final String customizedTraceTopic)
		DefaultMQProducer producer = new DefaultMQProducer("async"/*,true*/);
        
        producer.getDefaultMQProducerImpl().registerSendMessageHook(new TraceSendMessageHook());
        //指定mq服务器地址
        producer.setNamesrvAddr(nameSrv);
        //启动实例
        producer.start();
        //发送异步失败时的最大重试次数，默认2次，这里不重试
        producer.setRetryTimesWhenSendAsyncFailed(0);

        int count = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < 10; i++) {
            final int index = i;
            //创建一个消息实例，指定topic、tag和消息体
			Message msg = new Message();
			msg.setTopic(topic);
			msg.setTags(tags);
			msg.setBody(message.getBytes());
			msg.setKeys(UUID.randomUUID().toString());

            //发送异步消息
            producer.send(msg, new SendCallback() {
                public void onSuccess(SendResult sendResult) {
                    countDownLatch.countDown();
                    System.out.println("success:" + index + "," + new String(msg.getBody()));
                }
                public void onException(Throwable throwable) {
                    countDownLatch.countDown();
                    System.out.println("Exception:" + index + "," + throwable);
                    throwable.printStackTrace();
                }
            });
        }
        countDownLatch.await(5, TimeUnit.SECONDS);
        //实例关闭
        producer.shutdown();
	}
	
	private static void sendOneway(String nameSrv,String topic ,String message,String tags) throws MQClientException, RemotingException, InterruptedException {
        //生产者实例化
		// public DefaultMQProducer(final String producerGroup, boolean enableMsgTrace, final String customizedTraceTopic)
		DefaultMQProducer producer = new DefaultMQProducer("oneway"/*,true*/);
        
        producer.getDefaultMQProducerImpl().registerSendMessageHook(new TraceSendMessageHook());
        //指定mq服务器地址
        producer.setNamesrvAddr(nameSrv);
        //启动实例
        producer.start();
        for (int i = 0; i < 10; i++) {
            //创建一个消息实例，指定topic、tag和消息体
			Message msg = new Message();
			msg.setTopic(topic);
			msg.setTags(tags);
			msg.setBody(message.getBytes());
			msg.setKeys(UUID.randomUUID().toString());
            //发送消息
            producer.sendOneway(msg);
            System.out.println(new String(msg.getBody()));
        }
        //实例关闭
        producer.shutdown();
	}
	
	public static class TraceSendMessageHook implements SendMessageHook{
		private final static Logger log = LoggerFactory.getLogger(TraceSendMessageHook.class);
		@Override
		public String hookName() {
			return TraceSendMessageHook.class.getName();
		}

		@Override
		public void sendMessageBefore(SendMessageContext context) {
		    //if it is message trace data,then it doesn't recorded
		    if (context == null) {   // @1
		        return;
		    }
		    
		    // add traceID
			Message  message = context.getMessage();
			message.getProperties().put("TraceID", "TraceID"+UUID.randomUUID().toString());
			message.getProperties().put("SpanID", "SpanID"+UUID.randomUUID().toString());

		    //build the context content of TuxeTraceContext
		    TraceContext tuxeContext = new TraceContext();
		    tuxeContext.setTraceBeans(new ArrayList<TraceBean>(1));
		    context.setMqTraceContext(tuxeContext);
		    tuxeContext.setTraceType(TraceType.Pub);
		    tuxeContext.setGroupName(context.getProducerGroup());                                                                                                                       // @2
		    //build the data bean object of message trace
		    TraceBean traceBean = new TraceBean();                                                                                                                                                // @3
		    traceBean.setTopic(context.getMessage().getTopic());
		    traceBean.setTags(context.getMessage().getTags());
		    traceBean.setKeys(context.getMessage().getKeys());
		    traceBean.setStoreHost(context.getBrokerAddr());
		    traceBean.setBodyLength(context.getMessage().getBody().length);
		    traceBean.setMsgType(context.getMsgType());
		    tuxeContext.getTraceBeans().add(traceBean);
		}

		@Override
		public void sendMessageAfter(SendMessageContext context) {
		    //if it is message trace data,then it doesn't recorded
		    if (context == null || context.getMqTraceContext() == null) {
		        return;
		    }
		    if (context.getSendResult() == null) {
		        return;
		    }
		    if (context.getSendResult().getRegionId() == null) {
		        return;
		    }

		    TraceContext tuxeContext = (TraceContext) context.getMqTraceContext();
		    TraceBean traceBean = tuxeContext.getTraceBeans().get(0);                                                                                                // @2
		    int costTime = (int) ((System.currentTimeMillis() - tuxeContext.getTimeStamp()) / tuxeContext.getTraceBeans().size());     // @3
		    tuxeContext.setCostTime(costTime);                                                                                                                                      // @4
		    if (context.getSendResult().getSendStatus().equals(SendStatus.SEND_OK)) {                                                                    
		        tuxeContext.setSuccess(true);
		    } else {
		        tuxeContext.setSuccess(false);
		    }
		    tuxeContext.setRegionId(context.getSendResult().getRegionId());                                                                                      
		    traceBean.setMsgId(context.getSendResult().getMsgId());
		    traceBean.setOffsetMsgId(context.getSendResult().getOffsetMsgId());
		    traceBean.setStoreTime(tuxeContext.getTimeStamp() + costTime / 2);
		    log.info(JSON.toJSONString(traceBean));                                                                                                                                // @5
		}
		
	}
}

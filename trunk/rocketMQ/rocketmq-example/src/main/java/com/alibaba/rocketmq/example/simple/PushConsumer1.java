/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.rocketmq.example.simple;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageConst;
import com.alibaba.rocketmq.common.message.MessageExt;


/**
 * PushConsumer，订阅消息
 */
public class PushConsumer1 {
	private static final Logger logger = ClientLogger.getLog(PushConsumer1.class);
    public static void main(String[] args) throws InterruptedException, MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("c_group_test_2");
        consumer.setNamesrvAddr("10.10.128.90:9876");
        consumer.setInstanceName("consumer_push1");
        /**
         * 订阅指定topic下所有消息
         */
        // consumer.subscribe("TopicTest", "*");

        /**
         * 订阅指定topic下tags分别等于TagA或TagC或TagD
         */
//        consumer.subscribe("TopicTest", "TagA || TagC || TagD");
        consumer.subscribe("TopicTest", "TagA");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        consumer.registerMessageListener(new MessageListenerConcurrently() {
            AtomicLong consumeTimes = new AtomicLong(0);

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                    ConsumeConcurrentlyContext context) {
                long offset = msgs.get(0).getQueueOffset();
                String maxOffset = msgs.get(0).getProperty(MessageConst.PROPERTY_MAX_OFFSET);
                long diff = Long.parseLong(maxOffset) - offset;
                if (diff > 100000) {
                    // TODO 消息堆积情况的特殊处理
                    // return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                for (MessageExt msg : msgs) {
                	logger.info(msg.getTags() + " " + new String(msg.getBody()));
                }
                
//                System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs);
//                this.consumeTimes.incrementAndGet();
//                if ((this.consumeTimes.get() % 2) == 0) {
//                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//                }
//                else if ((this.consumeTimes.get() % 3) == 0) {
//                    context.setDelayLevelWhenNextConsume(5);
//                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//                }

				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();

        System.out.println("Consumer Started.");
    }
}

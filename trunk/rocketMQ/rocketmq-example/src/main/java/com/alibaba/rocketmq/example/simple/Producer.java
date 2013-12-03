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

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

/**
 * Producer，发送消息
 */
public class Producer {
	public static void main(String[] args) throws MQClientException, InterruptedException {
		DefaultMQProducer producer = new DefaultMQProducer("p_group_test");
		producer.setNamesrvAddr("10.10.128.90:9876");
//		producer.setNamesrvAddr("10.10.128.91:9876");
		producer.setInstanceName("Producer");
		producer.start();

//        String[] tags = new String[] { "TagA", "TagB", "TagC", "TagD", "TagE" };
		String[] tags = new String[] {"TagA", "TagB", "TagC", "TagD", "TagE" };

		for (int i = 0; i < 10; i++) {
			try {
				Message msg = new Message("TopicTest",// topic
				        tags[i % tags.length],// tag
				        "KEY" + i,// key
				        ("Hello RocketMQ " + i).getBytes()// body
				);
				SendResult sendResult = producer.send(msg);
				System.out.println(sendResult);
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(1000);
			}
		}

		producer.shutdown();
		System.exit(0);
	}
}

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
package com.alibaba.rocketmq.example.ordermessage;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.log.ClientLogger;


/**
 * 顺序消息消费，带事务方式（应用可控制Offset什么时候提交）
 */
public class LogTest {
	private static final Logger logger = ClientLogger.getLog(LogTest.class);
    public static void main(String[] args) throws MQClientException {
    	for (int i = 0; i < 10; i++) {
    		logger.info("afdafd");
        }
    }

}

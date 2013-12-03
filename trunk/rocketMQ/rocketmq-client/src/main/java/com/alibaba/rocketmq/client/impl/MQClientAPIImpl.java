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
package com.alibaba.rocketmq.client.impl;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.alibaba.rocketmq.client.VirtualEnvUtil;
import com.alibaba.rocketmq.client.consumer.PullCallback;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.consumer.PullStatus;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.impl.consumer.PullResultExt;
import com.alibaba.rocketmq.client.log.ClientLogger;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.MQVersion;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.admin.ConsumeStats;
import com.alibaba.rocketmq.common.admin.OffsetWrapper;
import com.alibaba.rocketmq.common.admin.TopicOffset;
import com.alibaba.rocketmq.common.admin.TopicStatsTable;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageDecoder;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.namesrv.NamesrvUtil;
import com.alibaba.rocketmq.common.namesrv.TopAddressing;
import com.alibaba.rocketmq.common.protocol.MQProtos.MQRequestCode;
import com.alibaba.rocketmq.common.protocol.MQProtos.MQResponseCode;
import com.alibaba.rocketmq.common.protocol.body.ClusterInfo;
import com.alibaba.rocketmq.common.protocol.body.ConsumerConnection;
import com.alibaba.rocketmq.common.protocol.body.KVTable;
import com.alibaba.rocketmq.common.protocol.body.LockBatchRequestBody;
import com.alibaba.rocketmq.common.protocol.body.LockBatchResponseBody;
import com.alibaba.rocketmq.common.protocol.body.ProducerConnection;
import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.common.protocol.body.UnlockBatchRequestBody;
import com.alibaba.rocketmq.common.protocol.header.ConsumerSendMsgBackRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.CreateTopicRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.DeleteSubscriptionGroupRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.DeleteTopicRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.EndTransactionRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.GetConsumeStatsRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.GetConsumerConnectionListRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.GetConsumerListByGroupRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.GetConsumerListByGroupResponseBody;
import com.alibaba.rocketmq.common.protocol.header.GetEarliestMsgStoretimeRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.GetEarliestMsgStoretimeResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.GetMaxOffsetRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.GetMaxOffsetResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.GetMinOffsetRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.GetMinOffsetResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.GetProducerConnectionListRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.GetTopicStatsInfoRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.PullMessageRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.PullMessageResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.QueryConsumerOffsetRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.QueryConsumerOffsetResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.QueryMessageRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.SearchOffsetRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.SearchOffsetResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.SendMessageRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.SendMessageResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.UnregisterClientRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.UpdateConsumerOffsetRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.ViewMessageRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.namesrv.DeleteKVConfigRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.namesrv.GetKVConfigRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.namesrv.GetKVConfigResponseHeader;
import com.alibaba.rocketmq.common.protocol.header.namesrv.GetKVListByNamespaceRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.namesrv.GetRouteInfoRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.namesrv.PutKVConfigRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.namesrv.WipeWritePermOfBrokerRequestHeader;
import com.alibaba.rocketmq.common.protocol.header.namesrv.WipeWritePermOfBrokerResponseHeader;
import com.alibaba.rocketmq.common.protocol.heartbeat.ConsumerData;
import com.alibaba.rocketmq.common.protocol.heartbeat.HeartbeatData;
import com.alibaba.rocketmq.common.protocol.heartbeat.ProducerData;
import com.alibaba.rocketmq.common.protocol.heartbeat.SubscriptionData;
import com.alibaba.rocketmq.common.protocol.route.TopicRouteData;
import com.alibaba.rocketmq.common.subscription.SubscriptionGroupConfig;
import com.alibaba.rocketmq.remoting.InvokeCallback;
import com.alibaba.rocketmq.remoting.RemotingClient;
import com.alibaba.rocketmq.remoting.common.RemotingHelper;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;
import com.alibaba.rocketmq.remoting.exception.RemotingCommandException;
import com.alibaba.rocketmq.remoting.exception.RemotingConnectException;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.alibaba.rocketmq.remoting.exception.RemotingSendRequestException;
import com.alibaba.rocketmq.remoting.exception.RemotingTimeoutException;
import com.alibaba.rocketmq.remoting.exception.RemotingTooMuchRequestException;
import com.alibaba.rocketmq.remoting.netty.NettyClientConfig;
import com.alibaba.rocketmq.remoting.netty.NettyRemotingClient;
import com.alibaba.rocketmq.remoting.netty.ResponseFuture;
import com.alibaba.rocketmq.remoting.protocol.RemotingCommand;
import com.alibaba.rocketmq.remoting.protocol.RemotingProtos.ResponseCode;
import com.alibaba.rocketmq.remoting.protocol.RemotingSerializable;


/**
 * 封装所有与服务器通信部分API
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-24
 */
public class MQClientAPIImpl {
    private final static Logger log = ClientLogger.getLog(MQClientAPIImpl.class);
    private final RemotingClient remotingClient;
    private final TopAddressing topAddressing = new TopAddressing();
    private final ClientRemotingProcessor clientRemotingProcessor;
    private String nameSrvAddr = null;
    // 虚拟运行环境相关的project group
    private String projectGroupPrefix;

    static {
        System.setProperty(RemotingCommand.RemotingVersionKey, Integer.toString(MQVersion.CurrentVersion));
    }


    public String getProjectGroupPrefix() {
        return projectGroupPrefix;
    }


    public MQClientAPIImpl(final NettyClientConfig nettyClientConfig,
            final ClientRemotingProcessor clientRemotingProcessor) {
        this.remotingClient = new NettyRemotingClient(nettyClientConfig);
        this.clientRemotingProcessor = clientRemotingProcessor;

        /**
         * 注册客户端支持的RPC CODE
         */
        this.remotingClient.registerProcessor(MQRequestCode.CHECK_TRANSACTION_STATE_VALUE,
            this.clientRemotingProcessor, null);

        this.remotingClient.registerProcessor(MQRequestCode.NOTIFY_CONSUMER_IDS_CHANGED_VALUE,
            this.clientRemotingProcessor, null);
    }


    public List<String> getNameServerAddressList() {
        return this.remotingClient.getNameServerAddressList();
    }


    public RemotingClient getRemotingClient() {
        return remotingClient;
    }


    public String fetchNameServerAddr() {
        try {
            String addrs = this.topAddressing.fetchNSAddr();
            if (addrs != null) {
                if (!addrs.equals(this.nameSrvAddr)) {
                    log.info("name server address changed, old: " + this.nameSrvAddr + " new: " + addrs);
                    this.updateNameServerAddressList(addrs);
                    this.nameSrvAddr = addrs;
                    return nameSrvAddr;
                }
            }
        }
        catch (Exception e) {
            log.error("fetchNameServerAddr Exception", e);
        }
        return nameSrvAddr;
    }


    public void updateNameServerAddressList(final String addrs) {
        List<String> lst = new ArrayList<String>();
        String[] addrArray = addrs.split(";");
        if (addrArray != null) {
            for (String addr : addrArray) {
                lst.add(addr);
            }

            this.remotingClient.updateNameServerAddressList(lst);
        }
    }


    public void start() {
        // 远程通信 Client 启动
        this.remotingClient.start();

        // 获取虚拟运行环境相关的project group
        try {
            String localAddress = RemotingUtil.getLocalAddress();
            projectGroupPrefix = this.getProjectGroupByIp(localAddress, 3000);
            log.info("The client[{}] in project group: {}", localAddress, projectGroupPrefix);
        }
        catch (Exception e) {
        }
    }


    public void shutdown() {
        this.remotingClient.shutdown();
    }


    public void createSubscriptionGroup(final String addr, final SubscriptionGroupConfig config,
            final long timeoutMillis) throws RemotingException, MQBrokerException, InterruptedException,
            MQClientException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            config.setGroupName(VirtualEnvUtil.buildWithProjectGroup(config.getGroupName(),
                projectGroupPrefix));
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.UPDATE_AND_CREATE_SUBSCRIPTIONGROUP_VALUE,
                    null);

        byte[] body = RemotingSerializable.encode(config);
        request.setBody(body);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());

    }


    public void createTopic(final String addr, final String defaultTopic, final TopicConfig topicConfig,
            final long timeoutMillis) throws RemotingException, MQBrokerException, InterruptedException,
            MQClientException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topicConfig.getTopicName();
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup =
                    VirtualEnvUtil.buildWithProjectGroup(topicConfig.getTopicName(), projectGroupPrefix);
        }

        CreateTopicRequestHeader requestHeader = new CreateTopicRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);
        requestHeader.setDefaultTopic(defaultTopic);
        requestHeader.setReadQueueNums(topicConfig.getReadQueueNums());
        requestHeader.setWriteQueueNums(topicConfig.getWriteQueueNums());
        requestHeader.setPerm(topicConfig.getPerm());
        requestHeader.setTopicFilterType(topicConfig.getTopicFilterType().name());

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.UPDATE_AND_CREATE_TOPIC_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * 发送消息
     */
    public SendResult sendMessage(//
            final String addr,// 1
            final String brokerName,// 2
            final Message msg,// 3
            final SendMessageRequestHeader requestHeader,// 4
            final long timeoutMillis,// 5
            final CommunicationMode communicationMode,// 6
            final SendCallback sendCallback// 7
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            msg.setTopic(VirtualEnvUtil.buildWithProjectGroup(msg.getTopic(), projectGroupPrefix));
            requestHeader.setProducerGroup(VirtualEnvUtil.buildWithProjectGroup(
                requestHeader.getProducerGroup(), projectGroupPrefix));
            requestHeader.setTopic(VirtualEnvUtil.buildWithProjectGroup(requestHeader.getTopic(),
                projectGroupPrefix));
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.SEND_MESSAGE_VALUE, requestHeader);
        request.setBody(msg.getBody());

        switch (communicationMode) {
        case ONEWAY:
            this.remotingClient.invokeOneway(addr, request, timeoutMillis);
            return null;
        case ASYNC:
            this.sendMessageAsync(addr, brokerName, msg, timeoutMillis, request, sendCallback);
            return null;
        case SYNC:
            return this.sendMessageSync(addr, brokerName, msg, timeoutMillis, request);
        default:
            assert false;
            break;
        }

        return null;
    }


    private SendResult sendMessageSync(//
            final String addr,//
            final String brokerName,//
            final Message msg,//
            final long timeoutMillis,//
            final RemotingCommand request//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        return this.processSendResponse(brokerName, msg, response);
    }


    private void sendMessageAsync(//
            final String addr,//
            final String brokerName,//
            final Message msg,//
            final long timeoutMillis,//
            final RemotingCommand request,//
            final SendCallback sendCallback//
    ) throws RemotingException, InterruptedException {
        this.remotingClient.invokeAsync(addr, request, timeoutMillis, new InvokeCallback() {
            @Override
            public void operationComplete(ResponseFuture responseFuture) {
                if (null == sendCallback)
                    return;

                RemotingCommand response = responseFuture.getResponseCommand();
                if (response != null) {
                    try {
                        SendResult sendResult =
                                MQClientAPIImpl.this.processSendResponse(brokerName, msg, response);
                        assert sendResult != null;
                        sendCallback.onSuccess(sendResult);
                    }
                    catch (Exception e) {
                        sendCallback.onException(e);
                    }
                }
                else {
                    if (!responseFuture.isSendRequestOK()) {
                        sendCallback.onException(new MQClientException("send request failed", responseFuture
                            .getCause()));
                    }
                    else if (responseFuture.isTimeout()) {
                        sendCallback.onException(new MQClientException("wait response timeout "
                                + responseFuture.getTimeoutMillis() + "ms", responseFuture.getCause()));
                    }
                    else {
                        sendCallback.onException(new MQClientException("unknow reseaon", responseFuture
                            .getCause()));
                    }
                }
            }
        });
    }


    private SendResult processSendResponse(//
            final String brokerName,//
            final Message msg,//
            final RemotingCommand response//
    ) throws MQBrokerException, RemotingCommandException {
        switch (response.getCode()) {
        case MQResponseCode.FLUSH_DISK_TIMEOUT_VALUE:
        case MQResponseCode.FLUSH_SLAVE_TIMEOUT_VALUE:
        case MQResponseCode.SLAVE_NOT_AVAILABLE_VALUE: {
            // TODO LOG
        }
        case ResponseCode.SUCCESS_VALUE: {
            SendStatus sendStatus = SendStatus.SEND_OK;
            switch (response.getCode()) {
            case MQResponseCode.FLUSH_DISK_TIMEOUT_VALUE:
                sendStatus = SendStatus.FLUSH_DISK_TIMEOUT;
                break;
            case MQResponseCode.FLUSH_SLAVE_TIMEOUT_VALUE:
                sendStatus = SendStatus.FLUSH_SLAVE_TIMEOUT;
                break;
            case MQResponseCode.SLAVE_NOT_AVAILABLE_VALUE:
                sendStatus = SendStatus.SLAVE_NOT_AVAILABLE;
                break;
            case ResponseCode.SUCCESS_VALUE:
                sendStatus = SendStatus.SEND_OK;
                break;
            default:
                assert false;
                break;
            }

            SendMessageResponseHeader responseHeader =
                    (SendMessageResponseHeader) response
                        .decodeCommandCustomHeader(SendMessageResponseHeader.class);

            MessageQueue messageQueue =
                    new MessageQueue(msg.getTopic(), brokerName, responseHeader.getQueueId());

            return new SendResult(sendStatus, responseHeader.getMsgId(), messageQueue,
                responseHeader.getQueueOffset(), projectGroupPrefix);
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 拉消息接口
     */
    public PullResult pullMessage(//
            final String addr,//
            final PullMessageRequestHeader requestHeader,//
            final long timeoutMillis,//
            final CommunicationMode communicationMode,//
            final PullCallback pullCallback//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            requestHeader.setConsumerGroup(VirtualEnvUtil.buildWithProjectGroup(
                requestHeader.getConsumerGroup(), projectGroupPrefix));
            requestHeader.setTopic(VirtualEnvUtil.buildWithProjectGroup(requestHeader.getTopic(),
                projectGroupPrefix));
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.PULL_MESSAGE_VALUE, requestHeader);

        switch (communicationMode) {
        case ONEWAY:
            assert false;
            return null;
        case ASYNC:
            this.pullMessageAsync(addr, request, timeoutMillis, pullCallback);
            return null;
        case SYNC:
            return this.pullMessageSync(addr, request, timeoutMillis);
        default:
            assert false;
            break;
        }

        return null;
    }


    private void pullMessageAsync(//
            final String addr,// 1
            final RemotingCommand request,//
            final long timeoutMillis,//
            final PullCallback pullCallback//
    ) throws RemotingException, InterruptedException {
        this.remotingClient.invokeAsync(addr, request, timeoutMillis, new InvokeCallback() {
            @Override
            public void operationComplete(ResponseFuture responseFuture) {
                RemotingCommand response = responseFuture.getResponseCommand();
                if (response != null) {
                    try {
                        PullResult pullResult = MQClientAPIImpl.this.processPullResponse(response);
                        assert pullResult != null;
                        pullCallback.onSuccess(pullResult);
                    }
                    catch (Exception e) {
                        pullCallback.onException(e);
                    }
                }
                else {
                    if (!responseFuture.isSendRequestOK()) {
                        pullCallback.onException(new MQClientException("send request failed", responseFuture
                            .getCause()));
                    }
                    else if (responseFuture.isTimeout()) {
                        pullCallback.onException(new MQClientException("wait response timeout "
                                + responseFuture.getTimeoutMillis() + "ms", responseFuture.getCause()));
                    }
                    else {
                        pullCallback.onException(new MQClientException("unknow reseaon", responseFuture
                            .getCause()));
                    }
                }
            }
        });
    }


    private PullResult processPullResponse(final RemotingCommand response) throws MQBrokerException,
            RemotingCommandException {
        PullStatus pullStatus = PullStatus.NO_NEW_MSG;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE:
            pullStatus = PullStatus.FOUND;
            break;
        case MQResponseCode.PULL_NOT_FOUND_VALUE:
            pullStatus = PullStatus.NO_NEW_MSG;
            break;
        case MQResponseCode.PULL_RETRY_IMMEDIATELY_VALUE:
            pullStatus = PullStatus.NO_MATCHED_MSG;
            break;
        case MQResponseCode.PULL_OFFSET_MOVED_VALUE:
            pullStatus = PullStatus.OFFSET_ILLEGAL;
            break;

        default:
            throw new MQBrokerException(response.getCode(), response.getRemark());
        }

        PullMessageResponseHeader responseHeader =
                (PullMessageResponseHeader) response
                    .decodeCommandCustomHeader(PullMessageResponseHeader.class);

        return new PullResultExt(pullStatus, responseHeader.getNextBeginOffset(),
            responseHeader.getMinOffset(), responseHeader.getMaxOffset(), null,
            responseHeader.getSuggestWhichBrokerId(), response.getBody());
    }


    private PullResult pullMessageSync(//
            final String addr,// 1
            final RemotingCommand request,// 2
            final long timeoutMillis// 3
    ) throws RemotingException, InterruptedException, MQBrokerException {
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        return this.processPullResponse(response);
    }


    /**
     * 根据时间查询Offset
     */
    public MessageExt viewMessage(final String addr, final long phyoffset, final long timeoutMillis)
            throws RemotingException, MQBrokerException, InterruptedException {
        ViewMessageRequestHeader requestHeader = new ViewMessageRequestHeader();
        requestHeader.setOffset(phyoffset);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.VIEW_MESSAGE_BY_ID_VALUE, requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            ByteBuffer byteBuffer = ByteBuffer.wrap(response.getBody());
            MessageExt messageExt = MessageDecoder.decode(byteBuffer);
            // 清除虚拟运行环境相关的projectGroupPrefix
            if (!UtilAll.isBlank(projectGroupPrefix)) {
                messageExt.setTopic(VirtualEnvUtil.clearProjectGroup(messageExt.getTopic(),
                    projectGroupPrefix));
            }
            return messageExt;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 根据时间查询Offset
     */
    public long searchOffset(final String addr, final String topic, final int queueId, final long timestamp,
            final long timeoutMillis) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topic;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(topic, projectGroupPrefix);
        }

        SearchOffsetRequestHeader requestHeader = new SearchOffsetRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);
        requestHeader.setQueueId(queueId);
        requestHeader.setTimestamp(timestamp);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.SEARCH_OFFSET_BY_TIMESTAMP_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            SearchOffsetResponseHeader responseHeader =
                    (SearchOffsetResponseHeader) response
                        .decodeCommandCustomHeader(SearchOffsetResponseHeader.class);
            return responseHeader.getOffset();
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 获取队列的最大Offset
     */
    public long getMaxOffset(final String addr, final String topic, final int queueId,
            final long timeoutMillis) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topic;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(topic, projectGroupPrefix);
        }

        GetMaxOffsetRequestHeader requestHeader = new GetMaxOffsetRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);
        requestHeader.setQueueId(queueId);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_MAX_OFFSET_VALUE, requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            GetMaxOffsetResponseHeader responseHeader =
                    (GetMaxOffsetResponseHeader) response
                        .decodeCommandCustomHeader(GetMaxOffsetResponseHeader.class);

            return responseHeader.getOffset();
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 获取某个组的Consumer Id列表
     */
    public List<String> getConsumerIdListByGroup(//
            final String addr, //
            final String consumerGroup, //
            final long timeoutMillis) throws RemotingConnectException, RemotingSendRequestException,
            RemotingTimeoutException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String consumerGroupWithProjectGroup = consumerGroup;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            consumerGroupWithProjectGroup =
                    VirtualEnvUtil.buildWithProjectGroup(consumerGroup, projectGroupPrefix);
        }

        GetConsumerListByGroupRequestHeader requestHeader = new GetConsumerListByGroupRequestHeader();
        requestHeader.setConsumerGroup(consumerGroupWithProjectGroup);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_CONSUMER_LIST_BY_GROUP_VALUE,
                    requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            if (response.getBody() != null) {
                GetConsumerListByGroupResponseBody body =
                        GetConsumerListByGroupResponseBody.decode(response.getBody(),
                            GetConsumerListByGroupResponseBody.class);
                return body.getConsumerIdList();
            }
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 获取队列的最小Offset
     */
    public long getMinOffset(final String addr, final String topic, final int queueId,
            final long timeoutMillis) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topic;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(topic, projectGroupPrefix);
        }

        GetMinOffsetRequestHeader requestHeader = new GetMinOffsetRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);
        requestHeader.setQueueId(queueId);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_MIN_OFFSET_VALUE, requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            GetMinOffsetResponseHeader responseHeader =
                    (GetMinOffsetResponseHeader) response
                        .decodeCommandCustomHeader(GetMinOffsetResponseHeader.class);

            return responseHeader.getOffset();
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 获取队列的最早时间
     */
    public long getEarliestMsgStoretime(final String addr, final String topic, final int queueId,
            final long timeoutMillis) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topic;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(topic, projectGroupPrefix);
        }

        GetEarliestMsgStoretimeRequestHeader requestHeader = new GetEarliestMsgStoretimeRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);
        requestHeader.setQueueId(queueId);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_EARLIEST_MSG_STORETIME_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            GetEarliestMsgStoretimeResponseHeader responseHeader =
                    (GetEarliestMsgStoretimeResponseHeader) response
                        .decodeCommandCustomHeader(GetEarliestMsgStoretimeResponseHeader.class);

            return responseHeader.getTimestamp();
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 查询Consumer消费进度
     */
    public long queryConsumerOffset(//
            final String addr,//
            final QueryConsumerOffsetRequestHeader requestHeader,//
            final long timeoutMillis//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            requestHeader.setConsumerGroup(VirtualEnvUtil.buildWithProjectGroup(
                requestHeader.getConsumerGroup(), projectGroupPrefix));
            requestHeader.setTopic(VirtualEnvUtil.buildWithProjectGroup(requestHeader.getTopic(),
                projectGroupPrefix));
        }

        RemotingCommand request =
                RemotingCommand
                    .createRequestCommand(MQRequestCode.QUERY_CONSUMER_OFFSET_VALUE, requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            QueryConsumerOffsetResponseHeader responseHeader =
                    (QueryConsumerOffsetResponseHeader) response
                        .decodeCommandCustomHeader(QueryConsumerOffsetResponseHeader.class);

            return responseHeader.getOffset();
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 更新Consumer消费进度
     */
    public void updateConsumerOffset(//
            final String addr,//
            final UpdateConsumerOffsetRequestHeader requestHeader,//
            final long timeoutMillis//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            requestHeader.setConsumerGroup(VirtualEnvUtil.buildWithProjectGroup(
                requestHeader.getConsumerGroup(), projectGroupPrefix));
            requestHeader.setTopic(VirtualEnvUtil.buildWithProjectGroup(requestHeader.getTopic(),
                projectGroupPrefix));
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.UPDATE_CONSUMER_OFFSET_VALUE,
                    requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 更新Consumer消费进度
     * 
     * @throws InterruptedException
     * @throws RemotingSendRequestException
     * @throws RemotingTimeoutException
     * @throws RemotingTooMuchRequestException
     * 
     * @throws RemotingConnectException
     */
    public void updateConsumerOffsetOneway(//
            final String addr,//
            final UpdateConsumerOffsetRequestHeader requestHeader,//
            final long timeoutMillis//
    ) throws RemotingConnectException, RemotingTooMuchRequestException, RemotingTimeoutException,
            RemotingSendRequestException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            requestHeader.setConsumerGroup(VirtualEnvUtil.buildWithProjectGroup(
                requestHeader.getConsumerGroup(), projectGroupPrefix));
            requestHeader.setTopic(VirtualEnvUtil.buildWithProjectGroup(requestHeader.getTopic(),
                projectGroupPrefix));
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.UPDATE_CONSUMER_OFFSET_VALUE,
                    requestHeader);
        this.remotingClient.invokeOneway(addr, request, timeoutMillis);
    }


    /**
     * 发送心跳
     */
    public void sendHearbeat(//
            final String addr,//
            final HeartbeatData heartbeatData,//
            final long timeoutMillis//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            Set<ConsumerData> consumerDatas = heartbeatData.getConsumerDataSet();
            for (ConsumerData consumerData : consumerDatas) {
                consumerData.setGroupName(VirtualEnvUtil.buildWithProjectGroup(consumerData.getGroupName(),
                    projectGroupPrefix));
                Set<SubscriptionData> subscriptionDatas = consumerData.getSubscriptionDataSet();
                for (SubscriptionData subscriptionData : subscriptionDatas) {
                    subscriptionData.setTopic(VirtualEnvUtil.buildWithProjectGroup(
                        subscriptionData.getTopic(), projectGroupPrefix));
                }
            }
            Set<ProducerData> producerDatas = heartbeatData.getProducerDataSet();
            for (ProducerData producerData : producerDatas) {
                producerData.setGroupName(VirtualEnvUtil.buildWithProjectGroup(producerData.getGroupName(),
                    projectGroupPrefix));
            }
        }

        RemotingCommand request = RemotingCommand.createRequestCommand(MQRequestCode.HEART_BEAT_VALUE, null);
        request.setBody(heartbeatData.encode());
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 发送心跳
     */
    public void unregisterClient(//
            final String addr,//
            final String clientID,//
            final String producerGroup,//
            final String consumerGroup,//
            final long timeoutMillis//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String producerGroupWithProjectGroup = producerGroup;
        String consumerGroupWithProjectGroup = consumerGroup;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            producerGroupWithProjectGroup =
                    VirtualEnvUtil.buildWithProjectGroup(producerGroup, projectGroupPrefix);
            consumerGroupWithProjectGroup =
                    VirtualEnvUtil.buildWithProjectGroup(consumerGroup, projectGroupPrefix);
        }

        final UnregisterClientRequestHeader requestHeader = new UnregisterClientRequestHeader();
        requestHeader.setClientID(clientID);
        requestHeader.setProducerGroup(producerGroupWithProjectGroup);
        requestHeader.setConsumerGroup(consumerGroupWithProjectGroup);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.UNREGISTER_CLIENT_VALUE, requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 提交或者回滚事务
     */
    public void endTransactionOneway(//
            final String addr,//
            final EndTransactionRequestHeader requestHeader,//
            final String remark,//
            final long timeoutMillis//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            requestHeader.setProducerGroup(VirtualEnvUtil.buildWithProjectGroup(
                requestHeader.getProducerGroup(), projectGroupPrefix));
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.END_TRANSACTION_VALUE, requestHeader);
        request.setRemark(remark);
        this.remotingClient.invokeOneway(addr, request, timeoutMillis);
    }


    /**
     * 查询消息
     */
    public void queryMessage(//
            final String addr,//
            final QueryMessageRequestHeader requestHeader,//
            final long timeoutMillis,//
            final InvokeCallback invokeCallback//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            requestHeader.setTopic(VirtualEnvUtil.buildWithProjectGroup(requestHeader.getTopic(),
                projectGroupPrefix));
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.QUERY_MESSAGE_VALUE, requestHeader);
        this.remotingClient.invokeAsync(addr, request, timeoutMillis, invokeCallback);
    }


    public boolean registerClient(final String addr, final HeartbeatData heartbeat, final long timeoutMillis)
            throws RemotingException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            Set<ConsumerData> consumerDatas = heartbeat.getConsumerDataSet();
            for (ConsumerData consumerData : consumerDatas) {
                consumerData.setGroupName(VirtualEnvUtil.buildWithProjectGroup(consumerData.getGroupName(),
                    projectGroupPrefix));
                Set<SubscriptionData> subscriptionDatas = consumerData.getSubscriptionDataSet();
                for (SubscriptionData subscriptionData : subscriptionDatas) {
                    subscriptionData.setTopic(VirtualEnvUtil.buildWithProjectGroup(
                        subscriptionData.getTopic(), projectGroupPrefix));
                }
            }
            Set<ProducerData> producerDatas = heartbeat.getProducerDataSet();
            for (ProducerData producerData : producerDatas) {
                producerData.setGroupName(VirtualEnvUtil.buildWithProjectGroup(producerData.getGroupName(),
                    projectGroupPrefix));
            }
        }

        RemotingCommand request = RemotingCommand.createRequestCommand(MQRequestCode.HEART_BEAT_VALUE, null);
        request.setBody(heartbeat.encode());
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        return response.getCode() == ResponseCode.SUCCESS_VALUE;
    }


    /**
     * 失败的消息发回Broker
     */
    public void consumerSendMessageBack(//
            final MessageExt msg,//
            final String consumerGroup,//
            final int delayLevel,//
            final long timeoutMillis//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String consumerGroupWithProjectGroup = consumerGroup;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            consumerGroupWithProjectGroup =
                    VirtualEnvUtil.buildWithProjectGroup(consumerGroup, projectGroupPrefix);
            msg.setTopic(VirtualEnvUtil.buildWithProjectGroup(msg.getTopic(), projectGroupPrefix));
        }

        ConsumerSendMsgBackRequestHeader requestHeader = new ConsumerSendMsgBackRequestHeader();
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.CONSUMER_SEND_MSG_BACK_VALUE,
                    requestHeader);
        requestHeader.setGroup(consumerGroupWithProjectGroup);
        requestHeader.setOffset(msg.getCommitLogOffset());
        requestHeader.setDelayLevel(delayLevel);

        String addr = RemotingHelper.parseSocketAddressAddr(msg.getStoreHost());

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    public Set<MessageQueue> lockBatchMQ(//
            final String addr,//
            final LockBatchRequestBody requestBody,//
            final long timeoutMillis) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            requestBody.setConsumerGroup((VirtualEnvUtil.buildWithProjectGroup(
                requestBody.getConsumerGroup(), projectGroupPrefix)));
            Set<MessageQueue> messageQueues = requestBody.getMqSet();
            for (MessageQueue messageQueue : messageQueues) {
                messageQueue.setTopic(VirtualEnvUtil.buildWithProjectGroup(messageQueue.getTopic(),
                    projectGroupPrefix));
            }
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.LOCK_BATCH_MQ_VALUE, null);
        request.setBody(requestBody.encode());
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            LockBatchResponseBody responseBody =
                    LockBatchResponseBody.decode(response.getBody(), LockBatchResponseBody.class);
            Set<MessageQueue> messageQueues = responseBody.getLockOKMQSet();
            // 清除虚拟运行环境相关的projectGroupPrefix
            if (!UtilAll.isBlank(projectGroupPrefix)) {
                for (MessageQueue messageQueue : messageQueues) {
                    messageQueue.setTopic(VirtualEnvUtil.clearProjectGroup(messageQueue.getTopic(),
                        projectGroupPrefix));
                }
            }
            return messageQueues;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    public void unlockBatchMQ(//
            final String addr,//
            final UnlockBatchRequestBody requestBody,//
            final long timeoutMillis,//
            final boolean oneway//
    ) throws RemotingException, MQBrokerException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            requestBody.setConsumerGroup(VirtualEnvUtil.buildWithProjectGroup(requestBody.getConsumerGroup(),
                projectGroupPrefix));
            Set<MessageQueue> messageQueues = requestBody.getMqSet();
            for (MessageQueue messageQueue : messageQueues) {
                messageQueue.setTopic(VirtualEnvUtil.buildWithProjectGroup(messageQueue.getTopic(),
                    projectGroupPrefix));
            }
        }

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.UNLOCK_BATCH_MQ_VALUE, null);
        request.setBody(requestBody.encode());

        if (oneway) {
            this.remotingClient.invokeOneway(addr, request, timeoutMillis);
        }
        else {
            RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
            switch (response.getCode()) {
            case ResponseCode.SUCCESS_VALUE: {
                return;
            }
            default:
                break;
            }

            throw new MQBrokerException(response.getCode(), response.getRemark());
        }
    }


    public TopicStatsTable getTopicStatsInfo(final String addr, final String topic, final long timeoutMillis)
            throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException,
            RemotingConnectException, MQBrokerException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topic;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(topic, projectGroupPrefix);
        }

        GetTopicStatsInfoRequestHeader requestHeader = new GetTopicStatsInfoRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_TOPIC_STATS_INFO_VALUE, requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            TopicStatsTable topicStatsTable =
                    TopicStatsTable.decode(response.getBody(), TopicStatsTable.class);
            // 清除虚拟运行环境相关的projectGroupPrefix
            if (!UtilAll.isBlank(projectGroupPrefix)) {
                HashMap<MessageQueue, TopicOffset> newTopicOffsetMap =
                        new HashMap<MessageQueue, TopicOffset>();
                for (Map.Entry<MessageQueue, TopicOffset> messageQueue : topicStatsTable.getOffsetTable()
                    .entrySet()) {
                    MessageQueue key = messageQueue.getKey();
                    key.setTopic(VirtualEnvUtil.clearProjectGroup(key.getTopic(), projectGroupPrefix));
                    newTopicOffsetMap.put(key, messageQueue.getValue());
                }
                topicStatsTable.setOffsetTable(newTopicOffsetMap);
            }
            return topicStatsTable;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    public ConsumeStats getConsumeStats(final String addr, final String consumerGroup,
            final long timeoutMillis) throws InterruptedException, RemotingTimeoutException,
            RemotingSendRequestException, RemotingConnectException, MQBrokerException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String consumerGroupWithProjectGroup = consumerGroup;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            consumerGroupWithProjectGroup =
                    VirtualEnvUtil.buildWithProjectGroup(consumerGroup, projectGroupPrefix);
        }

        GetConsumeStatsRequestHeader requestHeader = new GetConsumeStatsRequestHeader();
        requestHeader.setConsumerGroup(consumerGroupWithProjectGroup);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_CONSUME_STATS_VALUE, requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            ConsumeStats consumeStats = ConsumeStats.decode(response.getBody(), ConsumeStats.class);
            // 清除虚拟运行环境相关的projectGroupPrefix
            if (!UtilAll.isBlank(projectGroupPrefix)) {
                HashMap<MessageQueue, OffsetWrapper> newTopicOffsetMap =
                        new HashMap<MessageQueue, OffsetWrapper>();
                for (Map.Entry<MessageQueue, OffsetWrapper> messageQueue : consumeStats.getOffsetTable()
                    .entrySet()) {
                    MessageQueue key = messageQueue.getKey();
                    key.setTopic(VirtualEnvUtil.clearProjectGroup(key.getTopic(), projectGroupPrefix));
                    newTopicOffsetMap.put(key, messageQueue.getValue());
                }
                consumeStats.setOffsetTable(newTopicOffsetMap);
            }

            return consumeStats;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 根据ProducerGroup获取Producer连接列表
     */
    public ProducerConnection getProducerConnectionList(final String addr, final String producerGroup,
            final long timeoutMillis) throws RemotingConnectException, RemotingSendRequestException,
            RemotingTimeoutException, InterruptedException, MQBrokerException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String producerGroupWithProjectGroup = producerGroup;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            producerGroupWithProjectGroup =
                    VirtualEnvUtil.buildWithProjectGroup(producerGroup, projectGroupPrefix);
        }

        GetProducerConnectionListRequestHeader requestHeader = new GetProducerConnectionListRequestHeader();
        requestHeader.setProducerGroup(producerGroupWithProjectGroup);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_PRODUCER_CONNECTION_LIST_VALUE,
                    requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return ProducerConnection.decode(response.getBody(), ProducerConnection.class);
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 根据ConsumerGroup获取Consumer连接列表以及订阅关系
     */
    public ConsumerConnection getConsumerConnectionList(final String addr, final String consumerGroup,
            final long timeoutMillis) throws RemotingConnectException, RemotingSendRequestException,
            RemotingTimeoutException, InterruptedException, MQBrokerException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String consumerGroupWithProjectGroup = consumerGroup;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            consumerGroupWithProjectGroup =
                    VirtualEnvUtil.buildWithProjectGroup(consumerGroup, projectGroupPrefix);
        }

        GetConsumerConnectionListRequestHeader requestHeader = new GetConsumerConnectionListRequestHeader();
        requestHeader.setConsumerGroup(consumerGroupWithProjectGroup);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_CONSUMER_CONNECTION_LIST_VALUE,
                    requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            ConsumerConnection consumerConnection =
                    ConsumerConnection.decode(response.getBody(), ConsumerConnection.class);
            if (!UtilAll.isBlank(projectGroupPrefix)) {
                ConcurrentHashMap<String, SubscriptionData> subscriptionDataConcurrentHashMap =
                        consumerConnection.getSubscriptionTable();
                for (Map.Entry<String, SubscriptionData> subscriptionDataEntry : subscriptionDataConcurrentHashMap
                    .entrySet()) {
                    SubscriptionData subscriptionData = subscriptionDataEntry.getValue();
                    subscriptionDataEntry.getValue().setTopic(
                        VirtualEnvUtil.clearProjectGroup(subscriptionData.getTopic(), projectGroupPrefix));
                }
            }
            return consumerConnection;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    public KVTable getBrokerRuntimeInfo(final String addr, final long timeoutMillis)
            throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
            InterruptedException, MQBrokerException {

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_BROKER_RUNTIME_INFO_VALUE, null);
        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return KVTable.decode(response.getBody(), KVTable.class);
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());
    }


    /**
     * 更新Broker的配置文件
     * 
     * @param addr
     * @param properties
     * @param timeoutMillis
     * @throws RemotingConnectException
     * @throws RemotingSendRequestException
     * @throws RemotingTimeoutException
     * @throws InterruptedException
     * @throws MQBrokerException
     * @throws UnsupportedEncodingException
     */
    public void updateBrokerConfig(final String addr, final Properties properties, final long timeoutMillis)
            throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException,
            InterruptedException, MQBrokerException, UnsupportedEncodingException {

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.UPDATE_BROKER_CONFIG_VALUE, null);

        String str = MixAll.properties2String(properties);
        if (str != null && str.length() > 0) {
            request.setBody(str.getBytes(MixAll.DEFAULT_CHARSET));
            RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
            switch (response.getCode()) {
            case ResponseCode.SUCCESS_VALUE: {
                return;
            }
            default:
                break;
            }

            throw new MQBrokerException(response.getCode(), response.getRemark());
        }
    }


    /**
     * Name Server: 从Name Server获取集群信息
     */
    public ClusterInfo getBrokerClusterInfo(final long timeoutMillis) throws InterruptedException,
            RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException,
            MQBrokerException {
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_BROKER_CLUSTER_INFO_VALUE, null);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            ClusterInfo responseBody = ClusterInfo.decode(response.getBody(), ClusterInfo.class);
            return responseBody;
        }
        default:
            break;
        }

        throw new MQBrokerException(response.getCode(), response.getRemark());

    }


    /**
     * Name Server: 从Name Server获取 Default Topic 路由信息
     */
    public TopicRouteData getDefaultTopicRouteInfoFromNameServer(final String topic, final long timeoutMillis)
            throws RemotingException, MQClientException, InterruptedException {
        GetRouteInfoRequestHeader requestHeader = new GetRouteInfoRequestHeader();
        requestHeader.setTopic(topic);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_ROUTEINTO_BY_TOPIC_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case MQResponseCode.TOPIC_NOT_EXIST_VALUE: {
            // TODO LOG
            break;
        }
        case ResponseCode.SUCCESS_VALUE: {
            byte[] body = response.getBody();
            if (body != null) {
                return TopicRouteData.decode(body, TopicRouteData.class);
            }
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: 从Name Server获取Topic路由信息
     */
    public TopicRouteData getTopicRouteInfoFromNameServer(final String topic, final long timeoutMillis)
            throws RemotingException, MQClientException, InterruptedException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topic;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(topic, projectGroupPrefix);
        }

        GetRouteInfoRequestHeader requestHeader = new GetRouteInfoRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_ROUTEINTO_BY_TOPIC_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case MQResponseCode.TOPIC_NOT_EXIST_VALUE: {
            log.warn("get Topic [{}]RouteInfoFromNameServer is not exist value", topic);
            break;
        }
        case ResponseCode.SUCCESS_VALUE: {
            byte[] body = response.getBody();
            if (body != null) {
                return TopicRouteData.decode(body, TopicRouteData.class);
            }
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: 从Name Server获取所有Topic列表
     */
    public TopicList getTopicListFromNameServer(final long timeoutMillis) throws RemotingException,
            MQClientException, InterruptedException {
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_ALL_TOPIC_LIST_FROM_NAMESERVER_VALUE,
                    null);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            byte[] body = response.getBody();
            if (body != null) {
                TopicList topicList = TopicList.decode(body, TopicList.class);

                if (!UtilAll.isBlank(projectGroupPrefix)) {
                    HashSet<String> newTopicSet = new HashSet<String>();
                    for (String topic : topicList.getTopicList()) {
                        newTopicSet.add(VirtualEnvUtil.clearProjectGroup(topic, projectGroupPrefix));
                    }
                    topicList.setTopicList(newTopicSet);
                }
                return topicList;
            }
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: Broker下线前，清除Broker对应的权限
     */
    public int wipeWritePermOfBroker(final String namesrvAddr, String brokerName, final long timeoutMillis)
            throws RemotingCommandException, RemotingConnectException, RemotingSendRequestException,
            RemotingTimeoutException, InterruptedException, MQClientException {
        WipeWritePermOfBrokerRequestHeader requestHeader = new WipeWritePermOfBrokerRequestHeader();
        requestHeader.setBrokerName(brokerName);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.WIPE_WRITE_PERM_OF_BROKER_VALUE,
                    requestHeader);
        RemotingCommand response = this.remotingClient.invokeSync(namesrvAddr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            WipeWritePermOfBrokerResponseHeader responseHeader =
                    (WipeWritePermOfBrokerResponseHeader) response
                        .decodeCommandCustomHeader(WipeWritePermOfBrokerResponseHeader.class);
            return responseHeader.getWipeTopicCount();
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    public void deleteTopicInBroker(final String addr, final String topic, final long timeoutMillis)
            throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topic;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(topic, projectGroupPrefix);
        }

        DeleteTopicRequestHeader requestHeader = new DeleteTopicRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.DELETE_TOPIC_IN_BROKER_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    public void deleteTopicInNameServer(final String addr, final String topic, final long timeoutMillis)
            throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String topicWithProjectGroup = topic;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            topicWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(topic, projectGroupPrefix);
        }

        DeleteTopicRequestHeader requestHeader = new DeleteTopicRequestHeader();
        requestHeader.setTopic(topicWithProjectGroup);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.DELETE_TOPIC_IN_NAMESRV_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    public void deleteSubscriptionGroup(final String addr, final String groupName, final long timeoutMillis)
            throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        // 添加虚拟运行环境相关的projectGroupPrefix
        String groupWithProjectGroup = groupName;
        if (!UtilAll.isBlank(projectGroupPrefix)) {
            groupWithProjectGroup = VirtualEnvUtil.buildWithProjectGroup(groupName, projectGroupPrefix);
        }

        DeleteSubscriptionGroupRequestHeader requestHeader = new DeleteSubscriptionGroupRequestHeader();
        requestHeader.setGroupName(groupWithProjectGroup);
        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.DELETE_SUBSCRIPTIONGROUP_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(addr, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: 从Namesrv获取KV配置
     */
    public String getKVConfigValue(final String namespace, final String key, final long timeoutMillis)
            throws RemotingException, MQClientException, InterruptedException {
        GetKVConfigRequestHeader requestHeader = new GetKVConfigRequestHeader();
        requestHeader.setNamespace(namespace);
        requestHeader.setKey(key);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_KV_CONFIG_VALUE, requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            GetKVConfigResponseHeader responseHeader =
                    (GetKVConfigResponseHeader) response
                        .decodeCommandCustomHeader(GetKVConfigResponseHeader.class);
            return responseHeader.getValue();
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: 添加KV配置
     */
    public void putKVConfigValue(final String namespace, final String key, final String value,
            final long timeoutMillis) throws RemotingException, MQClientException, InterruptedException {
        PutKVConfigRequestHeader requestHeader = new PutKVConfigRequestHeader();
        requestHeader.setNamespace(namespace);
        requestHeader.setKey(key);
        requestHeader.setValue(value);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.PUT_KV_CONFIG_VALUE, requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: 添加KV配置
     */
    public void deleteKVConfigValue(final String namespace, final String key, final long timeoutMillis)
            throws RemotingException, MQClientException, InterruptedException {
        DeleteKVConfigRequestHeader requestHeader = new DeleteKVConfigRequestHeader();
        requestHeader.setNamespace(namespace);
        requestHeader.setKey(key);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.DELETE_KV_CONFIG_VALUE, requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: 通过 server ip 获取 project 信息
     */
    public String getProjectGroupByIp(String ip, final long timeoutMillis) throws RemotingException,
            MQClientException, InterruptedException {
        return getKVConfigValue(NamesrvUtil.NAMESPACE_PROJECT_CONFIG, ip, timeoutMillis);
    }


    /**
     * Name Server: 通过 value 获取所有的 key 信息
     */
    public String getKVConfigByValue(final String namespace, String projectGroup, final long timeoutMillis)
            throws RemotingException, MQClientException, InterruptedException {
        GetKVConfigRequestHeader requestHeader = new GetKVConfigRequestHeader();
        requestHeader.setNamespace(namespace);
        requestHeader.setKey(projectGroup);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_KV_CONFIG_BY_VALUE_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            GetKVConfigResponseHeader responseHeader =
                    (GetKVConfigResponseHeader) response
                        .decodeCommandCustomHeader(GetKVConfigResponseHeader.class);
            return responseHeader.getValue();
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: 获取指定Namespace下的所有KV
     */
    public KVTable getKVListByNamespace(final String namespace, final long timeoutMillis)
            throws RemotingException, MQClientException, InterruptedException {
        GetKVListByNamespaceRequestHeader requestHeader = new GetKVListByNamespaceRequestHeader();
        requestHeader.setNamespace(namespace);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.GET_KVLIST_BY_NAMESPACE_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return KVTable.decode(response.getBody(), KVTable.class);
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }


    /**
     * Name Server: 删除 value 对应的所有 key
     */
    public void deleteKVConfigByValue(final String namespace, final String projectGroup,
            final long timeoutMillis) throws RemotingException, MQClientException, InterruptedException {
        DeleteKVConfigRequestHeader requestHeader = new DeleteKVConfigRequestHeader();
        requestHeader.setNamespace(namespace);
        requestHeader.setKey(projectGroup);

        RemotingCommand request =
                RemotingCommand.createRequestCommand(MQRequestCode.DELETE_KV_CONFIG_BY_VALUE_VALUE,
                    requestHeader);

        RemotingCommand response = this.remotingClient.invokeSync(null, request, timeoutMillis);
        assert response != null;
        switch (response.getCode()) {
        case ResponseCode.SUCCESS_VALUE: {
            return;
        }
        default:
            break;
        }

        throw new MQClientException(response.getCode(), response.getRemark());
    }
}

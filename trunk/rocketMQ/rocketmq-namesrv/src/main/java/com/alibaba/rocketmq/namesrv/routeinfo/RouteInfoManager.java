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
package com.alibaba.rocketmq.namesrv.routeinfo;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.rocketmq.common.DataVersion;
import com.alibaba.rocketmq.common.MixAll;
import com.alibaba.rocketmq.common.TopicConfig;
import com.alibaba.rocketmq.common.constant.LoggerName;
import com.alibaba.rocketmq.common.constant.PermName;
import com.alibaba.rocketmq.common.namesrv.RegisterBrokerResult;
import com.alibaba.rocketmq.common.protocol.body.ClusterInfo;
import com.alibaba.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import com.alibaba.rocketmq.common.protocol.body.TopicList;
import com.alibaba.rocketmq.common.protocol.route.BrokerData;
import com.alibaba.rocketmq.common.protocol.route.QueueData;
import com.alibaba.rocketmq.common.protocol.route.TopicRouteData;
import com.alibaba.rocketmq.remoting.common.RemotingUtil;


/**
 * 运行过程中的路由信息，数据只在内存，宕机后数据消失，但是Broker会定期推送最新数据
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 * @since 2013-7-2
 */
public class RouteInfoManager {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NamesrvLoggerName);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final HashMap<String/* topic */, List<QueueData>> topicQueueTable;
    private final HashMap<String/* brokerName */, BrokerData> brokerAddrTable;
    private final HashMap<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;
    private final HashMap<String/* brokerAddr */, BrokerLiveInfo> brokerLiveTable;


    public RouteInfoManager() {
        this.topicQueueTable = new HashMap<String, List<QueueData>>(1024);
        this.brokerAddrTable = new HashMap<String, BrokerData>(128);
        this.clusterAddrTable = new HashMap<String, Set<String>>(32);
        this.brokerLiveTable = new HashMap<String, BrokerLiveInfo>(256);
    }


    public byte[] getAllClusterInfo() {
        ClusterInfo clusterInfoSerializeWrapper = new ClusterInfo();
        clusterInfoSerializeWrapper.setBrokerAddrTable(this.brokerAddrTable);
        clusterInfoSerializeWrapper.setClusterAddrTable(this.clusterAddrTable);
        return clusterInfoSerializeWrapper.encode();
    }


    public void deleteTopic(final String topic) {
        try {
            try {
                this.lock.writeLock().lockInterruptibly();
                this.topicQueueTable.remove(topic);
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("deleteTopic Exception", e);
        }
    }


    public byte[] getAllTopicList() {
        TopicList topicList = new TopicList();
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                topicList.getTopicList().addAll(this.topicQueueTable.keySet());
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("getAllTopicList Exception", e);
        }

        return topicList.encode();
    }


    /**
     * @return 如果是slave，则返回master的ha地址
     */
    public RegisterBrokerResult registerBroker(//
            final String clusterName,// 1
            final String brokerAddr,// 2
            final String brokerName,// 3
            final long brokerId,// 4
            final String haServerAddr,// 5
            final TopicConfigSerializeWrapper topicConfigWrapper,// 6
            final Channel channel// 7
    ) {
        RegisterBrokerResult result = new RegisterBrokerResult();
        try {
            try {
                this.lock.writeLock().lockInterruptibly();

                // 更新集群信息
                Set<String> brokerNames = this.clusterAddrTable.get(clusterName);
                if (null == brokerNames) {
                    brokerNames = new HashSet<String>();
                    this.clusterAddrTable.put(clusterName, brokerNames);
                }
                brokerNames.add(brokerName);

                boolean registerFirst = false;

                // 更新主备信息
                BrokerData brokerData = this.brokerAddrTable.get(brokerName);
                if (null == brokerData) {
                    registerFirst = true;
                    brokerData = new BrokerData();
                    brokerData.setBrokerName(brokerName);
                    HashMap<Long, String> brokerAddrs = new HashMap<Long, String>();
                    brokerData.setBrokerAddrs(brokerAddrs);

                    this.brokerAddrTable.put(brokerName, brokerData);
                }
                String oldAddr = brokerData.getBrokerAddrs().put(brokerId, brokerAddr);
                registerFirst = registerFirst || (null == oldAddr);

                // 更新Topic信息
                if (null != topicConfigWrapper //
                        && MixAll.MASTER_ID == brokerId) {
                    if (this.isBrokerTopicConfigChanged(brokerAddr, topicConfigWrapper.getDataVersion())//
                            || registerFirst) {
                        ConcurrentHashMap<String, TopicConfig> tcTable =
                                topicConfigWrapper.getTopicConfigTable();
                        if (tcTable != null) {
                            for (String topic : tcTable.keySet()) {
                                TopicConfig topicConfig = tcTable.get(topic);
                                this.createAndUpdateQueueData(brokerName, topicConfig);
                            }
                        }
                    }
                }

                // 更新最后变更时间
                BrokerLiveInfo prevBrokerLiveInfo = this.brokerLiveTable.put(brokerAddr, //
                    new BrokerLiveInfo(//
                        System.currentTimeMillis(), //
                        topicConfigWrapper.getDataVersion(),//
                        channel, //
                        haServerAddr));
                if (null == prevBrokerLiveInfo) {
                    log.info("new broker registerd, {} HAServer: {}", brokerAddr, haServerAddr);
                }

                // 返回值
                if (MixAll.MASTER_ID != brokerId) {
                    String masterAddr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                    if (masterAddr != null) {
                        BrokerLiveInfo brokerLiveInfo = this.brokerLiveTable.get(masterAddr);
                        if (brokerLiveInfo != null) {
                            result.setHaServerAddr(brokerLiveInfo.getHaServerAddr());
                            result.setMasterAddr(masterAddr);
                        }
                    }
                }
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("registerBroker Exception", e);
        }

        return result;
    }


    /**
     * 判断Topic配置信息是否发生变更
     */
    private boolean isBrokerTopicConfigChanged(final String brokerAddr, final DataVersion dataVersion) {
        BrokerLiveInfo prev = this.brokerLiveTable.get(brokerAddr);
        if (null == prev || !prev.getDataVersion().equals(dataVersion)) {
            return true;
        }

        return false;
    }


    public int wipeWritePermOfBrokerByLock(final String brokerName) {
        try {
            try {
                this.lock.writeLock().lockInterruptibly();
                return wipeWritePermOfBroker(brokerName);
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("wipeWritePermOfBrokerByLock Exception", e);
        }

        return 0;
    }


    private int wipeWritePermOfBroker(final String brokerName) {
        int wipeTopicCnt = 0;
        Iterator<Entry<String, List<QueueData>>> itTopic = this.topicQueueTable.entrySet().iterator();
        while (itTopic.hasNext()) {
            Entry<String, List<QueueData>> entry = itTopic.next();
            List<QueueData> qdList = entry.getValue();

            Iterator<QueueData> it = qdList.iterator();
            while (it.hasNext()) {
                QueueData qd = it.next();
                if (qd.getBrokerName().equals(brokerName)) {
                    int perm = qd.getPerm();
                    perm &= ~PermName.PERM_WRITE;
                    qd.setPerm(perm);
                    wipeTopicCnt++;
                }
            }
        }

        return wipeTopicCnt;
    }


    private void createAndUpdateQueueData(final String brokerName, final TopicConfig topicConfig) {
        QueueData queueData = new QueueData();
        queueData.setBrokerName(brokerName);
        queueData.setWriteQueueNums(topicConfig.getWriteQueueNums());
        queueData.setReadQueueNums(topicConfig.getReadQueueNums());
        queueData.setPerm(topicConfig.getPerm());

        List<QueueData> queueDataList = this.topicQueueTable.get(topicConfig.getTopicName());
        if (null == queueDataList) {
            queueDataList = new LinkedList<QueueData>();
            queueDataList.add(queueData);
            this.topicQueueTable.put(topicConfig.getTopicName(), queueDataList);
            log.info("new topic registerd, {} {}", topicConfig.getTopicName(), queueData);
        }
        else {
            boolean addNewOne = true;

            Iterator<QueueData> it = queueDataList.iterator();
            while (it.hasNext()) {
                QueueData qd = it.next();
                if (qd.getBrokerName().equals(brokerName)) {
                    if (qd.equals(queueData)) {
                        addNewOne = false;
                    }
                    else {
                        log.info("topic changed, {} OLD: {} NEW: {}", topicConfig.getTopicName(), qd,
                            queueData);
                        it.remove();
                    }
                }
            }

            if (addNewOne) {
                queueDataList.add(queueData);
            }
        }
    }


    public void unregisterBroker(//
            final String clusterName,// 1
            final String brokerAddr,// 2
            final String brokerName,// 3
            final long brokerId// 4
    ) {
        try {
            try {
                this.lock.writeLock().lockInterruptibly();
                BrokerLiveInfo brokerLiveInfo = this.brokerLiveTable.remove(brokerAddr);
                if (brokerLiveInfo != null) {
                    log.info("unregisterBroker, remove from brokerLiveTable {}, {}", //
                        (brokerLiveInfo != null ? "OK" : "Failed"),//
                        brokerAddr//
                    );
                }

                boolean removeBrokerName = false;
                BrokerData brokerData = this.brokerAddrTable.get(brokerName);
                if (null != brokerData) {
                    String addr = brokerData.getBrokerAddrs().remove(brokerId);
                    log.info("unregisterBroker, remove addr from brokerAddrTable {}, {}", //
                        (addr != null ? "OK" : "Failed"),//
                        brokerAddr//
                    );

                    if (brokerData.getBrokerAddrs().isEmpty()) {
                        this.brokerAddrTable.remove(brokerName);
                        log.info("unregisterBroker, remove name from brokerAddrTable OK, {}", //
                            brokerName//
                        );

                        removeBrokerName = true;
                    }
                }

                if (removeBrokerName) {
                    Set<String> nameSet = this.clusterAddrTable.get(clusterName);
                    if (nameSet != null) {
                        boolean removed = nameSet.remove(brokerName);
                        log.info("unregisterBroker, remove name from clusterAddrTable {}, {}", //
                            (removed ? "OK" : "Failed"),//
                            brokerName//
                        );

                        if (nameSet.isEmpty()) {
                            this.clusterAddrTable.remove(clusterName);
                            log.info("unregisterBroker, remove cluster from clusterAddrTable {}", //
                                clusterName//
                            );
                        }
                    }

                    // 删除相应的topic
                    this.removeTopicByBrokerName(brokerName);
                }
            }
            finally {
                this.lock.writeLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("unregisterBroker Exception", e);
        }
    }


    private void removeTopicByBrokerName(final String brokerName) {
        Iterator<Entry<String, List<QueueData>>> itMap = this.topicQueueTable.entrySet().iterator();
        while (itMap.hasNext()) {
            Entry<String, List<QueueData>> entry = itMap.next();

            String topic = entry.getKey();
            List<QueueData> queueDataList = entry.getValue();
            Iterator<QueueData> it = queueDataList.iterator();
            while (it.hasNext()) {
                QueueData qd = it.next();
                if (qd.getBrokerName().equals(brokerName)) {
                    log.info("removeTopicByBrokerName, remove one broker's topic {} {}", topic, qd);
                    it.remove();
                }
            }

            if (queueDataList.isEmpty()) {
                log.info("removeTopicByBrokerName, remove the topic all queue {}", topic);
                itMap.remove();
            }
        }
    }


    public TopicRouteData pickupTopicRouteData(final String topic) {
        TopicRouteData topicRouteData = new TopicRouteData();
        boolean foundQueueData = false;
        boolean foundBrokerData = false;
        Set<String> brokerNameSet = new HashSet<String>();
        List<BrokerData> brokerDataList = new LinkedList<BrokerData>();
        topicRouteData.setBrokerDatas(brokerDataList);

        try {
            try {
                this.lock.readLock().lockInterruptibly();
                List<QueueData> queueDataList = this.topicQueueTable.get(topic);
                if (queueDataList != null) {
                    topicRouteData.setQueueDatas(queueDataList);
                    foundQueueData = true;

                    // BrokerName去重
                    Iterator<QueueData> it = queueDataList.iterator();
                    while (it.hasNext()) {
                        QueueData qd = it.next();
                        brokerNameSet.add(qd.getBrokerName());
                    }

                    for (String brokerName : brokerNameSet) {
                        BrokerData brokerData = this.brokerAddrTable.get(brokerName);
                        if (null != brokerData) {
                            BrokerData brokerDataClone = new BrokerData();
                            brokerDataClone.setBrokerName(brokerData.getBrokerName());
                            brokerDataClone.setBrokerAddrs((HashMap<Long, String>) brokerData
                                .getBrokerAddrs().clone());
                            brokerDataList.add(brokerDataClone);
                            foundBrokerData = true;
                        }
                    }
                }
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("pickupTopicRouteData Exception", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("pickupTopicRouteData {} {}", topic, topicRouteData);
        }

        if (foundBrokerData && foundQueueData) {
            return topicRouteData;
        }

        return null;
    }

    // Broker Channel两分钟过期
    private final static long BrokerChannelExpiredTime = 1000 * 60 * 2;


    public void scanNotActiveBroker() {
        Iterator<Entry<String, BrokerLiveInfo>> it = this.brokerLiveTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, BrokerLiveInfo> next = it.next();
            long last = next.getValue().getLastUpdateTimestamp();
            if ((last + BrokerChannelExpiredTime) < System.currentTimeMillis()) {
                RemotingUtil.closeChannel(next.getValue().getChannel());
                it.remove();
                log.warn("The broker channel expired, {} {}ms", next.getKey(), BrokerChannelExpiredTime);
                this.onChannelDestroy(next.getKey(), next.getValue().getChannel());
            }
        }
    }


    /**
     * Channel被关闭，或者Channel Idle时间超限
     */
    public void onChannelDestroy(String remoteAddr, Channel channel) {
        String brokerAddrFound = null;

        // 加读锁，寻找断开连接的Broker
        if (channel != null) {
            try {
                try {
                    this.lock.readLock().lockInterruptibly();
                    Iterator<Entry<String, BrokerLiveInfo>> itBrokerLiveTable =
                            this.brokerLiveTable.entrySet().iterator();
                    while (itBrokerLiveTable.hasNext()) {
                        Entry<String, BrokerLiveInfo> entry = itBrokerLiveTable.next();
                        if (entry.getValue().getChannel() == channel) {
                            brokerAddrFound = entry.getKey();
                            break;
                        }
                    }
                }
                finally {
                    this.lock.readLock().unlock();
                }
            }
            catch (Exception e) {
                log.error("onChannelDestroy Exception", e);
            }
        }

        if (null == brokerAddrFound) {
            brokerAddrFound = remoteAddr;
        }

        // 加写锁，删除相关数据结构
        if (brokerAddrFound != null && brokerAddrFound.length() > 0) {
            log.info("the broker's channel destroyed, {}, clean it's data structure at once", brokerAddrFound);

            try {
                try {
                    this.lock.writeLock().lockInterruptibly();
                    // 清理brokerLiveTable
                    this.brokerLiveTable.remove(brokerAddrFound);

                    // 清理brokerAddrTable
                    String brokerNameFound = null;
                    boolean removeBrokerName = false;
                    Iterator<Entry<String, BrokerData>> itBrokerAddrTable =
                            this.brokerAddrTable.entrySet().iterator();
                    while (itBrokerAddrTable.hasNext() && (null == brokerNameFound)) {
                        BrokerData brokerData = itBrokerAddrTable.next().getValue();

                        // 遍历Master/Slave，删除brokerAddr
                        Iterator<Entry<Long, String>> it = brokerData.getBrokerAddrs().entrySet().iterator();
                        while (it.hasNext()) {
                            Entry<Long, String> entry = it.next();
                            Long brokerId = entry.getKey();
                            String brokerAddr = entry.getValue();
                            if (brokerAddr.equals(brokerAddrFound)) {
                                brokerNameFound = brokerData.getBrokerName();
                                it.remove();
                                log.info(
                                    "remove brokerAddr[{}, {}] from brokerAddrTable, because channel destroyed",
                                    brokerId, brokerAddr);
                                break;
                            }
                        }

                        // BrokerName无关联BrokerAddr
                        if (brokerData.getBrokerAddrs().isEmpty()) {
                            removeBrokerName = true;
                            itBrokerAddrTable.remove();
                            log.info("remove brokerName[{}] from brokerAddrTable, because channel destroyed",
                                brokerData.getBrokerName());
                        }
                    }

                    // 清理clusterAddrTable
                    if (brokerNameFound != null && removeBrokerName) {
                        Iterator<Entry<String, Set<String>>> it = this.clusterAddrTable.entrySet().iterator();
                        while (it.hasNext()) {
                            Entry<String, Set<String>> entry = it.next();
                            String clusterName = entry.getKey();
                            Set<String> brokerNames = entry.getValue();
                            boolean removed = brokerNames.remove(brokerNameFound);
                            if (removed) {
                                log.info(
                                    "remove brokerName[{}], clusterName[{}] from clusterAddrTable, because channel destroyed",
                                    brokerNameFound, clusterName);

                                // 如果集群对应的所有broker都下线了， 则集群也删除掉
                                if (brokerNames.isEmpty()) {
                                    log.info(
                                        "remove the clusterName[{}] from clusterAddrTable, because channel destroyed and no broker in this cluster",
                                        clusterName);
                                    it.remove();
                                }

                                break;
                            }
                        }
                    }

                    // 清理topicQueueTable
                    if (removeBrokerName) {
                        Iterator<Entry<String, List<QueueData>>> itTopicQueueTable =
                                this.topicQueueTable.entrySet().iterator();
                        while (itTopicQueueTable.hasNext()) {
                            Entry<String, List<QueueData>> entry = itTopicQueueTable.next();
                            String topic = entry.getKey();
                            List<QueueData> queueDataList = entry.getValue();

                            Iterator<QueueData> itQueueData = queueDataList.iterator();
                            while (itQueueData.hasNext()) {
                                QueueData queueData = itQueueData.next();
                                if (queueData.getBrokerName().equals(brokerNameFound)) {
                                    itQueueData.remove();
                                    log.info(
                                        "remove topic[{} {}], from topicQueueTable, because channel destroyed",
                                        topic, queueData);
                                }
                            }

                            if (queueDataList.isEmpty()) {
                                itTopicQueueTable.remove();
                                log.info(
                                    "remove topic[{}] all queue, from topicQueueTable, because channel destroyed",
                                    topic);
                            }
                        }
                    }
                }
                finally {
                    this.lock.writeLock().unlock();
                }
            }
            catch (Exception e) {
                log.error("onChannelDestroy Exception", e);
            }
        }
    }


    /**
     * 定期打印当前类的数据结构
     */
    public void printAllPeriodically() {
        try {
            try {
                this.lock.readLock().lockInterruptibly();
                log.info("--------------------------------------------------------");

                log.info("topicQueueTable {}", this.topicQueueTable);

                log.info("brokerAddrTable {}", this.brokerAddrTable);

                log.info("brokerLiveTable {}", this.brokerLiveTable);

                log.info("clusterAddrTable {}", this.clusterAddrTable);
            }
            finally {
                this.lock.readLock().unlock();
            }
        }
        catch (Exception e) {
            log.error("printAllPeriodically Exception", e);
        }
    }
}


class BrokerLiveInfo {
    private long lastUpdateTimestamp;
    private DataVersion dataVersion;
    private Channel channel;
    private String haServerAddr;


    public BrokerLiveInfo(long lastUpdateTimestamp, DataVersion dataVersion, Channel channel,
            String haServerAddr) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.dataVersion = dataVersion;
        this.channel = channel;
        this.haServerAddr = haServerAddr;
    }


    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }


    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }


    public DataVersion getDataVersion() {
        return dataVersion;
    }


    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }


    public Channel getChannel() {
        return channel;
    }


    public void setChannel(Channel channel) {
        this.channel = channel;
    }


    public String getHaServerAddr() {
        return haServerAddr;
    }


    public void setHaServerAddr(String haServerAddr) {
        this.haServerAddr = haServerAddr;
    }


    @Override
    public String toString() {
        return "BrokerLiveInfo [lastUpdateTimestamp=" + lastUpdateTimestamp + ", dataVersion=" + dataVersion
                + ", channel=" + channel + ", haServerAddr=" + haServerAddr + "]";
    }
}

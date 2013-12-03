package com.alibaba.rocketmq.tools.command.offset;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.alibaba.rocketmq.common.UtilAll;
import com.alibaba.rocketmq.common.admin.RollbackStats;
import com.alibaba.rocketmq.tools.admin.DefaultMQAdminExt;
import com.alibaba.rocketmq.tools.command.SubCommand;


/**
 * 根据时间来设置消费进度，设置之前要关闭这个订阅组的所有consumer，设置完再启动，方可生效。
 * 
 * @author: manhong.yqd<jodie.yqd@gmail.com>
 * @since: 13-9-12
 */
public class ResetOffsetByTimeSubCommand implements SubCommand {
    @Override
    public String commandName() {
        return "resetOffsetByTime";
    }


    @Override
    public String commandDesc() {
        return "Reset consumer offset by timestamp.";
    }


    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("g", "group", true, "set the consumer group");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("t", "topic", true, "set the topic");
        opt.setRequired(true);
        options.addOption(opt);

        opt =
                new Option("s", "timestamp", true,
                    "set the timestamp[currentTimeMillis|yyyy-MM-dd#HH:mm:ss:SSS]");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("f", "force", true, "set the force rollback by timestamp switch[true|false]");
        opt.setRequired(true);
        options.addOption(opt);
        return options;
    }


    @Override
    public void execute(CommandLine commandLine, Options options) {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt();
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
        try {
            String consumerGroup = commandLine.getOptionValue("g").trim();
            String topic = commandLine.getOptionValue("t").trim();
            String timeStampStr = commandLine.getOptionValue("s").trim();
            long timestamp = 0;
            try {
                // 直接输入 long 类型的 timestamp
                timestamp = Long.valueOf(timeStampStr);
            }
            catch (NumberFormatException e) {
                // 输入的为日期格式，精确到毫秒
                timestamp = UtilAll.parseDate(timeStampStr, UtilAll.yyyy_MM_dd_HH_mm_ss_SSS).getTime();
            }
            boolean force = Boolean.valueOf(commandLine.getOptionValue("f").trim());
            defaultMQAdminExt.start();
            List<RollbackStats> rollbackStatsList =
                    defaultMQAdminExt.resetOffsetByTimestamp(consumerGroup, topic, timestamp, force);
            System.out
                .printf(
                    "rollback consumer offset by specified consumerGroup[%s], topic[%s], force[%s], timestamp(string)[%s], timestamp(long)[%s]\n",
                    consumerGroup, topic, force, timeStampStr, timestamp);

            System.out.printf("%-20s  %-20s  %-20s  %-20s  %-20s  %-20s\n",//
                "#brokerName",//
                "#queueId",//
                "#brokerOffset",//
                "#consumerOffset",//
                "#timestampOffset",//
                "#rollbackOffset" //
            );

            for (RollbackStats rollbackStats : rollbackStatsList) {
                System.out.printf("%-20s  %-20d  %-20d  %-20d  %-20d  %-20d\n",//
                    UtilAll.frontStringAtLeast(rollbackStats.getBrokerName(), 32),//
                    rollbackStats.getQueueId(),//
                    rollbackStats.getBrokerOffset(),//
                    rollbackStats.getConsumerOffset(),//
                    rollbackStats.getTimestampOffset(),//
                    rollbackStats.getRollbackOffset() //
                    );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            defaultMQAdminExt.shutdown();
        }
    }
}

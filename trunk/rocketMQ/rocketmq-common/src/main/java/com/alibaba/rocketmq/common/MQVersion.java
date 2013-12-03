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
package com.alibaba.rocketmq.common;

/**
 * 定义各个版本信息
 * 
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class MQVersion {
    // TODO 每次发布版本都要修改此处版本号
    public static final int CurrentVersion = Version.V3_0_4_SNAPSHOT.ordinal();


    public static String getVersionDesc(int value) {
        Version v = Version.values()[value];
        return v.name();
    }


    public static Version value2Version(int value) {
        return Version.values()[value];
    }

    enum Version {
        V3_0_0_SNAPSHOT,
        V3_0_0_ALPHA1,
        V3_0_0_BETA1,
        V3_0_0_BETA2,
        V3_0_0_BETA3,
        V3_0_0_BETA4,
        V3_0_0_BETA5,
        V3_0_0_BETA6_SNAPSHOT,
        V3_0_0_BETA6,
        V3_0_0_BETA7_SNAPSHOT,
        V3_0_0_BETA7,
        V3_0_0_BETA8_SNAPSHOT,
        V3_0_0_BETA8,
        V3_0_0_BETA9_SNAPSHOT,
        V3_0_0_BETA9,
        V3_0_0_FINAL,
        V3_0_1_SNAPSHOT,
        V3_0_1,
        V3_0_2_SNAPSHOT,
        V3_0_2,
        V3_0_3_SNAPSHOT,
        V3_0_3,
        V3_0_4_SNAPSHOT,
        V3_0_4,
        V3_0_5_SNAPSHOT,
        V3_0_5,
        V3_0_6_SNAPSHOT,
        V3_0_6,
        V3_0_7_SNAPSHOT,
        V3_0_7,
    }
}

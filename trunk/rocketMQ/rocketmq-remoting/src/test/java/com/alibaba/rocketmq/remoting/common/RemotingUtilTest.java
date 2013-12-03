/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.alibaba.rocketmq.remoting.common;

import org.junit.Test;

/**
 * 
 * @author panguso
 * @date 2013年11月15日
 */
public class RemotingUtilTest {

	@Test
	public void testGetLocalAddress() {
		System.out.println(RemotingUtil.getLocalAddress());
	}

}

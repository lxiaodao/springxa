/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.open.jms.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.open.jms.JmsTransactionAwareDataSourceProxy;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/jms-context.xml")
public class MessagingTests {
	
	private static final Log logger = LogFactory.getLog(MessagingTests.class);

	@Autowired
	private JmsTemplate jmsTemplate;

	@Before
	public void onSetUp() throws Exception {
		Thread.sleep(100L);
		getMessages(); // drain queue
		logger.debug("----before convertAndSend messages---");
		jmsTemplate.convertAndSend("queue", "foo");
		jmsTemplate.convertAndSend("queue", "bar");
		logger.debug("----after convertAndSend messages---");
	}

	@Test
	public void testMessaging() throws Exception {
		List<String> list = getMessages();
		System.err.println(list);
		assertEquals(2, list.size());
		assertTrue(list.contains("foo"));
	}

	private List<String> getMessages() {
		String next = "";
		
		List<String> msgs = new ArrayList<String>();
		while (next != null) {
			next = (String) jmsTemplate.receiveAndConvert("queue");
			if (next != null)
				msgs.add(next);
		}
		return msgs;
	}
}

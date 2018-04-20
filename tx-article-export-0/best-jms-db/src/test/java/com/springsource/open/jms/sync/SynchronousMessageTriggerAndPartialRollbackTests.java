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

package com.springsource.open.jms.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.springsource.open.foo.FailureSimulator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/jms-context.xml")
public class SynchronousMessageTriggerAndPartialRollbackTests {

	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private FailureSimulator failureSimulator;

	private SimpleJdbcTemplate jdbcTemplate;
	
	private static final Log logger = LogFactory.getLog(SynchronousMessageTriggerAndPartialRollbackTests.class);
	private long begintime=0;
	
	@Autowired
	public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@BeforeTransaction
	public void clearData() {
		getMessages(); // drain queue
		jmsTemplate.convertAndSend("queue", "foo");
		//jmsTemplate.convertAndSend("queue", "bar");
		jdbcTemplate.update("delete from T_FOOS");
		this.begintime=System.currentTimeMillis();
	}

	@AfterTransaction
	public void checkPostConditions() {
		 this.logger.debug("------excute syschronous ROllback transaction and test time------"+(System.currentTimeMillis()-this.begintime));
		// The database committed...
		assertEquals(1, SimpleJdbcTestUtils.countRowsInTable(jdbcTemplate, "T_FOOS"));
		List<String> list = getMessages();
		// ...but the messages rolled back
		assertEquals(1, list.size());

	}

	@Test
	@Transactional
	@Rollback(false)
	public void testReceiveMessageUpdateDatabase() throws Exception {

		List<String> list = getMessages();
		//assertEquals(1, list.size());
		//assertTrue(list.contains("foo"));

		int id = 0;
		for (String name : list) {			
			jdbcTemplate.update("INSERT INTO T_FOOS (id,name,foo_date) values (?,?,?)", id++, name, new Date());
		}

		//assertEquals(2, SimpleJdbcTestUtils.countRowsInTable(jdbcTemplate, "T_FOOS"));

		failureSimulator.simulateMessageSystemFailure();
	
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

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

package com.springsource.open.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import test.jdbc.datasource.DataSourceInitializer;

public class TransactionPropagationTests extends BaseDatasourceTests  {

	// Inject a service that can be proxied so we can apply tx propagation
	@Autowired
	private AuditService auditService;
	
	private static final Log logger = LogFactory.getLog(TransactionPropagationTests.class);

	@BeforeTransaction
	public void clearData() {
		getJdbcTemplate().update("delete from T_FOOS");
		getOtherJdbcTemplate().update("delete from T_AUDITS");
	}

	@AfterTransaction
	public void checkPostConditions() {

		int count = getJdbcTemplate().queryForInt("select count(*) from T_FOOS");
		// This change was rolled back by the test framework
		assertEquals(0, count);

		count = getOtherJdbcTemplate().queryForInt("select count(*) from T_AUDITS");
		// This one doesn't roll back because of TX propagation
		assertEquals(1, count);
		// log the data
		
		List<Map<String,Object>> alist=getOtherJdbcTemplate().queryForList("select * from T_AUDITS");
		logger.debug("---see the value of T_AUDITS  @AfterTransaction---");
		for(Map<String,Object> map:alist){
			logger.debug("------"+map.values());
		}
		logger.debug("---see the value of T_AUDITS @AfterTransaction---");
		
	}

	@Transactional
	@Test
	public void testInsertIntoTwoDataSources() throws Exception {
		int count;
		count = getOtherJdbcTemplate().queryForInt("select count(*) from T_AUDITS");
		assertEquals(0, count);
		count = getJdbcTemplate().queryForInt("select count(*) from T_FOOS");
		assertEquals(0, count);

		getJdbcTemplate().update(
				"INSERT into T_FOOS (id,name,foo_date) values (?,?,null)", 0,
				"foo");
		count = getJdbcTemplate().queryForInt("select count(*) from T_FOOS");
		assertEquals(1, count);

		auditService.update(0, "INSERT", "foo");
		getOtherJdbcTemplate().update("UPDATE T_AUDITS set name=? where name=?",
				"bar", "foo");
		count = getOtherJdbcTemplate().queryForInt("select count(*) from T_AUDITS");
		assertEquals(1, count);
         List<Map<String,Object>> alist=getOtherJdbcTemplate().queryForList("select * from T_AUDITS");
         logger.debug("---see the value of T_AUDITS OUTER TX REQUIRES_NEW---");
		for(Map<String,Object> map:alist){
			logger.debug("------"+map.values());
		}
		 logger.debug("---see the value of T_AUDITS OUTER TX REQUIRES_NEW---");

	}
}

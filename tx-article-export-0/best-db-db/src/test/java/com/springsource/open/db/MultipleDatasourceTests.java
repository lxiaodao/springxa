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

import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/data-source-context.xml")
public class MultipleDatasourceTests {

	private SimpleJdbcTemplate jdbcTemplate;
	private SimpleJdbcTemplate otherJdbcTemplate;
	
	
	protected final static Log logger = LogFactory.getLog(MultipleDatasourceTests.class);

	private long beigintime=0;

	@Autowired
	public void setDataSources(@Qualifier("dataSource") DataSource dataSource,
			@Qualifier("otherDataSource") DataSource otherDataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
		this.otherJdbcTemplate = new SimpleJdbcTemplate(otherDataSource);
	}

	@BeforeTransaction
	public void clearData() {
		jdbcTemplate.update("delete from T_FOOS");
		otherJdbcTemplate.update("delete from T_AUDITS");
		//
		beigintime=System.currentTimeMillis();
	}

	@AfterTransaction
	public void checkPostConditions() {
		
		logger.debug("------maybe excute time------"+(System.currentTimeMillis()-beigintime));

		int count = jdbcTemplate.queryForInt("select count(*) from T_FOOS");
		// This change was rolled back by the test framework
		assertEquals(0, count);

		count = otherJdbcTemplate.queryForInt("select count(*) from T_AUDITS");
		// This rolls back as well if the connections are managed together
		assertEquals(0, count);

	}

	/**
	 * Vanilla test case for two inserts into two data sources. Both should roll
	 * back.
	 * 
	 * @throws Exception
	 */
	@Transactional
	@Test
	public void testInsertIntoTwoDataSources() throws Exception {

		int count = jdbcTemplate.update(
				"INSERT into T_FOOS (id,name,foo_date) values (?,?,null)", 0,
				"foo");
		assertEquals(1, count);

		count = otherJdbcTemplate
				.update(
						"INSERT into T_AUDITS (id,operation,name,audit_date) values (?,?,?,?)",
						0, "INSERT", "foo", new Date());
		assertEquals(1, count);

	}

	/**
	 * Shows how to check the operation on the inner data source to see if it
	 * has already been committed, and if it has do something different, instead
	 * of just hitting a {@link DataIntegrityViolationException}.
	 * 
	 * @throws Exception
	 */
	@Transactional
	@Test
	public void testInsertWithCheckForDuplicates() throws Exception {

		int count = jdbcTemplate.update(
				"INSERT into T_FOOS (id,name,foo_date) values (?,?,null)", 0,
				"foo");
		assertEquals(1, count);

		count = otherJdbcTemplate
				.update(
						"UPDATE T_AUDITS set operation=?, name=?, audit_date=? where id=?",
						"UPDATE", "foo", new Date(), 0);

		if (count == 0) {
			count = otherJdbcTemplate
					.update(
							"INSERT into T_AUDITS (id,operation,name,audit_date) values (?,?,?,?)",
							0, "INSERT", "foo", new Date());
		}

		assertEquals(1, count);

	}
}

/**
 * 
 */
package com.springsource.open.db;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yang
 * 事务传递Required类型测试
 *
 */
public class TransactionPropagationRequiredTest extends BaseDatasourceTests  {

	// Inject a service that can be proxied so we can apply tx propagation
	@Autowired
	private PropagationRequiredTransactionAuditService requiredAuditService;
	
	private static final Log logger = LogFactory.getLog(TransactionPropagationRequiredTest.class);
	private long begintime=0;

	@BeforeTransaction
	public void clearData() {
		
		getJdbcTemplate().update("delete from T_FOOS");
		getOtherJdbcTemplate().update("delete from T_AUDITS");
		this.begintime=System.currentTimeMillis();
	}

	@AfterTransaction
	
	public void checkPostConditions() {
		logger.debug("---Derby:DISTRIBUTED TRANSACTION EXCUTE Consume Time---"+(System.currentTimeMillis()-begintime));

		
		int count = getOtherJdbcTemplate().queryForInt("select count(*) from T_AUDITS");
		// This one doesn't roll back because of TX propagation
		assertEquals(1, count);
		
		
		count = getJdbcTemplate().queryForInt("select count(*) from T_FOOS");
		// This change was rolled back by the test framework
		assertEquals(1, count);

		
	
		logger.info("---PropagationRequiredTransactionAuditService 0  @AfterTransaction---");
	
		
	}

	@Transactional
	@Test
	public void testInsertIntoTwoDataSources() throws Exception {
		
		getJdbcTemplate().update(
				"INSERT into T_FOOS (id,name,foo_date) values (?,?,null)", 0,
				"foo");
		logger.debug("---insert into T-foos------");

		requiredAuditService.update_audit(0, "INSERT", "foo");
	
		int count = getOtherJdbcTemplate().queryForInt("select count(*) from T_AUDITS");
		// This one doesn't roll back because of TX propagation
		assertEquals(1, count);
      

	}
}
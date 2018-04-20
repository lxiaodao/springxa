/**
 * 
 */
package com.springsource.open.db;

import static org.junit.Assert.assertEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yang
 *
 */
public class XmlContextTransactionPropagationRequiredTest extends XmlBaseSourceTest {
	
	
	private PropagationRequiredTransactionAuditService requiredAuditService;
	
	private static final Log logger = LogFactory.getLog(XmlContextTransactionPropagationRequiredTest.class);
	private long begintime=0;
	
	
	@Before
	public void ready(){
		super.initialize();
		
		requiredAuditService=(PropagationRequiredTransactionAuditService)this.rootcontext.getBean("requiredAuditService");
		getJdbcTemplate().update("delete from T_FOOS");
		getOtherJdbcTemplate().update("delete from T_AUDITS");
		this.begintime=System.currentTimeMillis();
		
	}
	

	@After
	
	public void checkPostConditions() {
		logger.debug("---Derby:DISTRIBUTED TRANSACTION EXCUTE Consume Time---"+(System.currentTimeMillis()-begintime));

		
		int count = getOtherJdbcTemplate().queryForInt("select count(*) from T_AUDITS");
		// This one doesn't roll back because of TX propagation
		assertEquals(1, count);
		
		
		count = getJdbcTemplate().queryForInt("select count(*) from T_FOOS");
		// This change was rolled back by the test framework
		assertEquals(1, count);

		
	
		logger.info("---PropagationRequiredTransactionAuditService 0  @AfterTransaction---");
		super.destory();
	
		
	}
   
	// 114 114 130 126 ms
	@Transactional
	@Test
	public void testInsertIntoTwoDataSources() throws Exception {
		
		getJdbcTemplate().update(
				"INSERT into T_FOOS (id,name,foo_date) values (?,?,null)", 0,
				"foo");
		logger.debug("---insert into T-foos------");

		requiredAuditService.update_audit(0, "INSERT", "foo");
	
		//int count = getOtherJdbcTemplate().queryForInt("select count(*) from T_AUDITS");
		// This one doesn't roll back because of TX propagation
		//assertEquals(1, count);
      

	}

}

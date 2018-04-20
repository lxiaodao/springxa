/**
 * 
 */
package com.springsource.open.db;

import java.util.Date;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yang
 *
 */
@Component("requiredAuditService")
public class PropagationRequiredTransactionAuditService {


	private SimpleJdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(@Qualifier("otherDataSource") DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see com.springsource.open.db.AuditService#update(int, java.lang.String, java.lang.String)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void update_audit(int id, String operation, String name) {
		jdbcTemplate.update(
				"INSERT into T_AUDITS (id,operation,name,audit_date) "
						+ "values (?,?,?,?)", id, operation, name, new Date());
	}

}

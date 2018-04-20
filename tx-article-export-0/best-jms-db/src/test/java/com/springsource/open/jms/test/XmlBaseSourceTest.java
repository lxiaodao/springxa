/**
 * 
 */
package com.springsource.open.jms.test;

import java.io.File;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author yang
 *
 */
public abstract class XmlBaseSourceTest {
	protected JmsTemplate jmsTemplate;

	protected SimpleJdbcTemplate jdbcTemplate;
	

   
	
	
	protected ClassPathXmlApplicationContext rootcontext;
    
	@Before
	public void initialize(){
		rootcontext=new ClassPathXmlApplicationContext (new String[] { "classpath:/META-INF/spring/jms-context.xml"});
	
		this.jdbcTemplate = new SimpleJdbcTemplate((DataSource)rootcontext.getBean("dataSource"));
		this.jmsTemplate = (JmsTemplate)rootcontext.getBean("jmsTemplate");
	
	}
	
	@After
	public void destory(){
		if(rootcontext!=null){
			rootcontext.close();
		}
	}

	

}

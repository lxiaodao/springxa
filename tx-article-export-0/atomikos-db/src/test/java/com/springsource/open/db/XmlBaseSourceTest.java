/**
 * 
 */
package com.springsource.open.db;

import java.io.File;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * @author yang
 *
 */
public abstract class XmlBaseSourceTest {
	private SimpleJdbcTemplate jdbcTemplate;
	private SimpleJdbcTemplate otherJdbcTemplate;
	
	protected SimpleJdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	protected SimpleJdbcTemplate getOtherJdbcTemplate() {
		return otherJdbcTemplate;
	}
   
	
	@BeforeClass
	@AfterClass
	public static void clearLog() {
		// Ensure that Atomikos logging directory exists
		File dir = new File("atomikos");
		if (!dir.exists()) {
			dir.mkdir();
		}
		// ...and delete any stale locks (this would be a sign of a crash)
		File tmlog = new File("atomikos/tmlog.lck");
		if (tmlog.exists()) {
			tmlog.delete();
		}
	}
	
	
	protected ClassPathXmlApplicationContext rootcontext;
    
	@Before
	public void initialize(){
		rootcontext=new ClassPathXmlApplicationContext (new String[] { "classpath:/META-INF/spring/data-source-context.xml"});
		rootcontext.start();
		this.jdbcTemplate = new SimpleJdbcTemplate((DataSource)rootcontext.getBean("dataSource"));
		this.otherJdbcTemplate = new SimpleJdbcTemplate((DataSource)rootcontext.getBean("otherDataSource"));
	
	}
	
	@After
	public void destory(){
		if(rootcontext!=null){
			rootcontext.close();
		}
	}

	

}

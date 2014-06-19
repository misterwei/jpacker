package jpacker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jpacker.factory.JdbcExecutorUtils;
import jpacker.local.AbstractLocalExecutor;
import jpacker.local.PostgresSQLExecutor;
import junit.framework.TestCase;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PostgresTest extends TestCase{
	public void init() throws Exception{
		ComboPooledDataSource cpds =  new ComboPooledDataSource();
		cpds.setDriverClass("org.postgresql.Driver");
		cpds.setUser("postgres");
		cpds.setPassword("123456");
		cpds.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/test");
		cpds.setMinPoolSize(20);
		cpds.setMaxPoolSize(100);
		cpds.setMaxStatements(500);
		cpds.setCheckoutTimeout(1800);
		
		List list = new ArrayList();
		list.add(TestModel.class);
		
		Logger log = LoggerFactory.getLogger(PostgresTest.class);
		
		JdbcExecutorUtils.instanceConfiguration(cpds, list,new PostgresSQLExecutor());
	}
	
	public void testSelectBean() throws Exception{
		init();
		
		JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
		List<TestModel> recs = jdbc.queryForList(TestModel.class, "select * from users",null);
		jdbc.close();
		
		System.out.println(recs);
		
		assertTrue(recs.size() > 0);
	
	}
	
	public void testSelectOne() throws Exception{
		init();
		
		JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
		TestModel recs = jdbc.queryOne(TestModel.class, "select * from users where id=1045");
		jdbc.close();
		
		System.out.println(recs);
		
//		assertTrue(recs.size() > 0);
	
	}
	
	public void testSelectHandlerOne() throws Exception{
		init();
		
		JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
		jdbc.queryForObject(new ResultSetHandler<TestModel>(){

			@Override
			public TestModel handle(ResultSet rs) throws SQLException {
				if(rs.next()){
					System.out.println(rs.getString("username"));
				}
				return null;
			}
			
		}, "select * from users where id=1045",null);
		
		jdbc.close();
	}
	
	public void testSelectHandlerList() throws Exception{
		init();
		
		JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
		jdbc.queryForLimit(new ResultSetHandler<List<TestModel>>(){

			@Override
			public List<TestModel> handle(ResultSet rs) throws SQLException {
				while(rs.next()){
					System.out.println(rs.getString("username"));
				}
				return null;
			}
			
		}, "select * from users ",0,5,null);
		
		jdbc.close();
	}
	
	public void testInsertOne() throws Exception{
		init();
		System.out.println("start");
		JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
		
		jdbc.begin();
		TestModel test = new TestModel();
		test.setName("test100a");
		test.setPassword("pwd100a");
		test.setRole(0);
		test.setStatus("enabled");
		test.setRegtime(new Date());
		
		jdbc.save(test);
		System.out.println(test.getId());
		
		jdbc.commit();
		jdbc.close();
	}
	
	public void testInsert() throws Exception {
		init();
		
		System.out.println("start");
		long sum = 0;
		JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
//		jdbc.setThreadLocal(true);
		jdbc.begin();
		for(int i=0;i<1000;i++){
			long start = System.currentTimeMillis();
			
			TestModel test = new TestModel();
			test.setName("test"+i);
			test.setPassword("pwd"+i);
			test.setRole(0);
			test.setStatus("enabled");
			
			jdbc.save(test);
			System.out.println(test.getId());
			
			if(i%20 == 0){
				jdbc.commit();
				jdbc.begin();
			}
//			jdbc.commit();
			
			long end = System.currentTimeMillis();
			
			sum += end-start;
			
		}
		
		jdbc.commit();
		
		jdbc.close();
		
		System.out.println(sum + "   " + (sum/10000));
//		JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
		
//		JdbcExecutorUtils.relaseJdbcExecutor(jdbc);
		
	}
	
	public void testPageQuery() throws Exception{
		init();
		
		System.out.println("start");
		
		JdbcExecutor jdbc = JdbcExecutorUtils.getJdbcExecutor();
		//List<TestModel> recs = jdbc.select("select * from users", new BeanListHandler<TestModel>(TestModel.class));
		List<Object[]>  list= jdbc.queryForLimit(Object[].class, "select u.username,count(u.username) as usecount from users u group by u.username order by u.username",4,20);
		
		jdbc.close();
		
		for(Object[] map : list){
			System.out.println(Arrays.toString(map));
		}
	
	}
	
}

package jpacker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import jpacker.factory.JpackerUtils;
import jpacker.local.InsertContext;
import jpacker.local.MSSQL2005Executor;
import jpacker.model.SqlParameters;
import junit.framework.TestCase;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlserverTest extends TestCase{
	
	public void testJdbc() throws Exception{
		init();
		JpackerUtils.beginThreadLocal();
		Jpacker jdbc = JpackerUtils.getJpacker();
		TestModel recs = jdbc.queryOne(TestModel.class, "select * from users where id=? or id=?",new SqlParameters(100),new SqlParameters(102));
		jdbc.close();
		
		System.out.println(recs.getMaxId());
		System.out.println(recs.getPassword2());
		
		System.out.println(recs.getTestArray());
		
		JpackerUtils.endThreadLocal();
		System.out.println(recs);

	}
	
	public void testJdbc2() throws Exception{
		init();
		
		Jpacker jdbc = JpackerUtils.getJpacker();
		jdbc.queryOne(Map.class, "select * from users where id=? " ,new SqlParameters(100));
		jdbc.close();
		
//		System.out.println(recs);

	}
	
	public void testCount() throws Exception{

		Jpacker jdbc = JpackerUtils.getJpacker();
		int count = jdbc.queryOne(Integer.class, "select count(*) from users");
		jdbc.close();
		
	}
	
	public void testCount2() throws Exception{
		init();
		long start = System.currentTimeMillis();
		for(int i=0;i<1000;i++){
			testCount();
			if(i %20 == 0){
				Thread.sleep(5000);
				System.gc();
				System.out.println("gc");
				Thread.sleep(5000);
			}
		}
		long end = System.currentTimeMillis();
		
		System.out.println(end-start);
	}
	
	public void testSelectBean() throws Exception{
		init();
		
		Jpacker jdbc = JpackerUtils.getJpacker();
		List<TestModel> recs = jdbc.queryForList(TestModel.class, "select * from users");
		jdbc.close();
		
		System.out.println(recs);
		
		assertTrue(recs.size() > 0);
	
	}
	
	public void testPageQuery() throws Exception{
		init();
		
		Jpacker jdbc = JpackerUtils.getJpacker();
		//List<TestModel> recs = jdbc.select("select * from users", new BeanListHandler<TestModel>(TestModel.class));
		List<Object[]>  list= jdbc.queryForLimit(Object[].class, "select u.username,count(u.username) as usecount from users u group by u.username order by u.username",4,20);
		
		jdbc.close();
		
		for(Object[] map : list){
			System.out.println(Arrays.toString(map));
		}
	
	}
	
	public void testPageQuery2() throws Exception{
		init();

		Jpacker jdbc = JpackerUtils.getJpacker();
		//List<TestModel> recs = jdbc.select("select * from users", new BeanListHandler<TestModel>(TestModel.class));
		List<String>  list= jdbc.queryForLimit(String.class, "select distinct username from users",4,20);
		
		jdbc.close();
		
		for(String str: list){
			System.out.println(str);
		}
	
	}
	
	public void testGet2() throws Exception{
		init();
		
		long sum = 0;
		JpackerUtils.beginThreadLocal();
		
		for(int i=0;i<1;i++){
			Jpacker jdbc2 = JpackerUtils.getJpacker();
			
			long start = System.currentTimeMillis();
			TestModel test = jdbc2.get(TestModel.class, 100);
			System.out.println(test);
			for(TestModel t : test.getTestArray()){
				System.out.println(t.getPassword2() + "  " + t.getMaxId());
			}
			test.getMaxId();
			System.out.println(test);
			
			long end = System.currentTimeMillis();
			
			sum += end-start;
			jdbc2.close();
			
			System.out.println(end-start);
			
		}
		
//		System.out.println(sum +"   "+(sum/2000));
		JpackerUtils.endThreadLocal();
		
	}
	
	public void testGet() throws Exception{
		init();
		
		
		long sum = 0;
		Jpacker jdbc = JpackerUtils.getJpacker();
		
		for(int i=0;i<10;i++){
			Jpacker jdbc2 = JpackerUtils.getJpacker();
			
			long start = System.currentTimeMillis();
			TestModel test = jdbc2.get(TestModel.class, i);
			long end = System.currentTimeMillis();
			
			sum += end-start;
			jdbc2.close();
			System.out.println(end-start);
			
			
		}
//		System.out.println(test);
		
		Jpacker jdbc3 = JpackerUtils.getJpacker();
		long start = System.currentTimeMillis();
		TestModel test = jdbc3.get(TestModel.class, 100);
		long end = System.currentTimeMillis();
		
		sum += end-start;
		
			Jpacker jdbc4 = JpackerUtils.getJpacker();
			
			long start2 = System.currentTimeMillis();
			TestModel test2 = jdbc4.get(TestModel.class, 101);
			long end2 = System.currentTimeMillis();
			
			jdbc4.close();
			System.out.println(end2-start2);
			
		jdbc3.close();
		System.out.println(end-start);
		
		System.out.println(sum +"   "+(sum/2000));
		
		
		
			Jpacker jdbc5 = JpackerUtils.getJpacker();
			
			long start3 = System.currentTimeMillis();
			TestModel test3 = jdbc5.get(TestModel.class, 100);
			long end3 = System.currentTimeMillis();
			System.out.println(end3-start3);
			jdbc5.close();
		
		jdbc.close();
//		System.out.println(recs);
//		
//		assertTrue(recs.size() > 0);
	
	}
	
	public void testInsert() throws Exception {
		init();
		
		
		long sum = 0;
		Jpacker jdbc = JpackerUtils.getJpacker();
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
//		JdbcExecutor jdbc = JdbcExecutorUtils.getJpacker();
		
//		JdbcExecutorUtils.relaseJdbcExecutor(jdbc);
		
	}
	
	public void testDelete() throws Exception {
		init();
		
		TestModel test = new TestModel();
		test.setName("test");
		test.setPassword("pwd");
		test.setRole(0);
		test.setStatus("enabled");
		test.setId(7);
		
		Jpacker jdbc = JpackerUtils.getJpacker();
		jdbc.delete(TestModel.class,7);
		jdbc.close();
		
//		System.out.println(test.getId());
		
		
	}
	
	public void testUpdate() throws Exception {
		init();
		
		TestModel test = new TestModel();
		test.setName("tessssst");
		test.setPassword("pwd");
		test.setRole(0);
		test.setStatus("disabled");
		test.setId(4);
		
		Jpacker jdbc = JpackerUtils.getJpacker();
			jdbc.update(test);
		jdbc.close();
		
		
		System.out.println(test.getId());
		
		
	}
	
	
	public void init() throws Exception{
		ComboPooledDataSource cpds =  new ComboPooledDataSource();
		cpds.setDriverClass("net.sourceforge.jtds.jdbc.Driver");
		cpds.setUser("sa");
		cpds.setPassword("sq123456");
		cpds.setJdbcUrl("jdbc:jtds:sqlserver://127.0.0.1:1433/jpacker_test");
		cpds.setMinPoolSize(20);
		cpds.setMaxPoolSize(100);
		cpds.setMaxStatements(500);
		cpds.setCheckoutTimeout(1800);
		
		List list = new ArrayList();
		list.add(TestModel.class);
		
		JpackerUtils.instanceConfiguration(cpds, list,new MSSQL2005Executor());
		
	}
	
	
	public void testJdbc4() throws Exception{
		ComboPooledDataSource cpds =  new ComboPooledDataSource();
		cpds.setDriverClass("net.sourceforge.jtds.jdbc.Driver");
		cpds.setUser("sa");
		cpds.setPassword("123456");
		cpds.setJdbcUrl("jdbc:jtds:sqlserver://127.0.0.1:1433;databasename=ivrdb");
		cpds.setMinPoolSize(20);
		cpds.setMaxPoolSize(100);
		cpds.setMaxStatements(500);
		cpds.setCheckoutTimeout(1800);
		
		Connection conn = cpds.getConnection();
		
		PreparedStatement ps = conn.prepareStatement("select top 2 * from users");
		ResultSet rs = ps.executeQuery();

		while(rs.next()){
			System.out.println("========="+rs.getString(1));
			PreparedStatement ps2 = conn.prepareStatement("select top 1 * from users");
			ResultSet rs2 = ps2.executeQuery();
			while(rs2.next()){
				System.out.println(rs2.getString(1));
			}
			rs2.close();
			ps2.close();
		}
		rs.close();
		ps.close();
		
		conn.close();
	}
	
	
	public void testInit() throws Exception{
//		ComboPooledDataSource cpds =  new ComboPooledDataSource();
//		cpds.setDriverClass("net.sourceforge.jtds.jdbc.Driver");
//		cpds.setUser("sa");
//		cpds.setPassword("123456");
//		cpds.setJdbcUrl("jdbc:jtds:sqlserver://127.0.0.1:1433;databasename=ivrdb");
//		
//		Connection conn = cpds.getConnection();
//		Connection conn2 = cpds.getConnection();
//		
//		System.out.println(conn.equals(conn));
//		
//		conn.close();
//		conn2.close();
		
		
		ArrayList<String> list = new ArrayList<String>();
		
		long start2 = System.currentTimeMillis();
		for(int i=0;i<10000;i++){
			list.add("hello"+i);
		}
		long end2 = System.currentTimeMillis();
		
		System.out.println("ArrayList add time :"+(end2-start2));
		
		start2 = System.currentTimeMillis();
		for(int i=0;i<10000 ;i++){
			list.remove("hello"+i);
			if(list.size() > 0){
			list.get(0);
			}
		}
		end2 = System.currentTimeMillis();
		
		System.out.println("ArrayList remove time :"+(end2-start2));
		
		
		LinkedHashSet<String> tr = new LinkedHashSet<String>();
		long start = System.currentTimeMillis();
		for(int i=0;i<10000;i++){
			tr.add("hello"+i);
		}
		long end = System.currentTimeMillis();
		
		System.out.println("TreeSet add time :"+(end-start));
		
		start = System.currentTimeMillis();
		for(int i=0;i<10000 ;i++){
			tr.remove("hello"+i);
			Iterator it = tr.iterator();
			while(it.hasNext()){
				it.next();
			}
		}
		end = System.currentTimeMillis();
		
		System.out.println("TreeSet remove time :"+(end-start));
		
		
		HashMap<Integer,String> map = new HashMap<Integer, String>();
		
		long start3 = System.currentTimeMillis();
		for(int i=0;i<10000;i++){
			map.put(i, "hello"+i);
		}
		long end3 = System.currentTimeMillis();
		
		System.out.println("HashMap add time :"+(end3-start3));
		
		start3 = System.currentTimeMillis();
		for(int i=0;i<10000 ;i++){
			map.remove(i);
			map.get(i+1);
		}
		end3 = System.currentTimeMillis();
		
		System.out.println("HashMap remove time :"+(end3-start3));
		
		
		
		
	}
	
	public void testInit2() throws Exception{
		
		long start4 = System.currentTimeMillis();
		String[] sss = new String[1000];
		for(int i=0;i<1000;i++){
			sss[i] = "hello"+i;
		}
		long end4 = System.currentTimeMillis();
		
		System.out.println("String[] add time :"+(end4-start4));
		
		
		start4 = System.currentTimeMillis();
		for(int i=0;i<1000;i++){
			String s = sss[i];
		}
		end4 = System.currentTimeMillis();
		
		System.out.println("String[] remove time :"+(end4-start4));
		
		
		long start5 = System.currentTimeMillis();
		HashSet<String> set = new HashSet<String>();
		for(int i=0;i<1000;i++){
			set.add("hello"+i);
		}
		long end5 = System.currentTimeMillis();
		
		System.out.println("Set add time :"+(end5-start5));
		
		
		start5 = System.currentTimeMillis();
		for(String str : set){
			
		}
		end5 = System.currentTimeMillis();
		
		System.out.println("Set remove time :"+(end5-start5));
		
		
	}
	
	public void testNewObject() throws SQLException {
		
		long start = System.currentTimeMillis();
		for(int i=0;i<10000;i++){
//			ResultSetHandler rs = new ResultSetHandler<Number>() {
//				@Override
//				public Number handle(ResultSet rs) throws SQLException {
//					if(rs != null && rs.next()){
//						rs.getObject(1);
//					}
//					return 0l;
//				}
//			};
//			
//			rs.handle(null);
			
			new InsertContext("", null);
			
		}
		long end = System.currentTimeMillis();
		System.out.println(end-start);
		
	}

	
}

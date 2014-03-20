package jpacker.factory;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import jpacker.JdbcExecutor;
import jpacker.local.LocalExecutor;

public class JdbcExecutorUtils {
	
	private static JdbcExecutorFactory factory;
	public static void instanceConfiguration(DataSource ds,List<Class<?>> clazzs,LocalExecutor localExecutor) throws Exception{
		factory = new JdbcExecutorFactory(new Configuration(ds,clazzs,localExecutor));
	}
	
	public static JdbcExecutor getJdbcExecutor(){
		return factory.getJdbcExecutor();
	}
	
	public static void beginThreadLocal() throws SQLException{
		factory.beginThreadLocal();
	}
	
	public static void endThreadLocal(){
		factory.endThreadLocal();
	}
}

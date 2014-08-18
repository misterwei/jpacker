package jpacker.factory;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import jpacker.Jpacker;
import jpacker.local.LocalExecutor;

public class JpackerUtils {
	
	private static JpackerFactory factory;
	public static void instanceConfiguration(DataSource ds,List<Class<?>> clazzs,LocalExecutor localExecutor) throws Exception{
		factory = new DefaultJpackerFactory(new Configuration(ds,clazzs,localExecutor));
	}
	
	public static Jpacker getJpacker(){
		return factory.getJpacker();
	}
	
	public static void beginThreadLocal() throws SQLException{
		factory.beginThreadLocal();
	}
	
	public static void endThreadLocal(){
		factory.endThreadLocal();
	}
}

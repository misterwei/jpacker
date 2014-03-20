package jpacker.factory;

import java.util.List;

import javax.sql.DataSource;

import jpacker.JdbcExecutor;
import jpacker.connection.ConnectionManager;
import jpacker.impl.DefaultJdbcExecutor;
import jpacker.local.LocalExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Configuration {
	static final Logger log = LoggerFactory.getLogger(Configuration.class);
	
	private TableFactory tableContext = new TableFactory();
	
	private LocalExecutor localExecutor;
	
	private ConnectionManager connManager;
	
	private Class<? extends JdbcExecutor> jdbcExecutorClass;
	
	public Configuration(DataSource ds,List<Class<?>> clazzs,LocalExecutor localExecutor) throws Exception{
		this(ds,clazzs,localExecutor,DefaultJdbcExecutor.class);
	}
	
	public Configuration(DataSource ds,List<Class<?>> clazzs,LocalExecutor localExecutor,Class<? extends JdbcExecutor> jdbcClass) throws Exception{
		
		this.jdbcExecutorClass = jdbcClass;
		
		if(clazzs != null){
			for(int i=0;i<clazzs.size();i++){
				tableContext.loadTableAnnotation(clazzs.get(i));
			}
		}
		
		connManager = new ConnectionManager(ds);
		
		localExecutor.init(tableContext, connManager);
		this.localExecutor = localExecutor;
	}
	
	public LocalExecutor getLocalExecutor(){
		return localExecutor;
	}
	
	public ConnectionManager getConnectionManager(){
		return connManager;
	}
	
	public TableFactory getTableFactory(){
		return tableContext;
	}
	
	public Class<? extends JdbcExecutor> getJdbcExecutorClass(){
		return jdbcExecutorClass;
	}
}

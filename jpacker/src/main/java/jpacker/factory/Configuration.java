package jpacker.factory;

import java.util.List;

import javax.sql.DataSource;

import jpacker.Jpacker;
import jpacker.connection.ConnectionManager;
import jpacker.impl.DefaultJpacker;
import jpacker.local.LocalExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Configuration {
	static final Logger log = LoggerFactory.getLogger(Configuration.class);
	
	private TableFactory tableContext = new TableFactory();
	
	private LocalExecutor localExecutor;
	
	private DataSource ds;
	
	public Configuration(DataSource ds,List<Class<?>> clazzs,LocalExecutor localExecutor) throws Exception{
		this.ds = ds;
		
		if(clazzs != null){
			for(int i=0;i<clazzs.size();i++){
				tableContext.loadTableAnnotation(clazzs.get(i));
			}
		}
	
		this.localExecutor = localExecutor;
	}
	
	public void initLocalExecutor(ConnectionManager cm){
		this.localExecutor.init(tableContext, cm);
	}
	
	public LocalExecutor getLocalExecutor(){
		return localExecutor;
	}
	
	public TableFactory getTableFactory(){
		return tableContext;
	}
	
	public DataSource getDataSource(){
		return ds;
	}
	
}

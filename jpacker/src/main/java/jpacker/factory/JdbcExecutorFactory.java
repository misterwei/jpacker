package jpacker.factory;

import java.sql.SQLException;

import jpacker.JdbcExecutor;
import jpacker.connection.ConnectionHolder;
import jpacker.connection.ConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcExecutorFactory {
	static Logger log = LoggerFactory.getLogger(JdbcExecutorFactory.class);
	
	protected ConnectionManager cm;
	protected Configuration config; 
	
	public JdbcExecutorFactory(Configuration config){
		this.config = config;
		this.cm = config.getConnectionManager();
	}
	
	public JdbcExecutor getJdbcExecutor(){
		return newJdbcExecutor();
	}
	
	public void beginThreadLocal() throws SQLException{
		cm.initThreadConnection();
	}
	
	public void endThreadLocal(){
		ConnectionHolder conn = cm.getThreadConnection();
		if(conn  != null){
			cm.releaseConnection(conn);
		}
	}
	
	
	protected JdbcExecutor newJdbcExecutor(){
		try {
			log.debug("Returns a new JdbcExecutor");
			JdbcExecutor jdbc = config.getJdbcExecutorClass().newInstance();
			
			jdbc.init(config);
			return jdbc;
		} catch (Exception e) {
			log.error("instance error",e);
		}
		return null;
	}
	
	public Configuration getConfiguration(){
		return config;
	}
}

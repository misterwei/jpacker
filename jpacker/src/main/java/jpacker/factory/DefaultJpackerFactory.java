package jpacker.factory;

import java.sql.SQLException;

import jpacker.Jpacker;
import jpacker.connection.ConnectionHolder;
import jpacker.connection.DefaultConnectionManager;
import jpacker.impl.DefaultJpacker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJpackerFactory implements JpackerFactory {
static Logger log = LoggerFactory.getLogger(DefaultJpackerFactory.class);
	
	protected DefaultConnectionManager cm;
	protected Configuration config; 
	
	public DefaultJpackerFactory(Configuration config){
		this.config = config;
		this.cm = new DefaultConnectionManager(config.getDataSource());
		this.config.initLocalExecutor(cm);
	}
	
	@Override
	public Jpacker getJpacker() {
		return newJpacker();
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
	
	
	protected Jpacker newJpacker(){
		try {
			log.debug("Returns a new Jpacker");
			return new DefaultJpacker(config,cm);
		} catch (Exception e) {
			log.error("instance error",e);
		}
		return null;
	}
}

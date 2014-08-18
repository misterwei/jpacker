package jpacker.connection;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConnectionManager implements ConnectionManager{

	static Logger log = LoggerFactory.getLogger(DefaultConnectionManager.class);
	private volatile Integer global_id = 0;
	private ThreadLocal<ConnectionHolder> holders = new ThreadLocal<ConnectionHolder>();
	
	private DataSource ds;
	public DefaultConnectionManager(DataSource ds){
		this.ds = ds;
	}
	
	public ConnectionHolder getThreadConnection() {
		return holders.get();
	}
	
	public void initThreadConnection() throws SQLException{
		ConnectionHolder holder = holders.get();
		if(holder == null){
			holder = getNewConnection();
			holders.set(holder);
		}
	}
	
	public ConnectionHolder getConnection() throws SQLException {
		ConnectionHolder conn = getThreadConnection();
		
		if(conn == null){
			return getNewConnection();
		}else{
			return new ConnectionHolder(conn);
		}

	}
	
	public ConnectionHolder getNewConnection() throws SQLException {
		
		synchronized (global_id) {
			global_id++;
			if(global_id == Integer.MAX_VALUE){
				global_id = 0;
			}
			
			return new ConnectionHolder(global_id,ds.getConnection());
		}
	}
	
	public void releaseConnection(ConnectionHolder holder){
		try {
			holder.release();

			if(holder.equals(holders.get())){
				holders.remove();
				log.debug("threadLocal holders removed");
			}

		} catch (SQLException e) {
			
		}
	}


}

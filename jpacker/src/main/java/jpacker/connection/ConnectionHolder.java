package jpacker.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionHolder {
	static Logger log = LoggerFactory.getLogger(ConnectionHolder.class);
	private Connection connection;
	private boolean isAutoCommit = true;
	
	private boolean isActive = false;
	private ConnectionHolder parentHolder = null;
	private int id;
	
	public ConnectionHolder(int id,Connection connection){
		this.connection = connection;
		isActive = true;
		this.id = id;
		log.debug("create new connection,id:{}",id);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectionHolder other = (ConnectionHolder) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public ConnectionHolder(ConnectionHolder parent){
		this.parentHolder = parent;
	}
	
	public ConnectionHolder getParent(){
		return parentHolder;
	}
	
	public Connection getConnection(){
		if(parentHolder != null){
			return parentHolder.getConnection();
		}else{
			return connection;
		}
	}
	
	public void commit() throws SQLException{
		if(!isAutoCommit){
			if(parentHolder != null){
				parentHolder.commit();
			}else{
				connection.commit();
				connection.setAutoCommit(true);
				log.debug("commit this connection");
			}
			isAutoCommit = true;
		}
	}
	
	public void rollback() throws SQLException{
		if(!isAutoCommit){
			if(parentHolder != null){
				parentHolder.rollback();
			}else{
				connection.rollback();
				log.debug("rollback this connection");
			}
			isAutoCommit = true;
		}
	}
	
	public boolean begin() throws SQLException{
		if(parentHolder != null){
			if(parentHolder.begin()){
				isAutoCommit = false;
				return true;
			}
			return false;
		}else if(isAutoCommit){
			isAutoCommit = false;
			connection.setAutoCommit(false);
			log.debug("begin this connection");
			return true;
		}
		return false;
	}
	
	
	public boolean isActive(){
		if(isActive && parentHolder != null){
			return parentHolder.isActive();
		}else{
			return isActive;
		}
	}
	
	
	protected void release() throws SQLException{
		try{
			if(!isAutoCommit){
				try{
					commit();
				}finally{
					rollback();
				}
			}
			
		}finally{
			isAutoCommit = true;
			isActive = false;
			
			if(parentHolder == null){
				connection.close();
				log.debug("close the connection,id:{}",id);
			}else{
				parentHolder = null;
			}
		}
		
		connection = null;
		
	}
}

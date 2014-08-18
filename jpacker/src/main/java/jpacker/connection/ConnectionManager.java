package jpacker.connection;

import java.sql.SQLException;


public interface ConnectionManager {
	public ConnectionHolder getConnection() throws SQLException;
	
	public void releaseConnection(ConnectionHolder holder);
	
	public ConnectionHolder getNewConnection() throws SQLException;
}

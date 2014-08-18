package jpacker.factory;

import java.sql.SQLException;

import jpacker.Jpacker;

public interface JpackerFactory {
	public Jpacker getJpacker();
	
	public void beginThreadLocal() throws SQLException;
	
	public void endThreadLocal();
	
}

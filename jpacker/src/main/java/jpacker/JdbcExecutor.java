package jpacker;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import jpacker.factory.Configuration;
import jpacker.model.SqlParameters;


public interface JdbcExecutor {
	
	  void init(Configuration confg) throws SQLException;
	
	  <T> T queryOne(Class<T> target,String sql,SqlParameters ...parameters) throws SQLException;
	  
	  <T> List<T> queryForList(Class<T> target, String sql,SqlParameters ...parameters) throws SQLException;

	  <T> List<T> queryForLimit(Class<T> target,String sql,int start,int rows, SqlParameters ...parameters) throws SQLException;
	  
	  <T> T queryForObject(ResultSetHandler<T> handler,String sql,SqlParameters ...parameters) throws SQLException;
	  
	  <T> List<T> queryForLimit(ResultSetHandler<List<T>> handler,String sql,int start,int rows,SqlParameters ...parameters) throws SQLException;
	  
	  int execute(String sql, SqlParameters ...parameters) throws SQLException;
	  
	  int save(Object obj) throws SQLException;
	  
	  int update(Object obj) throws SQLException;
	  
	  int delete(Class<?> type,Serializable id) throws SQLException;
	  
	  <T> T get(Class<T> type,Serializable id) throws SQLException;
	  
	  void commit() throws SQLException;

	  void begin() throws SQLException;

	  void rollback() ;
	  
	  void close() ;
	  
}

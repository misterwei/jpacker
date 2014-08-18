package jpacker;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import jpacker.exception.JpackerException;
import jpacker.model.SqlParameters;


public interface Jpacker {
	
	  <T> T queryOne(Class<T> target,String sql,SqlParameters ...parameters) throws JpackerException;
	  
	  <T> List<T> queryForList(Class<T> target, String sql,SqlParameters ...parameters) throws JpackerException;

	  <T> List<T> queryForLimit(Class<T> target,String sql,int start,int rows, SqlParameters ...parameters) throws JpackerException;
	  
	  <T> T queryForObject(ResultSetHandler<T> handler,String sql,SqlParameters ...parameters) throws JpackerException;
	  
	  <T> List<T> queryForLimit(ResultSetHandler<List<T>> handler,String sql,int start,int rows,SqlParameters ...parameters) throws JpackerException;
	  
	  int execute(String sql, SqlParameters ...parameters) throws JpackerException;
	  
	  int save(Object obj) throws JpackerException;
	  
	  int update(Object obj) throws JpackerException;
	  
	  int delete(Class<?> type,Serializable id) throws JpackerException;
	  
	  <T> T get(Class<T> type,Serializable id) throws JpackerException;
	  
	  void commit() throws JpackerException;

	  void begin() throws JpackerException;

	  void rollback() ;
	  
	  void close() ;
	  
}

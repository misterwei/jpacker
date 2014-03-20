package jpacker.local;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jpacker.DbUtils;
import jpacker.ResultSetHandler;
import jpacker.connection.ConnectionHolder;
import jpacker.connection.ConnectionManager;
import jpacker.factory.TableFactory;
import jpacker.processor.RowUtils;
import jpacker.processor.TableProcessor;
import jpacker.proxy.CglibProxyFactory;
import jpacker.proxy.ProxyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLocalExecutor implements LocalExecutor {
	protected Logger log = LoggerFactory.getLogger(AbstractLocalExecutor.class);
	protected boolean pmdKnownBroken = false;
	protected TableFactory tableFactory = null;
	protected ConnectionManager connectionManager = null;
	protected TableProcessor processor;
	
	
	public void init(TableFactory tableFactory,ConnectionManager connManager){
		this.tableFactory = tableFactory;
		this.connectionManager = connManager;
		
		ProxyFactory proxyFactory = new CglibProxyFactory(tableFactory,this,connManager);
		processor = new TableProcessor(tableFactory, this,proxyFactory);
		
	}
	
	protected String resolveSql(String sql){
		List<String[]> names =  tableFactory.getTableNames();
		for(String[] name : names){
			sql = sql.replace(name[0], name[1]);
		}
		
		return sql;
	}

	protected  <T> T handleSelectOne(Class<T> type,ConnectionHolder conn,String sql,
			Object... parameters) throws SQLException{
		if(log.isDebugEnabled()){
			log.debug("SELECT ONE SQL:{},PARAMETERS:{}",new Object[]{sql,Arrays.toString(parameters)});
		}
		
		sql = resolveSql(sql);
		
		PreparedStatement stmt = null;
        ResultSet rs = null;
        T result = null;
        
        try {
        	
            stmt = this.prepareStatement(conn.getConnection(), sql);
            this.fillStatement(stmt, parameters);
            rs = this.wrap(stmt.executeQuery());
            
            if(rs.next())
            result = handleRow(type,conn,rs);

        } catch (SQLException e) {
            this.rethrow(e, sql, parameters);
            
        } finally {
        	DbUtils.closeQuietly(rs);
            DbUtils.close(stmt);
        }

        return result;
	}
	
	protected  <T> List<T> handleSelectList(Class<T> type,ConnectionHolder conn,String sql,
			Object... parameters) throws SQLException{
		
		if(log.isDebugEnabled()){
			log.debug("SELECT LIST SQL:{},PARAMETERS:{}",new Object[]{sql,Arrays.toString(parameters)});
		}
		
		sql = resolveSql(sql);
		
		PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
        	
            stmt = this.prepareStatement(conn.getConnection(), sql);
            this.fillStatement(stmt, parameters);
            rs = this.wrap(stmt.executeQuery());
            if(rs.next()){
            	List<T> rows = new ArrayList<T>();
            	rows.add(handleRow(type,conn,rs));
            	
    	        while (rs.next()) {
    	            rows.add(handleRow(type,conn,rs));
    	        }
    	        
    	        return rows;
            }

        } catch (SQLException e) {
            this.rethrow(e, sql, parameters);
            
        } finally {
        	DbUtils.closeQuietly(rs);
            DbUtils.close(stmt);
        }

        return null;
	}
	
	protected  <T> T handleSelect(ResultSetHandler<T> handler,ConnectionHolder conn,String sql,
			Object... parameters) throws SQLException{
		if(log.isDebugEnabled()){
			log.debug("SELECT ONE(HANDLER) SQL:{},PARAMETERS:{}",new Object[]{sql,Arrays.toString(parameters)});
		}
		
		sql = resolveSql(sql);
		
		PreparedStatement stmt = null;
        ResultSet rs = null;
        T result = null;
        
        try {
        	
            stmt = this.prepareStatement(conn.getConnection(), sql);
            this.fillStatement(stmt, parameters);
            rs = this.wrap(stmt.executeQuery());
            
            result = handler.handle(rs);

        } catch (SQLException e) {
            this.rethrow(e, sql, parameters);
            
        } finally {
        	DbUtils.closeQuietly(rs);
            DbUtils.close(stmt);
        }

        return result;
	}
	
	 protected <T> T handleRow(Class<T> type,ConnectionHolder conn,ResultSet rs) throws SQLException{
		if(tableFactory.contains(type)){
			return processor.toBean(conn,rs, type);
		}else{
			T result = RowUtils.autoConvert(rs, type);
			if(result == null){
				throw new SQLException("can not find specified type :" + type.getName());
			}
			
			return result;
		}
	 }
	
	 protected int handleUpdate(ConnectionHolder conn,String sql, Object... parameters) throws SQLException {
		
		if(log.isDebugEnabled()){
			log.debug("UPDATE SQL:{},PARAMETERS:{}",new Object[]{sql,Arrays.toString(parameters)});
		}
	
		PreparedStatement stmt = null;
        int rows = 0;
        try {
            stmt = this.prepareStatement(conn.getConnection(), sql);
            this.fillStatement(stmt, parameters);
            rows = stmt.executeUpdate();

        } catch (SQLException e) {
            this.rethrow(e, sql, parameters);

        } finally{
        	DbUtils.close(stmt);
        }

        return rows;
	}
	
	protected void rethrow(SQLException cause, String sql, Object... params)
	        throws SQLException {

        String causeMessage = cause.getMessage();
        if (causeMessage == null) {
            causeMessage = "";
        }
        StringBuilder msg = new StringBuilder(causeMessage);

        msg.append(" Sql: ");
        msg.append(sql);
        msg.append(" Parameters: ");

        if (params == null) {
            msg.append("[]");
        } else {
            msg.append(Arrays.deepToString(params));
        }

        SQLException e = new SQLException(msg.toString(), cause.getSQLState(),
                cause.getErrorCode());
        e.setNextException(cause);

        throw e;
    }
	
	protected PreparedStatement prepareStatement(Connection conn, String sql)
	        throws SQLException {
            
        return conn.prepareStatement(sql);
    }
	
	protected void fillStatement(PreparedStatement stmt, Object... params)
	        throws SQLException {

        if (params == null) {
            return;
        }
        
        ParameterMetaData pmd = null;
        if (!pmdKnownBroken) {
            pmd = stmt.getParameterMetaData();
            if (pmd.getParameterCount() < params.length) {
                throw new SQLException("Too many parameters: expected "
                        + pmd.getParameterCount() + ", was given " + params.length);
            }
        }
        
        for (int i = 0; i < params.length; i++) {
        	int sqlType = Types.VARCHAR;
        	Integer sqlTypeOrNull = null;
        	
        	if (!pmdKnownBroken) {
        		try {
        			sqlType = pmd.getParameterType(i + 1);
        			sqlTypeOrNull = sqlType;
        		} catch (SQLException e) {
        			pmdKnownBroken = true;
        			sqlTypeOrNull = null;
        		}
        	}
        	
        	Object value = convertParameter(sqlTypeOrNull,params[i]);
        	
            if (params[i] != null) {
                stmt.setObject(i + 1, value);
            } else {
                // VARCHAR works with many drivers regardless
                // of the actual column type.  Oddly, NULL and 
                // OTHER don't work with Oracle's drivers.
                stmt.setNull(i + 1, sqlType);
            }
        }
    }
	
	protected ResultSet wrap(ResultSet rs) {
        return rs;
    }
	
	protected Object convertParameter(Integer sqlType, Object value) {
		if(value != null && value instanceof Date){
			long date = ((Date)value).getTime();
			if(sqlType != null){
				switch(sqlType){
					case Types.TIMESTAMP:
						return new java.sql.Timestamp(date);
					case Types.TIME:
						return new java.sql.Time(date);
					case Types.DATE:
						return new java.sql.Date(date);
						
				}
			}
			
			return new java.sql.Timestamp(date);
		}
		return value;
	}
}

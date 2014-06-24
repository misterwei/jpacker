package jpacker.local;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import jpacker.DbUtils;
import jpacker.connection.ConnectionHolder;
import jpacker.model.IdModel;
import jpacker.processor.RowUtils;

public class MSSQL2005Executor extends AbstractLocalExecutor{
	static String SQL_LIMIT_PREFIX = "WITH query AS (SELECT inner_query.*,ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as inner_query_id FROM ("; 
	static String SQL_LIMIT_SUFFIX = " ) inner_query ) SELECT * FROM query WHERE inner_query_id > ? and inner_query_id < ?";
	
	@Override
	public int insert(InsertContext ctx,ConnectionHolder conn) throws SQLException {
		
		int result = handleUpdate(conn, ctx.getSql(), ctx.getParameters());
		Object target = ctx.getTarget();
		if(target != null){
			IdModel idModel = ctx.getTable().getIdModel();
			if(idModel != null && idModel.isIdentity()){
				Class<?> idType = idModel.getPropertyType();
				
				PreparedStatement stmt = null;
		        ResultSet rs = null;
				try {
				    stmt = prepareStatement(conn.getConnection(), "select @@IDENTITY");
		            rs = stmt.executeQuery();
		            
		            if(rs.next()){
		            	Object idValue = RowUtils.autoConvert(rs,idType);
						idModel.getProperty().invokeWrite(target, idValue);
					}
				
				} catch (Exception e) {
					throw new SQLException(e);
				}finally{
					DbUtils.closeQuietly(rs);
		            DbUtils.close(stmt);
				}
			}
		}
		
		return result;
			
	}

	@Override
	public int update(UpdateContext ctx,ConnectionHolder conn) throws SQLException {
		String sql = null;
		if(ctx.getTable() == null){
			sql = resolveSql(ctx.getSql());
		}else{
			sql = ctx.getSql();
		}
		
		return handleUpdate(conn, sql, ctx.getParameters());
	}

	@Override
	public int delete(DeleteContext ctx,ConnectionHolder conn) throws SQLException {
		return handleUpdate(conn, ctx.getSql(),ctx.getParameters());
	}

	@Override
	public <T> T selectOne(SelectContext<T> ctx,ConnectionHolder conn) throws SQLException {
		return handleSelectOne(ctx.getReturnClass(),conn, ctx.getSql(), ctx.getParameters());
	}

	@Override
	public <T> List<T> selectList(SelectListContext<T> ctx,ConnectionHolder conn) throws SQLException {
		
		final Class<T> returnClass = ctx.getReturnClass();
		
		if(ctx.isLimitQuery()){
			String querySelect  = ctx.getSql();
			Object[] parameters = ctx.getParameters();
			
			Object[] newparams = null;
			
			if(querySelect.toLowerCase().matches(".*order\\s+by(\\s*\\,?\\s*[\\w\\._-]+\\s*(desc|asc)?)+\\s*$")){
	    		querySelect = querySelect.replaceFirst("^\\s*(select|SELECT)", "select TOP(?)");
	    		
	    		int paramlength = 0;
	    		if(parameters != null){
	    			paramlength = parameters.length;
	    		}
	    		newparams = new Object[paramlength+3];
	        	int total = ctx.getOffset() + ctx.getLimit() + 1;
	        	newparams[0] = total;
	        	
	        	if(paramlength > 0){
	        		System.arraycopy(parameters, 0, newparams, 1, parameters.length);
	        	}
	        	
	        	newparams[paramlength+1] = ctx.getOffset();
	        	newparams[paramlength+2] = total;
	    		
	    	}else{
	    		int paramlength = 0;
	    		if(parameters != null){
	    			paramlength = parameters.length;
	    		}
	    		
	    		newparams = new Object[paramlength+2];
	        	int total = ctx.getOffset() + ctx.getLimit() + 1;
	        	//newparams[0] = total;
	        	if(paramlength > 0){	
	        		System.arraycopy(parameters, 0, newparams, 0, parameters.length);
	        	}
	        	
	        	newparams[paramlength+0] = ctx.getOffset();
	        	newparams[paramlength+1] = total;

	    	}
		
			String newSql = new StringBuilder(SQL_LIMIT_PREFIX)
							.append(querySelect)
							.append(SQL_LIMIT_SUFFIX)
							.toString();
			
			return handleSelectList(returnClass,conn, newSql,  newparams);
		}else{
			return handleSelectList(returnClass,conn, ctx.getSql(), ctx.getParameters());
		}
		
	}

	public static void main(String[] args){
		String test = "select * from goodsres where startprovince='黑龙江' and startcity='哈尔滨' order by priority desc ,publishtime desc";
		System.out.println(test.matches(".*order\\s+by(\\s*\\,?\\s*[\\w\\._-]+\\s*(desc|asc)?)+\\s*$"));
		System.out.println(test.replaceFirst("^\\s*(select|SELECT)", "select TOP(?)"));
	}
	
	@Override
	public <T> T selectHandler(HandlerContext<T> ctx, ConnectionHolder conn)
			throws SQLException {
		if(ctx.isLimitQuery()){
			String querySelect  = ctx.getSql();
			Object[] parameters = ctx.getParameters();
			
			Object[] newparams = null;
			
			if(querySelect.toLowerCase().matches(".*order\\s+by(\\s*\\,?\\s*[\\w\\._-]+\\s*(desc|asc)?)+\\s*$")){
	    		querySelect = querySelect.replaceFirst("^\\s*(select|SELECT)", "select TOP(?)");
	    		
	    		if(parameters != null){
	    			newparams = new Object[parameters.length+3];
	        		int total = ctx.getOffset() + ctx.getLimit() + 1;
	        		newparams[0] = total;
	        		
	        		System.arraycopy(parameters, 0, newparams, 1, parameters.length);
	        		
	        		newparams[parameters.length+1] = ctx.getOffset();
	        		newparams[parameters.length+2] = total;
	        	}
	    		
	    	}else{
	    		
	    		if(parameters != null){
	    			newparams = new Object[parameters.length+2];
	        		int total = ctx.getOffset() + ctx.getLimit() + 1;
	        		//newparams[0] = total;
	        		
	        		System.arraycopy(parameters, 0, newparams, 0, parameters.length);
	        		
	        		newparams[parameters.length+0] = ctx.getOffset();
	        		newparams[parameters.length+1] = total;
	        		
	        	}
	    	}
			
			if(newparams == null){
        		newparams = new Object[]{ctx.getOffset(),ctx.getLimit()};
        	}
			
			String newSql = new StringBuilder(SQL_LIMIT_PREFIX)
							.append(querySelect)
							.append(SQL_LIMIT_SUFFIX)
							.toString();
			
			return handleSelect(ctx.getHandler(),conn, newSql,  newparams);
		}else{
			return handleSelect(ctx.getHandler(),conn, ctx.getSql(), ctx.getParameters());
		}
	}


}

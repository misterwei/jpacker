package jpacker.local;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jpacker.DbUtils;
import jpacker.connection.ConnectionHolder;
import jpacker.model.IdModel;
import jpacker.processor.RowUtils;

public class MSSQLExecutor extends AbstractLocalExecutor{
	
	@Override
	public int insert( InsertContext ctx,ConnectionHolder conn) throws SQLException {
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
	public int update( UpdateContext ctx,ConnectionHolder conn) throws SQLException {
		String sql = null;
		if(ctx.getTable() == null){
			sql = resolveSql(ctx.getSql());
		}else{
			sql = ctx.getSql();
		}
		return handleUpdate(conn, sql, ctx.getParameters());
	}

	@Override
	public int delete( DeleteContext ctx,ConnectionHolder conn) throws SQLException {
		return handleUpdate(conn, ctx.getSql(), ctx.getParameters());
	}

	@Override
	public <T> T selectOne( SelectContext<T> ctx,ConnectionHolder conn) throws SQLException {
		return handleSelectOne(ctx.getReturnClass(),conn, ctx.getSql(),  ctx.getParameters());
	}

	@Override
	public <T> List<T> selectList(
			SelectListContext<T> ctx,ConnectionHolder conn) throws SQLException {
		
		final Class<T> returnClass = ctx.getReturnClass();
		
		if(ctx.isLimitQuery()){
			String newSql = getLimitString(ctx.getSql(), ctx.getLimit());
			
			PreparedStatement stmt = null;
	        ResultSet rs = null;
	        
	        ArrayList<T> list = new ArrayList<T>();
	        try {
	        	
	            stmt = prepareStatement2(conn.getConnection(), newSql);
	            this.fillStatement(stmt, ctx.getParameters());
	            rs = this.wrap(stmt.executeQuery());
	            
	            while(rs.next()){
	            	handleRow(returnClass, conn, rs);
	            }
	            
	        } catch (SQLException e) {
	            this.rethrow(e, newSql, ctx.getParameters());
	            
	        } finally {
	        	DbUtils.closeQuietly(rs);
	            DbUtils.close(stmt);
	        }
	        
	        return list;
		
		}else{
			return handleSelectList(returnClass,conn, ctx.getSql(), ctx.getParameters());
		}
	}

	private PreparedStatement prepareStatement2(Connection conn,String sql) throws SQLException{
		return conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY);
	}
	
	private static int getAfterSelectInsertPoint(String sql) {
		String lower = sql.toLowerCase();
		int selectIndex = lower.indexOf( "select" );
		final int selectDistinctIndex = lower.indexOf( "select distinct" );
		return selectIndex + ( selectDistinctIndex == selectIndex ? 15 : 6 );
	}


    private String getLimitString(String querySelect, int limit) {
		return new StringBuilder( querySelect.length() + 8 )
				.append( querySelect )
				.insert( getAfterSelectInsertPoint( querySelect ), " top " + limit )
				.toString();
	}

	@Override
	public <T> T selectHandler(HandlerContext<T> ctx, ConnectionHolder conn)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}

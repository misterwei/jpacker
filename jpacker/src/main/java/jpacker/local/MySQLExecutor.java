package jpacker.local;

import java.sql.SQLException;
import java.util.List;

import jpacker.connection.ConnectionHolder;
import jpacker.model.IdModel;
import jpacker.model.TableModel;

public class MySQLExecutor extends AbstractLocalExecutor {

	@Override
	public int insert(InsertContext ctx, ConnectionHolder conn)
			throws SQLException {
		int result = super.handleUpdate(conn, ctx.getSql(), ctx.getParameters());
		
		TableModel table = ctx.getTable();
		
		if(table != null){
			IdModel idModel = ctx.getTable().getIdModel();
			if(idModel != null && idModel.isIdentity()){
				String sequencesql = new StringBuilder()
									.append("SELECT LAST_INSERT_ID()")
									.toString();
				Class<?> idType = idModel.getPropertyType();
				
				Object idValue = handleSelectOne(idType, conn, sequencesql);
				try {
					idModel.getProperty().invokeWrite(ctx.getTarget(), idValue);
				} catch (Exception e) {
					throw new SQLException(e);
				}
			}
		}
		
		return result;
	}

	@Override
	public int update(UpdateContext ctx, ConnectionHolder conn)
			throws SQLException {
		
		String sql = null;
		if(ctx.getTable() == null){
			sql = resolveSql(ctx.getSql());
		}else{
			sql = ctx.getSql();
		}
		
		return super.handleUpdate(conn, sql, ctx.getParameters());
	}

	@Override
	public int delete(DeleteContext ctx, ConnectionHolder conn)
			throws SQLException {
		return super.handleUpdate(conn, ctx.getSql(), ctx.getParameters());
	}

	@Override
	public <T> T selectOne(SelectContext<T> ctx, ConnectionHolder conn)
			throws SQLException {
		return super.handleSelectOne(ctx.getReturnClass(), conn, ctx.getSql(), ctx.getParameters());
	}

	@Override
	public <T> List<T> selectList(SelectListContext<T> ctx,
			ConnectionHolder conn) throws SQLException {
		
		Class<T> returnClass = ctx.getReturnClass();
		
		if(ctx.isLimitQuery()){
			String sql = new StringBuilder().append(ctx.getSql())
							.append(" limit ")
							.append(ctx.getLimit())
							.append(" offset ")
							.append(ctx.getOffset())
							.toString();
			
			return handleSelectList(returnClass, conn, sql, ctx.getParameters());
		}else{
			return handleSelectList(returnClass, conn, ctx.getSql(), ctx.getParameters());
		}
		
	}

	@Override
	public <T> T selectHandler(HandlerContext<T> ctx, ConnectionHolder conn)
			throws SQLException {
		if(ctx.isLimitQuery()){
			String sql = new StringBuilder().append(ctx.getSql())
							.append(" limit ")
							.append(ctx.getLimit())
							.append(" offset ")
							.append(ctx.getOffset())
							.toString();
			
			return handleSelect(ctx.getHandler(), conn, sql, ctx.getParameters());
		}else{
			return handleSelect(ctx.getHandler(), conn, ctx.getSql(), ctx.getParameters());
		}
	}
}

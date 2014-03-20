package jpacker.local;

import java.sql.SQLException;
import java.util.List;

import jpacker.connection.ConnectionHolder;
import jpacker.connection.ConnectionManager;
import jpacker.factory.TableFactory;

public interface LocalExecutor {
	
	public  void init(TableFactory tableFactory,ConnectionManager cm);
	
	public  int insert(InsertContext ctx,ConnectionHolder conn) throws SQLException;
	
	public  int update(UpdateContext ctx,ConnectionHolder conn) throws SQLException;
	
	public  int delete(DeleteContext ctx,ConnectionHolder conn) throws SQLException;
	
	public <T> T  selectOne(SelectContext<T> ctx,ConnectionHolder conn) throws SQLException;
	
	public <T> List<T> selectList(SelectListContext<T> ctx,ConnectionHolder conn) throws SQLException;
	
	public <T> T  selectHandler(HandlerContext<T> ctx,ConnectionHolder conn) throws SQLException;
	
}

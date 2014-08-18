package jpacker.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import jpacker.Jpacker;
import jpacker.ResultSetHandler;
import jpacker.connection.ConnectionHolder;
import jpacker.connection.ConnectionManager;
import jpacker.exception.JpackerException;
import jpacker.factory.Configuration;
import jpacker.factory.TableFactory;
import jpacker.local.DeleteContext;
import jpacker.local.HandlerContext;
import jpacker.local.InsertContext;
import jpacker.local.LocalExecutor;
import jpacker.local.SelectContext;
import jpacker.local.SelectListContext;
import jpacker.local.UpdateContext;
import jpacker.model.SqlParameters;
import jpacker.model.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 只能在当前线程中使用，不可传入其他线程中调用。数据库处理类。
 * @author cool
 *
 */
public class DefaultJpacker implements Jpacker {
	static final Logger log = LoggerFactory.getLogger(DefaultJpacker.class);
	
	
	private LocalExecutor localExecutor = null;
	
	private TableFactory tableFactory = null;
	
	private ConnectionManager connManager = null;
	
	private ConnectionHolder holder;
	
	public DefaultJpacker(Configuration config,ConnectionManager cm) throws JpackerException{

		this.connManager = cm;
		this.localExecutor = config.getLocalExecutor();
		this.tableFactory = config.getTableFactory();
		
		try{
			this.holder = connManager.getConnection();
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}
	
	/**
	 * 提交
	 */
	public void commit() throws JpackerException {
		try{
			holder.commit();
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}
	
	/**
	 * 事务开始
	 */
	
	public void begin() throws JpackerException {
		if(log.isDebugEnabled()){
			log.debug("begin the JdbcExecutor's transaction");
		}
		try{
			holder.begin();
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}
	
	/**
	 * 事务回滚
	 */
	
	public void rollback() {
		if(log.isDebugEnabled()){
			log.debug("rollback the JdbcExecutor's transaction");
		}
		try{
			holder.rollback();
		}catch(Exception e){
			log.error("rollback error",e);
		}
	}

	
	public void close() {
		
		log.debug("colse the JdbcExecutor's connection. ");
		connManager.releaseConnection(holder);
		
	}

	private Object[] getParameters(SqlParameters parameters[]){
		if(parameters == null || parameters.length == 0){
			return null;
		}
		
		SqlParameters first = parameters[0];
		if(first == null)
			return null;
		
		for(int i=1;i<parameters.length;i++){
			first.add(parameters[i]);
		}
		
		return first.getArray();
	}

	@Override
	public <T> T queryOne(Class<T> target, String sql, SqlParameters ...parameters)
			throws JpackerException {
		Object[] array = getParameters(parameters);
		try{
			return localExecutor.selectOne(new SelectContext<T>(target, sql, array),holder);
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public <T> List<T> queryForList(Class<T> target, String sql,
			SqlParameters ...parameters) throws JpackerException {
		Object[] array = getParameters(parameters);
		try{
			return localExecutor.selectList( new SelectListContext<T>(target, sql,array),holder);
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public <T> List<T> queryForLimit(Class<T> target, String sql, int start,
			int rows, SqlParameters ...parameters) throws JpackerException {
		Object[] array = getParameters(parameters);
		try{
			return localExecutor.selectList( new SelectListContext<T>(target, sql,start,rows,array),holder);
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public int execute(String sql, SqlParameters ...parameters) throws JpackerException {
		Object[] array = getParameters(parameters);
		try{
			return localExecutor.update( new UpdateContext(sql, array),holder);
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public int save(Object obj) throws JpackerException {
		TableModel tableModel = tableFactory.get(obj.getClass());
		try {
			return localExecutor.insert( new InsertContext(tableModel, obj),holder);
		} catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public int update(Object obj) throws JpackerException {
		TableModel tableModel = tableFactory.get(obj.getClass());
		try{
			return localExecutor.update( new UpdateContext(tableModel, obj),holder);
		} catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public int delete(Class<?> type, Serializable rowid) throws JpackerException {
		TableModel tableModel = tableFactory.get(type);
		try{	
			return localExecutor.delete( new DeleteContext(tableModel, rowid),holder);
		} catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public <T> T get(Class<T> type, Serializable rowid) throws JpackerException {
		TableModel tableModel = tableFactory.get(type);
		try{
			return localExecutor.selectOne( new SelectContext<T>(tableModel, rowid),holder);
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public <T> T queryForObject(ResultSetHandler<T> handler, String sql,
			SqlParameters ...parameters) throws JpackerException {
		Object[] array = getParameters(parameters);
		try{
			return localExecutor.selectHandler(new HandlerContext<T>(handler,sql,array), holder);
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}

	@Override
	public <T> List<T> queryForLimit(ResultSetHandler<List<T>> handler,
			String sql, int start, int rows, SqlParameters ...parameters)
			throws JpackerException {
		Object[] array = getParameters(parameters);
		try{
			return localExecutor.selectHandler(new HandlerContext<List<T>>(handler, sql, start, rows, array), holder);
		}catch(Exception e){
			throw new JpackerException(e);
		}
	}

}

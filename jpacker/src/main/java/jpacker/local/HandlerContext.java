package jpacker.local;

import jpacker.ResultSetHandler;

public class HandlerContext<T> implements SqlContext{
	private ResultSetHandler<T> handler;
	private String sql;
	private Object[] parameters;
	
	private int start = -1;
	private int rows = -1;
	private boolean isLimitQuery = false;
	
	public HandlerContext(ResultSetHandler<T> handler,String sql,Object[] params){
		this.handler = handler;
		this.sql = sql;
		this.parameters = params;
	}
	
	public HandlerContext(ResultSetHandler<T> handler,String sql,int start,int rows,Object[] params){
		this.handler = handler;
		this.sql = sql;
		this.parameters = params;
		this.start = start;
		this.rows = rows;
		isLimitQuery = true;
	}
	
	@Override
	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public String getSql() {
		return sql;
	}

	public ResultSetHandler<T> getHandler(){
		return handler;
	}
	
	public int getOffset() {
		return start;
	}

	public int getLimit() {
		return rows;
	}

	public boolean isLimitQuery(){
		return isLimitQuery;
	}
}

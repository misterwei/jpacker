package jpacker.local;

import jpacker.model.TableModel;

/**
 * dialect查询 所用到的 上下文，如果本地化不能通用这个sql可以通过参数进行校正
 * @author cool
 *
 */
public class SelectContext<T> implements SqlContext{
	static String SELECTONE_PREFIX = "select * from ";
	private String selectSql;    //标准通用的select sql语句
	private Object[] parameters;   //参数
	
	private Class<T> returnClass;
	
	/**
	 * count and query 
	 * 
	 * @param standardSql
	 * @param parameters
	 */
	public SelectContext(Class<T> returnClass,String selectSql,Object[] parameters){
		this.returnClass = returnClass;
		this.selectSql = selectSql;
		this.parameters = parameters;
	}
	
	/**
	 * page query
	 * 
	 * @param standardSql
	 * @param parameters
	 * @param start
	 * @param end
	 */
	
	@SuppressWarnings("unchecked")
	public SelectContext(TableModel table,Object id){
		
		StringBuilder sb = new StringBuilder();
		sb.append(SELECTONE_PREFIX)
			.append(table.getName())
			.append(" where ")
			.append(table.getIdModel().getName())
			.append("=?");
		
		this.selectSql = sb.toString();
		parameters = new Object[]{id};
		this.returnClass = (Class<T>)table.getTargetClass();
		
	}
	
	public String getSql() {
		return selectSql;
	}
	
	public Object[] getParameters() {
		return parameters;
	}

	public Class<T> getReturnClass(){
		return returnClass;
	}
}

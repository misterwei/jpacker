package jpacker.local;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import jpacker.model.TableModel;

public class DeleteContext implements SqlContext{
	static String DELETE_PREFIX = "delete from ";
	private TableModel table;
	
	private String sql;
	private Object[] parameters;
	
	
	public DeleteContext(TableModel tableModel,Serializable id) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		
		this.table = tableModel;
		
		StringBuilder sql = new StringBuilder(DELETE_PREFIX);
		sql.append(tableModel.getName())
			.append(" where ")
			.append(tableModel.getIdModel().getName())
			.append("=?");
		
		this.sql = sql.toString();
		parameters = new Object[]{id};
	}
	
	public Object[] getParameters() {
		return parameters;
	}

	public String getSql() {
		return sql;
	}

	public TableModel getTableModel(){
		return table;
	}
	
}

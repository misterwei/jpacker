package jpacker.local;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import jpacker.model.ColumnModel;
import jpacker.model.IdModel;
import jpacker.model.TableModel;

public class UpdateContext implements SqlContext{
	static String UPDATE_PREFIX = "update ";
	private String updateSql;
	private Object[] parameters;
	
	private TableModel table;
	
	public UpdateContext(TableModel tableModel,Object obj) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		this.table = tableModel;
		
		ColumnModel[] columnModels = tableModel.getColumnModels();
		StringBuilder sql = new StringBuilder(UPDATE_PREFIX).append(tableModel.getName()).append(" set ");
		
		ArrayList<Object> params = new ArrayList<Object>(columnModels.length);
		
		for(ColumnModel columnModel : columnModels){
			if(!columnModel.isWriteable()){
				continue;
			}
			sql.append(columnModel.getPropertyName()).append("=?,");
			
			params.add( columnModel.getProperty().invokeRead(obj));
			
		}
		
		sql.deleteCharAt(sql.length()-1);
		
		IdModel idModel = tableModel.getIdModel();
		sql.append(" where ").append(idModel.getName()).append("=?");
		
		params.add( idModel.getProperty().invokeRead(obj));
		
		
		updateSql = sql.toString();
		parameters = params.toArray();
	}
	
	public UpdateContext(String sql,Object[] parameters){
		this.updateSql = sql;
		this.parameters = parameters;
	}
	
	public TableModel getTable() {
		return table;
	}
	public Object[] getParameters() {
		return parameters;
	}
	public String getSql(){
		return updateSql;
	}

}

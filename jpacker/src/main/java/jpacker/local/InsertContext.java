package jpacker.local;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import jpacker.model.ColumnModel;
import jpacker.model.IdModel;
import jpacker.model.TableModel;

public class InsertContext implements SqlContext{
	static String INSERT_PREFIX = "insert into ";
	private String updateSql;
	private Object[] parameters;
	
	private TableModel table;
	private Object target;
	
	public InsertContext(TableModel tableModel,Object obj) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		this.table = tableModel;
		this.target = obj;
		
		IdModel idModel = table.getIdModel();
		StringBuilder sql = new StringBuilder(INSERT_PREFIX).append(tableModel.getName()).append(" (");
		
		StringBuilder sql_values = new StringBuilder(" values (");
		
		ColumnModel[] columnModels = tableModel.getColumnModels();
		
		ArrayList<Object> params = new ArrayList<Object>(columnModels.length);
		if(idModel != null && !idModel.isIdentity()){
			
			parameters = new Object[columnModels.length+1];
			for(int i=0;i<columnModels.length;i++){
				ColumnModel columnModel  = columnModels[i];
				
				if(!columnModel.isWriteable()){
					continue;
				}
				
				Object value = columnModel.getProperty().invokeRead(obj);
				
				String name = columnModel.getName();
				sql.append(name).append(",");
				sql_values.append("?").append(",");
				
				params.add(value);
			}
			
			Object idValue = idModel.getProperty().invokeRead(obj);
			
			sql.append(idModel.getName()).append(")");
			sql_values.append("?)");
			params.add(idValue);
			
		}else{
			
			for(ColumnModel columnModel  : columnModels){
				
				if(!columnModel.isWriteable()){
					continue;
				}
				
				Object value = columnModel.getProperty().invokeRead(obj);
				
				String name = columnModel.getName();
				sql.append(name).append(",");
				sql_values.append("?").append(",");
				
				params.add(value);
			}
			
			sql.deleteCharAt(sql.length() - 1);
			sql_values.deleteCharAt(sql_values.length() - 1);
			
			sql.append(")");
			sql_values.append(")");
			
		}
		
		updateSql = sql.append(sql_values).toString();
		parameters = params.toArray();
//		updateSql = "insert into users (username, password, realname, role, status) values (?, ?, ?, ?, ?)";
//		parameters = new Object[]{"ssssss","dddddd","dasdfasfas",1,"cccc"};
	}
	
	public InsertContext(String sql,Object[] parameters){
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
	public Object getTarget(){
		return target;
	}
}

package jpacker.model;

import java.util.ArrayList;
import java.util.List;


/**
 * 表对应的实体类的 模型，主要用于sql语句的组装
 * @author cool
 *
 */
public class TableModel {
	private String name; // 表名称
	private String alias; // 表别名
	
	private Class<?> targetClass;
	
	private ColumnModel[] columns;
	private ColumnModel[] allColumns;
	
	private SelectModel[] lazySelects;
	private SelectModel[] timelySelects;
	
	private boolean lazy = false;
	private IdModel id;
	
	public TableModel(Class<?> targetClass,String name,String alias,IdModel id,ColumnModel[] columns,SelectModel[] selects){
		this.name = name;
		this.alias = alias;
		this.id = id;
		this.columns = columns;
		
		this.targetClass = targetClass;
		
		List<SelectModel> lazys = new ArrayList<SelectModel>();
		List<SelectModel> timelys = new ArrayList<SelectModel>();
		
		for(SelectModel select: selects){
			if(select.isLazy() || select.getTargetType() == targetClass){
				lazy = true;
				lazys.add(select);
			}else{
				timelys.add(select);
			}
		}
		
		
		allColumns = new ColumnModel[columns.length+1];
		
		System.arraycopy(columns, 0, allColumns, 0, columns.length);
		
		allColumns[columns.length] = id;
		
		lazySelects = new SelectModel[lazys.size()];
		lazys.toArray(lazySelects);
		
		timelySelects = new SelectModel[timelys.size()];
		timelys.toArray(timelySelects);
		
	}
	
	public boolean isLazy(){
		return lazy;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public IdModel getIdModel(){
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<?> getTargetClass(){
		return targetClass;
	}
	
	public String toString(){
		return new StringBuilder("table:")
		.append(name)
		.append("{")
		.append("id:")
		.append(id)
		.append(",alias:")
		.append(alias)
		.append(",columns:")
		.append(columns)
		.append(",lazyselects:")
		.append(lazySelects)
		.append(",timelyselects:")
		.append(timelySelects)
		.append("}")
		.toString();
	}

	public ColumnModel[] getColumnModels(){
		return columns;
	}
	
	public ColumnModel[] getAllColumnModels(){
		return allColumns;
	}
	
	public SelectModel[] getLazySelectModels(){
		return lazySelects;
	}

	public SelectModel[] getTimelySelectModels(){
		return timelySelects;
	}
}

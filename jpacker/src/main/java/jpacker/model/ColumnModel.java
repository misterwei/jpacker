package jpacker.model;

import jpacker.annotation.Column;

/**
 * 列模型
 * @author cool
 *
 */
public class ColumnModel {
	private String name;        //列名称
	private String defaultValue;//默认值
	private Object value;       //字段值
	private boolean writeable;
	
	private SimpleProperty property;
	
	public ColumnModel(SimpleProperty property,String name,String defaultValue,boolean writeable){
		
//		this.property = property;
		
		if("".equals(name)){
			this.name = property.getName();
		}else{
			this.name = name;
		}
		
		if("".equals(defaultValue)){
			this.defaultValue = null;
		}else{
			this.defaultValue = defaultValue;
		}
		this.writeable = writeable;
		
		this.property = property;
	}
	
	public ColumnModel(SimpleProperty property,String name,String defaultValue){
		this(property,name,defaultValue,true);
	}
	
	public ColumnModel(SimpleProperty property,Column column){
		this(property,column.name(),column.defaultValue(),column.writeable());
	}
	

	public Class<?> getPropertyType() {
		return property.getType();
	}
	
	public String getPropertyName(){
		return property.getName();
	}
	
	public SimpleProperty getProperty(){
		return property;
	}
	
	public String getName() {
		return name;
	}
	public String getDefaultValue() {
		return defaultValue;
	}

	public Object getValue() {
		return value;
	}


	public boolean isWriteable() {
		return writeable;
	}

}

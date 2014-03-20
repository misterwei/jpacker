package jpacker.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jpacker.annotation.Select;

public class SelectModel {
	private String sql;                     //sql 语句
	private int offset;        //起始记录，可以直接制定数值，也可以使用属性名称的值
	private int limit;		 //结束记录，可以直接制定数值，也可以使用属性名称的值
	private SimpleProperty[] refParameters; //sql 参数，使用指定属性名称
	private boolean lazy;     //是否延迟加载
	private String name;
	
	private Map<Integer,SimpleProperty> properties = new HashMap<Integer,SimpleProperty>();
	private Map<Integer,Class<?>> targetTypes = new HashMap<Integer, Class<?>>(); 
	private boolean returnList = false;
	private boolean hasRef = false;
	
	public SelectModel(SimpleProperty property,Select select,SimpleProperty[] refParams){
		this.sql = select.sql();
		this.offset = select.offset();
		this.limit = select.limit();
		this.lazy = select.lazy();
		this.name = select.name();
		
		properties.put(1, property);
		Class<?> propertyType = property.getType();
		if(propertyType != List.class){
			this.targetTypes.put(1, propertyType);
		}else{
			this.returnList = true;
			this.targetTypes.put(1, select.targetType());
		}
		
		this.refParameters = refParams;
	}
	
	public String getSql() {
		return sql;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getLimit() {
		return limit;
	}
	
	public SimpleProperty[] getRefProperties() {
		return refParameters;
	}
	
	public boolean containsMethod(String methodName){
		Collection<SimpleProperty> vs = properties.values();
		for(SimpleProperty tsp : vs){
			if(tsp.getReadName().equals(methodName)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isLazy() {
		return lazy;
	}
	
	public Class<?> getTargetType(){
		return targetTypes.get(1);
	}
	
	public boolean isReturnList(){
		return returnList;
	}
	
	public SimpleProperty getProperty(){
		return properties.get(1);
	}
	
	public SimpleProperty getProperty(int columnIndex){
		return properties.get(columnIndex);
	}
	
	public Class<?> getTargetType(int columnIndex){
		return targetTypes.get(columnIndex);
	}
	
	public void addRefSelect(int columnIndex,Class<?> targetType,SimpleProperty p){
		if(columnIndex < 2){
			throw new RuntimeException("This index subscript is not valid, must be greater than 1");
		}
		this.properties.put(columnIndex, p);
		this.targetTypes.put(columnIndex, targetType);
		this.hasRef = true;
	}
	
	public boolean isHasRef() {
		return hasRef;
	}

	public String getName(){
		return name;
	}
}

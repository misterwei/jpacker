package jpacker.factory;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jpacker.annotation.Column;
import jpacker.annotation.Exclude;
import jpacker.annotation.Id;
import jpacker.annotation.RefSelect;
import jpacker.annotation.Select;
import jpacker.annotation.Table;
import jpacker.model.ColumnModel;
import jpacker.model.IdModel;
import jpacker.model.SelectModel;
import jpacker.model.SimpleProperty;
import jpacker.model.TableModel;
import net.sf.cglib.reflect.FastClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableFactory {
	static Logger log = LoggerFactory.getLogger(TableFactory.class);
	private Map<String,TableModel> tableMap = new HashMap<String, TableModel>(); 
	private List<String[]> tableNames = new ArrayList<String[]>();
	
	
	public TableModel get(Class<?> clazz){
		return tableMap.get(getKey(clazz));
	}

	public boolean contains(Class<?> clazz){
		return tableMap.containsKey(getKey(clazz));
	}
	
	public final List<String[]>  getTableNames(){
		return tableNames;
	}
	
	public Collection<TableModel> getTableModels(){
		return tableMap.values();
	}
	
	private String getKey(Class<?> clazz){
		return clazz.getSimpleName();
	}
	
	public synchronized void loadTableAnnotation(Class<?> clazz) throws Exception{
		
		if(!clazz.isAnnotationPresent(Table.class)){
			log.warn("Class ["+clazz.getName()+"] 没有注解配置,不能进行加载");
			return ;
		}
		
		String cName = getKey(clazz);
		
		if(tableMap.containsKey(cName)){
			log.warn("已经加载了 Class [{}],不能重复加载",cName);
			return;
		}
		
		Table tableAnnotation = (Table)clazz.getAnnotation(Table.class);
		String name = tableAnnotation.name();
		String alias = tableAnnotation.alias();
		
		FastClass fc = FastClass.create(clazz);
		
		//加载JoinColumn 和 SelectColumn
		PropertyDescriptor[] properties = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
		List<ColumnModel> columns = new ArrayList<ColumnModel>();
		List<SelectModel> selects = new ArrayList<SelectModel>();
		
		List<Object[]> refSelects = new ArrayList<Object[]>();
		
		IdModel idModel = null;
		
		if(properties != null && properties.length > 0){
			Map<String,SimpleProperty> simpleProperties = new HashMap<String,SimpleProperty>();
			
			for(PropertyDescriptor descriptor : properties){
				Method read = descriptor.getReadMethod();
				Method write = descriptor.getWriteMethod();
				if(read != null && write != null && !read.isAnnotationPresent(Exclude.class)){
					String propertyName = descriptor.getName();
					String readM = read.getName();
					String writeM = write.getName();
					Class<?> pt  = descriptor.getPropertyType();
					simpleProperties.put(propertyName, new SimpleProperty(propertyName,pt ,fc.getMethod(readM, null) ,fc.getMethod(writeM, new Class[]{pt})));
				}
			}
			
			
			for(PropertyDescriptor descriptor : properties){
//				Class propertyType = descriptor.getPropertyType();
//				log.debug("property name :"+ descriptor.getName()+" type:"+propertyType.getName());
				Method read = descriptor.getReadMethod();
				Method write = descriptor.getWriteMethod();
				if(read != null && write != null && !read.isAnnotationPresent(Exclude.class)){
					String propertyName = descriptor.getName();
					
					if(read.isAnnotationPresent(Column.class)){ //列 注解
						Column column = (Column)read.getAnnotation(Column.class);
						columns.add(new ColumnModel(simpleProperties.get(propertyName), column));
						
					}else if(read.isAnnotationPresent(Id.class)){ //主键 注解
						Id id = (Id)read.getAnnotation(Id.class);
						idModel = new IdModel(simpleProperties.get(propertyName), id);
						
					}else if(read.isAnnotationPresent(Select.class)){ //Select 查询注解
						Select select = (Select)read.getAnnotation(Select.class);
						
						SimpleProperty[] sp = null;
						String[] params = select.refProperties();
						if(!(params.length == 1 && params[0].equals(""))){
							sp = new SimpleProperty[params.length];
							for(int i=0;i<params.length;i++){
								sp[i] = simpleProperties.get(params[i]);
							}
						}
						selects.add(new SelectModel(simpleProperties.get(propertyName), select,sp));
						
					}else if(read.isAnnotationPresent(RefSelect.class)){ //Select 查询注解
						RefSelect select = (RefSelect)read.getAnnotation(RefSelect.class);
						
						Object[] ref = new Object[]{select,simpleProperties.get(propertyName)};
						
						refSelects.add(ref);
						
					}else { // 其他 ，根据属性名作为 列
						columns.add(new ColumnModel(simpleProperties.get(propertyName),propertyName,null));
					}
				}
			}
			
			for(Object[] objs : refSelects){
				RefSelect ref = (RefSelect)objs[0];
				SimpleProperty p = (SimpleProperty)objs[1];
				
				for(SelectModel select: selects){
					if(select.getName().equals(ref.ref())){
						if(p.getReadReturnType() == List.class || p.getReadReturnType() == Map.class || p.getReadReturnType() == Object[].class){
							throw new Exception("select ref does not support return-list,map,array type");
						}
						select.addRefSelect(ref.columnIndex(), ref.targetType(), p);
					}
				}
				
			}
			
		}
		
		
		TableModel table = new TableModel(clazz,name,alias,idModel,columns.toArray(new ColumnModel[columns.size()]),selects.toArray(new SelectModel[selects.size()]));
		
		tableMap.put(cName, table);
		
		int i = 0; // 按类名长短排序
		for(i=0;i<tableNames.size();i++){
			String[] tname = tableNames.get(i);
			if(tname[0].length() < cName.length()){
				break;
			}
		}
		tableNames.add(i,new String[]{cName,name});
		
		if(idModel == null){
			log.error("Class["+clazz.getName()+"]没有主键（Id）注解配置");
		}
	}
	
}

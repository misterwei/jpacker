package jpacker.model;

import java.lang.reflect.InvocationTargetException;

import net.sf.cglib.reflect.FastMethod;

public class SimpleProperty {
	private String name;
	private FastMethod readMethod;
	private FastMethod writeMethod;
	private String readMethodName;
	private String writeMethodName;
	private Class<?> type;
	
	public SimpleProperty(String name,Class<?> type,FastMethod readMethod,FastMethod writeMethod){
		this.name = name;
		this.readMethod = readMethod;
		this.writeMethod = writeMethod;
		this.type = type;
		this.readMethodName = readMethod.getName();
		this.writeMethodName = writeMethod.getName();
	}
	
	
	public String getName(){
		return name;
	}
	
	public Class<?> getType(){
		return type;
	}
	
	public Object invokeRead(Object obj) throws InvocationTargetException{
		return readMethod.invoke(obj,null);
	}
	
	public void invokeWrite(Object obj,Object... args) throws InvocationTargetException{
		writeMethod.invoke(obj, args);
	}
	
	public Class<?>[] getWriteParameterTypes(){
		return writeMethod.getParameterTypes();
	}
	
	public Class<?> getReadReturnType(){
		return readMethod.getReturnType();
	}
	
	public String getReadName(){
		return readMethodName;
	}
	
	public String getWriteName(){
		return writeMethodName;
	}
}

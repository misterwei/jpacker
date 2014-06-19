package jpacker.model;

import java.util.Arrays;

public class SqlParameters {
	private Object[] parameters;
	private int index = -1;
	private int length = 10;
	
	public SqlParameters(){
		parameters = new Object[length];
	}
	
	public SqlParameters(Object... parameters){
		this.parameters = parameters;
		this.length = parameters.length;
		this.index = length - 1;
	}
	
	public void add(Object obj){
		index ++;
		if(index == length){
			Object[] newparams = new Object[length+10];
			System.arraycopy(parameters, 0, newparams, 0, length);
			parameters = newparams;
			length = length + 10;
		}
		parameters[index] = obj;
	}
	
	public void add(SqlParameters params){
		if(params == null)
			return;
		Object[] r = params.getArray();
		if(r == null)
			return;
		
		for(Object obj : r){
			add(obj);
		}
	}
	
	public Object[] getArray(){
		if(index != -1){
			return Arrays.copyOfRange(parameters, 0, index+1);
		}
		return null;
	}
	
}

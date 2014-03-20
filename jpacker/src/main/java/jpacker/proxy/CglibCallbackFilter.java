package jpacker.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.CallbackFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CglibCallbackFilter implements CallbackFilter{
	static Logger log = LoggerFactory.getLogger(CglibCallbackFilter.class);
	private String[] methods;
	public CglibCallbackFilter(String[] methods){
		this.methods = methods;
	}
	
	@Override
	public int accept(Method method) {
		String name = method.getName();
		if(!name.startsWith("get")){
			return 1;
		}
		
		method.getReturnType();
		for(String m : methods){
			if(m.equals(name)){
				if(log.isDebugEnabled()){
					log.debug("the method will be used proxy. method : {}",name);
				}
				return 0;
			}
		}
		return 1;
	}

}

package jpacker.proxy;

import java.lang.reflect.Method;
import java.sql.SQLException;

import jpacker.connection.ConnectionHolder;
import jpacker.connection.ConnectionManager;
import jpacker.local.LocalExecutor;
import jpacker.local.SelectContext;
import jpacker.local.SelectListContext;
import jpacker.model.SelectModel;
import jpacker.model.SimpleProperty;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class CglibLazyProxy implements MethodInterceptor {
	
	private SelectModel[] select;
	private LocalExecutor localExecutor;
	private ConnectionManager cm;
	public CglibLazyProxy(SelectModel[] select, LocalExecutor localExecutor,ConnectionManager cm){
		this.select = select;
		this.localExecutor  = localExecutor;
		this.cm = cm;
	}
	
	@Override
	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {
		
		Object sourceResult = proxy.invokeSuper(obj, args);
		if(sourceResult != null){
			return sourceResult;
		}
		
		ConnectionHolder conn = null;
		Object result = null;
		try{
			conn = cm.getNewConnection();
			for(SelectModel s : select){
				if(s.containsMethod(method.getName())){
					Object[] parameters = null;
					
					SimpleProperty[] ps = s.getRefProperties();
					if(ps != null){
						parameters = new Object[ps.length];
						for(int i=0;i<ps.length;i++){
							parameters[i] = ps[i].invokeRead(obj);
						}
					}
					if(s.isReturnList()){
						result = localExecutor.selectList(new SelectListContext(s.getTargetType(), s.getSql(), s.getOffset(),s.getLimit(), parameters), conn);
						SimpleProperty p = s.getProperty();
						p.invokeWrite(obj,result);
					}else if(s.isHasRef()){
						Object[] propertyResult = (Object[])localExecutor.selectOne(new SelectContext(Object[].class, s.getSql(), parameters),conn);
						for(int i=0;i<propertyResult.length;i++){
							SimpleProperty p = s.getProperty(i+1);
							if(p.getReadName().equals(method.getName())){
								result = propertyResult[i];
							}
							p.invokeWrite(obj,propertyResult[i]);
						}
					}else{
						result = localExecutor.selectOne(new SelectContext(s.getTargetType(), s.getSql(), parameters),conn);
						SimpleProperty p = s.getProperty();
						p.invokeWrite(obj,result);
					}
					break;
				}
			}
		}catch(Exception e){
			throw new SQLException(e);
		}finally{
			if(conn != null)
				cm.releaseConnection(conn);
		}
		
		return result;
	}

	
}

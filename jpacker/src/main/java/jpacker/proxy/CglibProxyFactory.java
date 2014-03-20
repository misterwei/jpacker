package jpacker.proxy;

import java.util.Collection;
import java.util.HashMap;

import jpacker.connection.ConnectionManager;
import jpacker.factory.TableFactory;
import jpacker.local.LocalExecutor;
import jpacker.model.SelectModel;
import jpacker.model.TableModel;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;


public class CglibProxyFactory implements ProxyFactory{
	private HashMap<Class<?>,CglibLazyProxy> maps = new HashMap<Class<?>, CglibLazyProxy>();
	private HashMap<Class<?>,CglibCallbackFilter> filters = new HashMap<Class<?>,CglibCallbackFilter>();
	
	public CglibProxyFactory(TableFactory tf,LocalExecutor localExecutor,ConnectionManager cm){
		Collection<TableModel> tables = tf.getTableModels();
		
		for(TableModel t : tables){
			SelectModel[] models = t.getLazySelectModels();
			String[] methods = new String[models.length];
			
			for(int i=0;i<models.length;i++){
				SelectModel model = models[i];
				methods[i] = model.getProperty().getReadName();
			}
			
			maps.put(t.getTargetClass(),new CglibLazyProxy(t.getLazySelectModels(), localExecutor,cm));
			filters.put(t.getTargetClass(), new CglibCallbackFilter(methods));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProxyObject(Class<T> type) {
		Enhancer en = new Enhancer();
		en.setSuperclass(type);
		en.setCallbackFilter(filters.get(type));
		en.setCallbacks(new Callback[]{maps.get(type),NoOp.INSTANCE});
		return (T) en.create();
	}
	
}

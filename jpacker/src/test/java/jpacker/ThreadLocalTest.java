package jpacker;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import jpacker.impl.DefaultJdbcExecutor;

public class ThreadLocalTest {
	static enum CAN_COMMIT {NO,YES};
	public static void main(String[] orgs) throws InstantiationException, IllegalAccessException{
//		long start = System.currentTimeMillis();
//		for(int i=0;i<1000000;i++){
//			JdbcExecutor jdbc = DefaultJdbcExecutor.class.newInstance();
//		}
//		long end = System.currentTimeMillis();
//		System.out.println(end-start);
//		
//		ThreadLocal<Reference<JdbcExecutor>> js = new ThreadLocal<Reference<JdbcExecutor>>();
//		js.set(new SoftReference<JdbcExecutor>(DefaultJdbcExecutor.class.newInstance()));
//		
//		long start2 = System.currentTimeMillis();
//		for(int i=0;i<1000000;i++){
//			JdbcExecutor jdbc = js.get().get();
//		}
//		
//		long end2 = System.currentTimeMillis();
//		System.out.println(end2-start2);
		
		
		
		CAN_COMMIT it = CAN_COMMIT.NO;
		t(it);
		System.out.print(it);
	}
	
	public static void t(CAN_COMMIT tt){
	}
}

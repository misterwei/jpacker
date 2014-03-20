package jpacker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {
	public String sql();                     
	public int offset() default -1;        //
	public int limit()   default -1;		 //
	public String[] refProperties() default ""; //
	public boolean lazy() default false;     //
	public Class<?> targetType() default EmptyClass.class;
	public String name() default "";
	
}

package jpacker.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RefSelect {
	public Class<?> targetType() default EmptyClass.class;
	public int columnIndex() ;
	public String ref() ;
	
}

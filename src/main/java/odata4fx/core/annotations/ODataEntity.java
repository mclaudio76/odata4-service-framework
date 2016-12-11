package mclaudio76.odata4fx.core.annotations;
	
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;



@Retention(RUNTIME)
@Target(TYPE)
public @interface ODataEntity {
	public String entityName();
	public String entitySetName();
	public Class<?> controller();
}

package mclaudio76.odata4fx.core.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface ODataNavigationProperty {
	public Class   entityType();
	public String  name();
	public String  path();
	public String  target();
	public boolean nullable();
	public String  partner() default "";
	
}

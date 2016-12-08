package mclaudio76.odataspring.core.annotations;
	
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import mclaudio76.odataspring.core.IODataService;

@Retention(RUNTIME)
@Target(TYPE)
public @interface ODataEntity {
	public String entityName();
	public String entitySetName();
	public Class<? extends IODataService<?>> controller();
}

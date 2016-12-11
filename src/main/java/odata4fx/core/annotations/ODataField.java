package odata4fx.core.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;

@Retention(RUNTIME)
@Target(FIELD)
public @interface ODataField {
	public String name() default  "";
	public EdmTypeKind			ODataTypeKind();
	public EdmPrimitiveTypeKind ODataType();
	public boolean isKey() default false;
}

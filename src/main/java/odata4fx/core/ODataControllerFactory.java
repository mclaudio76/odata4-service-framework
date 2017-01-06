package odata4fx.core;

public interface ODataControllerFactory {
	Object instantiateController(Class<?> controllerClass) throws Exception;
}

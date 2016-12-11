package odata4fx.demo;


import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import odata4fx.core.Endpoint;

@Configuration
public class ApplicationConfiguration {
	
	@Bean
	public ServletRegistrationBean servletRegistrationBean(){
		Endpoint endPoint = new Endpoint("Demo");
		endPoint.addEntity(Product.class)
				.addEntity(Category.class);
		
	    return new ServletRegistrationBean(endPoint,"/ProductStore/*");
	}
	  
	
}

package mclaudio76.odataspring.demo;


import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import mclaudio76.odataspring.core.Endpoint;

@Configuration
public class ApplicationConfiguration {
	
	@Bean
	public ServletRegistrationBean servletRegistrationBean(){
		Endpoint endPoint = new Endpoint("Demo");
		endPoint.addEntity(Product.class);
	    return new ServletRegistrationBean(endPoint,"/ProductService/*");
	}
	
	
}

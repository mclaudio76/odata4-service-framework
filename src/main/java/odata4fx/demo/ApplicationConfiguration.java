package odata4fx.demo;


import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import odata4fx.core.Endpoint;
import odata4fx.core.ODataEntityHelper;
import odata4fx.demo.entities.Category;
import odata4fx.demo.entities.Product;
import odata4fx.demo.service.IProductStoreService;
import odata4fx.demo.service.ProductStoreService;
	

@Configuration
@EnableTransactionManagement
public class ApplicationConfiguration {
	
	
	@Autowired
	private Environment env;
	
	@Autowired
	private Endpoint serviceEndpoint;
	
	
	@Bean
	public DataSource getDatasource() {
        return DataSourceBuilder.create()
        	   .driverClassName(env.getProperty("spring.datasource.driver-class-name"))
        	   .url(env.getProperty("spring.datasource.url"))
        	   .username(env.getProperty("spring.datasource.username"))
        	   .password(env.getProperty("spring.datasource.password"))
        	   .build();
    }
	
	@Bean
    public JpaVendorAdapter getJPAVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(env.getProperty("spring.jpa.show-sql").trim().equalsIgnoreCase("true"));
        jpaVendorAdapter.setDatabasePlatform(env.getProperty("spring.jpa.properties.hibernate.dialect"));
        return jpaVendorAdapter;
    } 
	
	@Bean
    public PlatformTransactionManager transactionManager(){
       JpaTransactionManager transactionManager = new JpaTransactionManager();
	   transactionManager.setEntityManagerFactory(entityManagerFactory());
       return transactionManager;
   }
	 
   @Bean
   public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
      return new PersistenceExceptionTranslationPostProcessor();
   }
	 
	@Bean
    public EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setDataSource(getDatasource());
        lef.setPackagesToScan("odata4fx.demo.entities");
        lef.setPersistenceUnitName("odata-spring");
        lef.setJpaVendorAdapter(getJPAVendorAdapter());
        
        Properties properties = new Properties();
        properties.setProperty("spring.jpa.hibernate.ddl-auto",env.getProperty("spring.jpa.hibernate.ddl-auto"));
        lef.setJpaProperties(properties);
        lef.afterPropertiesSet();
        return lef.getObject();
    }    
	
	
	@Bean
	public Endpoint getProductStoreServiceEndpoint() {
		Endpoint endPoint = new Endpoint("Demo");
		endPoint.addEntity(Product.class)
				.addEntity(Category.class);
		return endPoint;
	}
	
	@Bean
	public IProductStoreService getProductStoreService() {
		return new ProductStoreService();
	}
	
	@Bean
	public ODataEntityHelper getODataEntityHelper() {
		return new ODataEntityHelper();
	}
	
	
	@Bean
	public ServletRegistrationBean servletRegistrationBean(){
	    return new ServletRegistrationBean(serviceEndpoint,"/ProductStore/*");
	}
	
	
}

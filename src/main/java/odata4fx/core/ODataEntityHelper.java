package odata4fx.core;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Service;

import odata4fx.core.annotations.ODataController;
import odata4fx.core.annotations.ODataEntity;
import odata4fx.core.annotations.ODataField;
import odata4fx.core.annotations.ODataNavigationProperty;

//@Service
public class ODataEntityHelper {
	
	private static Locale locale = Locale.ENGLISH;
	private ODataControllerFactory controllerFactory = null;
	
	//private ApplicationContext context;
	
	
	/*@Autowired
	public void setApplicationContext(ApplicationContext ctx) {
		this.context = ctx;
	}*/
	
	public ODataEntityHelper(ODataControllerFactory controllerFactory) {
		this.controllerFactory = controllerFactory;
	}
	
	
	public ODataEntityHelper() {
		this(null);
	}
	
	
	public String getEntityName(Class<?> clz) throws ODataApplicationException {
		if(clz.isAnnotationPresent(ODataEntity.class)) {
			ODataEntity metaData = (ODataEntity) clz.getAnnotation(ODataEntity.class);
			return metaData.entityName();
		}
		else {
			throw new ODataApplicationException("Object isn't annotated with @ODataEntity ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	public String getEntitySetName(Class<?> clz) throws ODataApplicationException {
		if(clz.isAnnotationPresent(ODataEntity.class)) {
			ODataEntity metaData = (ODataEntity) clz.getAnnotation(ODataEntity.class);
			return metaData.entitySetName();
		}
		else {
			throw new ODataApplicationException("Object isn't annotated with @ODataEntity ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	
	public Class<?> getControllerClass(Class<?> entityClz) throws ODataApplicationException {
		try {
			if(entityClz.isAnnotationPresent(ODataEntity.class)) {
				ODataEntity metaData  = (ODataEntity) entityClz.getAnnotation(ODataEntity.class);
				Class<?>controllerClass = metaData.controller();
				if(!controllerClass.isAnnotationPresent(ODataController.class)) {
					throw new ODataApplicationException("Class ["+controllerClass+"] is not annotated with @ODataController",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
				}
				return controllerClass;
			}
			else {
				throw new ODataApplicationException("Object isn't annotated with @ODataEntity ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
			}
		}
		catch(Exception e) {
			throw new ODataApplicationException("Unable to instantiate a controller for class ["+entityClz.getName()+"], reason is = "+e.getMessage(),HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	public Object getController(Class<?> clz) throws ODataApplicationException {
		try {
			Class controllerClass = getControllerClass(clz);
			if(controllerFactory == null) {
				return controllerClass.newInstance();
			}
			else  {
				return controllerFactory.instantiateController(controllerClass);
			}
		}
		catch(Exception e) {
			throw new ODataApplicationException("Unable to instantiate a controller for class ["+clz.getName()+"], reason is = "+e.getMessage(),HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	
	
	public List<CsdlProperty> getClassAttributes(Class<?> entity) throws ODataApplicationException {
		if(entity.isAnnotationPresent(ODataEntity.class)) {
			ArrayList<CsdlProperty> properties = new ArrayList<>();
			for(Field field : entity.getDeclaredFields()) {
				if(field.isAnnotationPresent(ODataField.class)) {
				   ODataField annotation = (ODataField) field.getAnnotation(ODataField.class);
				     CsdlProperty currentProperty = buildProperty(field, annotation);
					   if(currentProperty != null) {
						   properties.add(currentProperty);
					   }
				  }
			}
			return properties;
		}
		else {
			throw new ODataApplicationException("Object isn't annotated with @ODataEntity ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	public List<CsdlNavigationProperty> getNavigationProperties(Class<?> entity,String nameSpace) throws ODataApplicationException {
		if(entity.isAnnotationPresent(ODataEntity.class)) {
			ArrayList<CsdlNavigationProperty> properties = new ArrayList<>();
			for(Field field : entity.getDeclaredFields()) {
				if(field.isAnnotationPresent(ODataNavigationProperty.class)) {
					ODataNavigationProperty annotation 		= (ODataNavigationProperty) field.getAnnotation(ODataNavigationProperty.class);
					CsdlNavigationProperty currentProperty  = buildNavigationProperty(nameSpace,field, annotation);
					   if(currentProperty != null) {
						   properties.add(currentProperty);
					   }
				  }
			}
			return properties;
		}
		else {
			throw new ODataApplicationException("Object isn't annotated with @ODataEntity ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	public List<CsdlNavigationPropertyBinding> getNavigationPropertiesForEntitySet(Class<?> entity) throws ODataApplicationException {
		if(entity.isAnnotationPresent(ODataEntity.class)) {
			ArrayList<CsdlNavigationPropertyBinding> properties = new ArrayList<>();
			for(Field field : entity.getDeclaredFields()) {
				if(field.isAnnotationPresent(ODataNavigationProperty.class)) {
					ODataNavigationProperty annotation 		= (ODataNavigationProperty) field.getAnnotation(ODataNavigationProperty.class);
					CsdlNavigationPropertyBinding currentProperty  = buildNavigationPropertyBinding(field, annotation);
					   if(currentProperty != null) {
						   properties.add(currentProperty);
					   }
				  }
			}
			return properties;
		}
		else {
			throw new ODataApplicationException("Object isn't annotated with @ODataEntity ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	
	private CsdlNavigationProperty buildNavigationProperty(String nameSpace,Field field, ODataNavigationProperty annotation) {
		CsdlNavigationProperty prop = new CsdlNavigationProperty();
		// Load annotation about class category
		prop.setName(annotation.name());
		if(!annotation.partner().trim().isEmpty()) {
			prop.setPartner(annotation.partner());
		}
		prop.setNullable(annotation.nullable());
		// If field represents a collection..
		if(List.class.isAssignableFrom(field.getType())) {
			prop.setCollection(true);
		}
		
		Class relatedObjectClass = annotation.entityType();
		ODataEntity relatedEntityAnn = (ODataEntity) relatedObjectClass.getAnnotation(ODataEntity.class);
		prop.setType(new FullQualifiedName(nameSpace, relatedEntityAnn.entityName()));
		return prop;
	}
	
	private CsdlNavigationPropertyBinding buildNavigationPropertyBinding(Field field, ODataNavigationProperty annotation) {
		CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
		navPropBinding.setPath(annotation.name()); // the path from entity type to navigation property
		navPropBinding.setTarget(annotation.target()); //target entitySet, where the nav prop points to
		return navPropBinding;
	}

	public List<CsdlPropertyRef> getClassKeys(Class<?> entity) throws ODataApplicationException {
		if(entity.isAnnotationPresent(ODataEntity.class)) {
			ArrayList<CsdlPropertyRef> properties = new ArrayList<>();
			for(Field field : entity.getDeclaredFields()) {
				if(field.isAnnotationPresent(ODataField.class)) {
				   ODataField annotation = (ODataField) field.getAnnotation(ODataField.class);
				   if(annotation.isKey()) {
					  CsdlPropertyRef currentProperty = buildPropertyKey(field, annotation);
					  if(currentProperty != null) {
						 properties.add(currentProperty);
					  }
				  }
				}
			}
			return properties;
		}
		else {
			throw new ODataApplicationException("Object isn't annotated with @ODataEntity ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}

	private CsdlProperty buildProperty(Field field, ODataField annotation) {
		String name 				= annotation.name();
		if(name == null || name.trim().isEmpty()) {
			name = field.getName();
		}
		CsdlProperty property = new CsdlProperty().setName(name).setType(annotation.ODataType().getFullQualifiedName());
		return property;
	}
	
	private CsdlPropertyRef buildPropertyKey(Field field, ODataField annotation) {
		String name 				= annotation.name();
		if(name == null || name.trim().isEmpty()) {
			name = field.getName();
		}
		CsdlPropertyRef property = new CsdlPropertyRef().setName(name);
		return property;
	}
	
	public Entity buildEntity(Object entity) throws ODataApplicationException {
		if(entity == null) {
			   throw new ODataApplicationException("Cannot handle a null entity. ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
		Class<?> entityClass = entity.getClass();
		if(entityClass.isAnnotationPresent(ODataEntity.class)) {
			ODataEntity entityAnnotation = (ODataEntity) entity.getClass().getAnnotation(ODataEntity.class);
			Entity builtEntity = new Entity();
			StringBuffer uniqueKeyUri = new StringBuffer(entityAnnotation.entityName());
			for(Field field : entityClass.getDeclaredFields()) {
				if(field.isAnnotationPresent(ODataField.class)) {
				   ODataField annotation = (ODataField) field.getAnnotation(ODataField.class);
				   String     mappedFieldName = annotation.name() == null || annotation.name().trim().isEmpty() ? field.getName() : annotation.name().trim();
				   if(annotation.isKey()) {
					   try {
						   uniqueKeyUri.append("(").append(mappedFieldName).append("=").append(String.valueOf(field.get(entity))).append(")");
					   }
					   catch(Exception e) {
						   throw new ODataApplicationException("Failed to create an unique URI for ID of entity "+entity.getClass().getName(),HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
					   }
					   
				   }
				   Property prop = buildProperty(entity,field, annotation);
				   builtEntity.addProperty(prop);
				}
			}
			try {
				builtEntity.setId(new URI(uniqueKeyUri.toString()));
			}
			catch(Exception e) {
				throw new ODataApplicationException("Object "+entity.getClass().getName()+" has unvalid ID uri ",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
			}
			return builtEntity;
		}
		else {
			throw new ODataApplicationException("Object is not annotated with @ODataEntity",HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	private Property buildProperty(Object obj, Field f, ODataField annotation) throws ODataApplicationException {
		try {
			Property p = new Property();
			p.setName(annotation.name().isEmpty() ? f.getName() : annotation.name());
			p.setType(null);
			Object value = f.get(obj);
			switch (annotation.ODataTypeKind()) {
				case PRIMITIVE : p.setValue(ValueType.PRIMITIVE, value); break;
				default: break;
			}
			return p;
		}
		catch(IllegalAccessException iae) {
			throw new ODataApplicationException("Illegal access of field "+f.getName()+" --> "+iae.toString(),HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),locale);
		}
	}
	
	
	
	public void setFieldsValueFromEntity(Object object, List<ODataParameter> attributes) {
		for(ODataParameter paramValue : attributes) {
			try {
				object.getClass().getDeclaredField(paramValue.propertyName).set(object, paramValue.value);
			}
			catch(Exception e) {
				System.err.println(" Unable to set value ["+paramValue.propertyName+", "+paramValue.value+"]; error is "+e.getMessage());
			}
		}
	}
	
	
}

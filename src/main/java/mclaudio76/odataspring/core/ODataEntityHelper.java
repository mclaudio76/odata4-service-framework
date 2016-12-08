package mclaudio76.odataspring.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.ex.ODataException;

import mclaudio76.odataspring.core.annotations.ODataController;
import mclaudio76.odataspring.core.annotations.ODataEntity;
import mclaudio76.odataspring.core.annotations.ODataField;
import mclaudio76.odataspring.core.annotations.ODataNavigationProperty;
	
public class ODataEntityHelper {
	
	public String getEntityName(Class<?> clz) throws ODataException {
		if(clz.isAnnotationPresent(ODataEntity.class)) {
			ODataEntity metaData = (ODataEntity) clz.getAnnotation(ODataEntity.class);
			return metaData.entityName();
		}
		else {
			throw new ODataException("Object isn't annotated with @ODataEntity ");
		}
	}
	
	public String getEntitySetName(Class<?> clz) throws ODataException {
		if(clz.isAnnotationPresent(ODataEntity.class)) {
			ODataEntity metaData = (ODataEntity) clz.getAnnotation(ODataEntity.class);
			return metaData.entitySetName();
		}
		else {
			throw new ODataException("Object isn't annotated with @ODataEntity ");
		}
	}
	
	public Object getController(Class<?> clz) throws ODataException {
		try {
			if(clz.isAnnotationPresent(ODataEntity.class)) {
				ODataEntity metaData  = (ODataEntity) clz.getAnnotation(ODataEntity.class);
				Class controllerClass = metaData.controller();
				if(!controllerClass.isAnnotationPresent(ODataController.class)) {
					throw new ODataException(" Class "+controllerClass.getCanonicalName()+" isn't annotated with @ODataController ");
				}
				return  metaData.controller().newInstance();
			}
			else {
				throw new ODataException("Object isn't annotated with @ODataEntity ");
			}
		}
		catch(InstantiationException | IllegalAccessException  | ODataException oe) {
			throw new ODataException("Unable to instantiate a controller.");
		}
	}
	
	
	
	public List<CsdlProperty> getClassAttributes(Class<?> entity) throws ODataException {
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
			throw new ODataException("Object isn't annotated with @ODataEntity ");
		}
	}
	
	public List<CsdlNavigationProperty> getNavigationProperties(Class<?> entity,String nameSpace) throws ODataException {
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
			throw new ODataException("Object isn't annotated with @ODataEntity ");
		}
	}
	
	public List<CsdlNavigationPropertyBinding> getNavigationPropertiesForEntitySet(Class<?> entity) throws ODataException {
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
			throw new ODataException("Object isn't annotated with @ODataEntity ");
		}
	}
	
	
	private CsdlNavigationProperty buildNavigationProperty(String nameSpace,Field field, ODataNavigationProperty annotation) {
		CsdlNavigationProperty prop = new CsdlNavigationProperty();
		// Load annotation about class category
		prop.setName(annotation.entityName());
		if(!annotation.partner().trim().isEmpty()) {
			prop.setPartner(annotation.partner());
		}
		prop.setNullable(annotation.nullable());
		Class relatedObjectClass = annotation.entityType();
		ODataEntity relatedEntityAnn = (ODataEntity) relatedObjectClass.getAnnotation(ODataEntity.class);
		prop.setType(new FullQualifiedName(nameSpace, relatedEntityAnn.entityName()));
		return prop;
	}
	
	private CsdlNavigationPropertyBinding buildNavigationPropertyBinding(Field field, ODataNavigationProperty annotation) {
		CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
		// Load annotation about class category
		navPropBinding.setPath(annotation.entityName()); // the path from entity type to navigation property
		navPropBinding.setTarget(annotation.entitySetName()); //target entitySet, where the nav prop points to
		return navPropBinding;
	}

	public List<CsdlPropertyRef> getClassKeys(Class<?> entity) throws ODataException {
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
			throw new ODataException("Object isn't annotated with @ODataEntity ");
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
	
	public Entity buildEntity(Object entity) throws ODataException {
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
						   throw new ODataException("Failed to create unique uri for ID "+e.getMessage());
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
				throw new ODataException("Object has unvalid URI ID");
			}
			return builtEntity;
		}
		else {
			throw new ODataException("Object isn't annotated with @ODataEntity ");
		}
	}
	
	private Property buildProperty(Object obj, Field f, ODataField annotation) throws ODataException {
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
			throw new ODataException("Illegal access of field "+f.getName()+" --> "+iae.toString());
		}
	}
	
	
	
	public void setFieldsValueFromEntity(Object object, ODataParamValue ... attributes) {
		for(ODataParamValue paramValue : attributes) {
			try {
				object.getClass().getDeclaredField(paramValue.propertyName).set(object, paramValue.value);
			}
			catch(Exception e) {
				System.err.println(" Unable to set value ["+paramValue.propertyName+", "+paramValue.value+"]; error is "+e.getMessage());
			}
		}
	}
	
	public boolean entityMatchesKeys(Object object, ODataParamValue ... attributes) {
		boolean matches = true;
		for(ODataParamValue paramValue : attributes) {
			try {
				Object keyValue    = object.getClass().getDeclaredField(paramValue.propertyName).get(object);
				Constructor<?> constr = keyValue.getClass().getConstructor(String.class);
				if(constr == null) {
					matches &= keyValue.toString().equals(paramValue.value);	
				}
				else {
					Object pValue = constr.newInstance(paramValue.value);
					matches &= keyValue.equals(pValue);
				}
			}
			catch(Exception e) {
				System.err.println(" Unable to inspect value ["+paramValue.propertyName+", "+paramValue.value+"]; error is "+e.getMessage());
			}
		}
		return matches;
	}
	
	
}

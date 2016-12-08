package mclaudio76.odataspring.core;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import mclaudio76.odataspring.core.annotations.ODataCreateEntity;
import mclaudio76.odataspring.core.annotations.ODataDeleteEntity;
import mclaudio76.odataspring.core.annotations.ODataReadEntity;
import mclaudio76.odataspring.core.annotations.ODataReadEntityCollection;
import mclaudio76.odataspring.core.annotations.ODataUpdateEntity;

/****
 * 
 * Note: the first segment of the service urls corresponds to entity set.
 *
 * 
 */

public class ODataServiceHandler implements EntityCollectionProcessor, EntityProcessor {
	private OData 				initODataItem;
	private ServiceMetadata 	initServiceMetaData;
	private ODataEntityHelper 	oDataHelper;
	private GenericEDMProvider  edmProvider;
	
	public ODataServiceHandler(GenericEDMProvider provider) {
		this.edmProvider = provider;
	}
	
	@Override
	public void init(OData odata, ServiceMetadata metadata) {
		this.initODataItem = odata;
		this.initServiceMetaData = metadata;
		this.oDataHelper = new ODataEntityHelper();
	}
	
	
	@Override
	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType contentType)	throws ODataApplicationException, ODataLibraryException {
	  try {
		  EdmEntitySet edmEntitySet  = getEdmEntitySet(uriInfo);
		  Object businessService 	 = instatiateDataService(edmEntitySet.getEntityType());
		  Class  workEntityClass     = edmProvider.findActualClass(edmEntitySet.getEntityType().getFullQualifiedName());
		  EntityCollection entitySet = new EntityCollection();
		  try {
			  // Search for a method annotated with @ODataReadEntityCollections
			  Collection<Object> entityList = (Collection<Object>) invokeMethod(businessService,workEntityClass, ODataReadEntityCollection.class, null);
			  for(Object localEntity : entityList) {
				  entitySet.getEntities().add(oDataHelper.buildEntity(localEntity));  
			  }
		  }
		  catch(Exception e) {
			  e.printStackTrace(System.err);
		  }
		  serializeCollection(request, response, contentType, edmEntitySet, entitySet);
	  }
	  catch(Exception e) {
		  sendError(response, contentType);
	  }
	}

	
	

	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,  ContentType requestFormat, ContentType responseFormat)  throws  ODataApplicationException, DeserializerException, SerializerException {
		EdmEntitySet edmEntitySet       = getEdmEntitySet(uriInfo);
		EdmEntityType edmEntityType     = edmEntitySet.getEntityType();
		Class  workEntityClass     		= edmProvider.findActualClass(edmEntitySet.getEntityType().getFullQualifiedName());
		Object businessService 			= instatiateDataService(edmEntitySet.getEntityType());
		InputStream requestInputStream  = request.getBody();
		ODataDeserializer deserializer  = initODataItem.createDeserializer(requestFormat);
		DeserializerResult result 	    = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity 		    = result.getEntity();
		List<ODataParamValue> attributes = new ArrayList<>();
		for(Property prop : requestEntity.getProperties()) {
		  attributes.add(new ODataParamValue(prop));
		}
		try {
		  Object newCreatedEntity   = invokeMethod(businessService, workEntityClass, ODataCreateEntity.class,attributes.toArray(new ODataParamValue[attributes.size()]));
		  sendEntity(response, responseFormat, edmEntitySet, edmEntityType, newCreatedEntity);
		}
		catch(Exception e) {
		  sendError(response, responseFormat);
		}
	}

	
	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)	throws ODataApplicationException, ODataLibraryException {
  	   ODataParamValue[] keys	     = getKeyPredicates(uriInfo);
  	   EdmEntitySet edmEntitySet     = getEdmEntitySet(uriInfo);
	   Object businessService 		 = instatiateDataService(edmEntitySet.getEntityType());
	   Class  workEntityClass     	 = edmProvider.findActualClass(edmEntitySet.getEntityType().getFullQualifiedName());
	   try {
			invokeMethod(businessService, workEntityClass, ODataDeleteEntity.class,keys);
			response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	   }
	   catch(Exception e) {
		  sendError(response, ContentType.APPLICATION_JSON);
		}
	   
	   
	}

	

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
	    EdmEntitySet edmEntitySet = getEdmEntitySet(uriInfo);
	    Object businessService    = instatiateDataService(edmEntitySet.getEntityType());
	    ODataParamValue params[]  = getKeyPredicates(uriInfo);
	    Class  workEntityClass    = edmProvider.findActualClass(edmEntitySet.getEntityType().getFullQualifiedName());
	    try {
	    	Object readEntity  =  invokeMethod(businessService, workEntityClass, ODataReadEntity.class,params);//businessService.findByKey(params);   
		    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		    sendEntity(response, responseFormat, edmEntitySet, edmEntityType, readEntity);
	    }
	    catch(Exception e) {
	    	sendError(response, responseFormat);
	    }
		
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)	throws ODataApplicationException, ODataLibraryException {
		 // 1. Retrieve the entity type from the URI
		  EdmEntitySet edmEntitySet     = getEdmEntitySet(uriInfo);
		  EdmEntityType edmEntityType   = edmEntitySet.getEntityType();
		  Class  workEntityClass    	= edmProvider.findActualClass(edmEntitySet.getEntityType().getFullQualifiedName());
		  Object businessService 	    = instatiateDataService(edmEntitySet.getEntityType());
		  // 2.1. retrieve the payload from the POST request for the entity to create and deserialize it
		  InputStream requestInputStream = request.getBody();
		  ODataDeserializer deserializer = initODataItem.createDeserializer(requestFormat);
		  DeserializerResult result 	 = deserializer.entity(requestInputStream, edmEntityType);
		  Entity requestEntity 			 = result.getEntity();
		  List<ODataParamValue> attributes = new ArrayList<>();
		  for(Property prop : requestEntity.getProperties()) {
			  attributes.add(new ODataParamValue(prop));
		  }
		  try {
			  Object target	= invokeMethod(businessService, workEntityClass, ODataUpdateEntity.class,attributes.toArray(new ODataParamValue[attributes.size()]));//businessService.findByKey(params);   //businessService.update(attributes.toArray(new ODataParamValue[attributes.size()]));
			  sendEntity(response, responseFormat, edmEntitySet, edmEntityType, target);
		  }
		  catch(Exception e) {
	    	 sendError(response, responseFormat);
		  }
		
	}
	
	/// Helper methods
	
	private void sendEntity(ODataResponse response, ContentType responseFormat, EdmEntitySet edmEntitySet,  EdmEntityType edmEntityType, Object object) throws SerializerException, ODataException {

		  Entity actualODataEntity  = oDataHelper.buildEntity(object);
		  ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

		  EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
		  ODataSerializer serializer = initODataItem.createSerializer(responseFormat);
		  SerializerResult serializedResponse = serializer.entity(initServiceMetaData, edmEntityType, actualODataEntity, options);

		  response.setContent(serializedResponse.getContent());
		  response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		  response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	
	private Object instatiateDataService(EdmEntityType entityType) throws ODataApplicationException {
		try {
			Class clz = edmProvider.findActualClass(entityType.getFullQualifiedName());
			return oDataHelper.getController(clz);
		}
		catch(ODataException exp) {
			raiseODataApplicationException(exp.getMessage(), HttpStatusCode.NOT_ACCEPTABLE);
			return null;
		}
	}

	
	
	private void raiseODataApplicationException(String message, HttpStatusCode statusCode) throws ODataApplicationException {
		 throw new ODataApplicationException(message, statusCode.getStatusCode(),Locale.ENGLISH);
	}

	
	private EdmEntitySet getEdmEntitySet(UriInfoResource uriInfo) throws ODataApplicationException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Invalid resource type for first segment.",
                      HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),Locale.ENGLISH);
        }
        UriResourceEntitySet uriResource = (UriResourceEntitySet) resourcePaths.get(0);
        return uriResource.getEntitySet();
    }

	
	
	private ODataParamValue[] getKeyPredicates(UriInfo uriInfo) {
		UriResourceEntitySet uriResourceEntitySet 	= (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
	    List<UriParameter> keyPredicates 			= uriResourceEntitySet.getKeyPredicates();
	    List<ODataParamValue> keys = new ArrayList<>();
	    for(UriParameter uri : keyPredicates)  {
	    	keys.add(new ODataParamValue(uri));
	    }
	    return keys.toArray(new ODataParamValue[keys.size()]);
	}
	
	
	private void sendError(ODataResponse response, ContentType contentType) {
		response.setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
	}

	private void sendData(ODataResponse response, ContentType contentType) {
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
	}

	
	private void serializeCollection(ODataRequest request, ODataResponse response, ContentType contentType, EdmEntitySet edmEntitySet, EntityCollection entitySet) throws SerializerException {
		  ODataSerializer serializer = initODataItem.createSerializer(contentType);
		  EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		  ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		  final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		  EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl).build();
		  SerializerResult serializerResult = serializer.entityCollection(initServiceMetaData, edmEntityType, entitySet, opts);
		  InputStream serializedContent = serializerResult.getContent();
		  response.setContent(serializedContent);
		  sendData(response, contentType);
	}
	

	/// Reflection support for invoking methods.
	/// Verifies if, given a certain annotation, a method with such annotation exists and accepts expected parameters
	private Object invokeMethod(Object businessService, Class<?> workEntityClass, Class<? extends Annotation> annotation, ODataParamValue[] params) throws ODataException {
		Method targetMethod = null;
		for(Method method : businessService.getClass().getDeclaredMethods()) {
			Class[] mParams  = method.getParameterTypes();
			// Target method must be annotated with required annotation
			boolean annotationPresent = method.isAnnotationPresent(annotation);
			if(annotationPresent) {
				boolean matches  		  = annotationPresent; 
				// Reading collections of entities
				if(annotation.equals(ODataReadEntityCollection.class)) {
					ODataReadEntityCollection actualAnnotation = (ODataReadEntityCollection) method.getAnnotation(annotation); 
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1; // Only one parameter
					matches			&= mParams[0].equals(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= method.getReturnType().isInstance(Collection.class); // Must return a Collection
					if(matches) {
						targetMethod = method;
						break;
					}
				}
				else // Read of an entity
				if(annotation.equals(ODataReadEntity.class)) {
					ODataReadEntity actualAnnotation = (ODataReadEntity) method.getAnnotation(annotation);
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1; // Only one parameter
					matches			&= mParams[0].equals(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= method.getReturnType().isInstance(workEntityClass); // Must return an object
					if(matches) {
						targetMethod = method;
						break;
					}
				}
				else // Creation of an Entity
				if(annotation.equals(ODataCreateEntity.class)) {
					ODataCreateEntity actualAnnotation = (ODataCreateEntity) method.getAnnotation(annotation);
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1; // Only one parameter
					matches			&= mParams[0].equals(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= method.getReturnType().isInstance(workEntityClass); // Must return an object
					if(matches) {
						targetMethod = method;
						break;
					}
				}
				else // Deletion of an Entity
				if(annotation.equals(ODataDeleteEntity.class)) {
					ODataDeleteEntity actualAnnotation = (ODataDeleteEntity) method.getAnnotation(annotation);
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1; // Only one parameter
					matches			&= mParams[0].equals(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class); // must return nothing
					if(matches) {
						targetMethod = method;
						break;
					}
				}
				else // Update of an entity
				if(annotation.equals(ODataUpdateEntity.class)) {
					ODataUpdateEntity actualAnnotation = (ODataUpdateEntity) method.getAnnotation(annotation);
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1; // Only one parameter
					matches			&= mParams[0].equals(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= method.getReturnType().isInstance(workEntityClass); // Must return an object
					if(matches) {
						targetMethod = method;
						break;
					}
				}
			}
		}
		if(targetMethod == null) {
			throw new ODataException("No suitable method found ");
		}
		try {
			return targetMethod.invoke(businessService, (Object[])params);
		}
		catch(Exception e) {
			throw new ODataException("An error occurred while invoking method "+targetMethod.getName()+" on class "+businessService.getClass().getName());
		}
	}
	

}

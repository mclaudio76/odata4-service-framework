package mclaudio76.odataspring.core;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
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
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceProperty;

/****
 * 
 * Note: the first segment of the service urls corresponds to entity set.
 * 
 * 
 */

public class AbstractODataService<T> implements EntityCollectionProcessor, EntityProcessor, PrimitiveProcessor {
	private OData 				initODataItem;
	private ServiceMetadata 	initServiceMetaData;
	private ODataEntityHelper 	oDataHelper;
	private IODataService<T> 	businessService;
	
	public AbstractODataService(IODataService<T> businessService) {
		this.businessService = businessService;
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
			  List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
			  UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0); 
			  EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
			  EntityCollection entitySet = new EntityCollection();
			  try {
				  for(T localEntity : businessService.listAll()) {
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
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,  ContentType requestFormat, ContentType responseFormat)  throws ODataApplicationException, DeserializerException, SerializerException {
		EdmEntitySet edmEntitySet   = getEdmEntitySet(uriInfo);
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		InputStream requestInputStream = request.getBody();
		ODataDeserializer deserializer = initODataItem.createDeserializer(requestFormat);
		DeserializerResult result 	 = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity 			 = result.getEntity();
		List<ODataParamValue> attributes = new ArrayList<>();
		for(Property prop : requestEntity.getProperties()) {
		  attributes.add(new ODataParamValue(prop));
		}
		try {
		  T newCreatedEntity   = businessService.create(attributes.toArray(new ODataParamValue[attributes.size()]));
		  sendEntity(response, responseFormat, edmEntitySet, edmEntityType, newCreatedEntity);
		}
		catch(Exception e) {
		  sendError(response, responseFormat);
		}
	}

	

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)	throws ODataApplicationException, ODataLibraryException {
  	   ODataParamValue[] keys	    = getKeyPredicates(uriInfo);
  	   businessService.delete(keys);
	   response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
	    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    ODataParamValue params[]  = getKeyPredicates(uriInfo);
	    try {
	    	T readEntity  = businessService.findByKey(params);   
		    EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		    sendEntity(response, responseFormat, edmEntitySet, edmEntityType, readEntity);
	    }
	    catch(Exception e) {
	    	sendError(response, responseFormat);
	    }
		
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)	throws ODataApplicationException, ODataLibraryException {
		  EdmEntitySet edmEntitySet      = getEdmEntitySet(uriInfo);
		  ODataParamValue keyParams[]    = getKeyPredicates(uriInfo);
		  EdmEntityType edmEntityType    = edmEntitySet.getEntityType();
		  InputStream requestInputStream = request.getBody();
		  ODataDeserializer deserializer = initODataItem.createDeserializer(requestFormat);
		  DeserializerResult result 	 = deserializer.entity(requestInputStream, edmEntityType);
		  Entity requestEntity 			 = result.getEntity();
		  List<ODataParamValue> attributes = new ArrayList<>();
		  for(Property prop : requestEntity.getProperties()) {
			  attributes.add(new ODataParamValue(prop));
		  }
		  try {
			  T target	= businessService.findByKey(keyParams);
			  if(target != null) {
				 businessService.update(target, attributes.toArray(new ODataParamValue[attributes.size()]));
			  }
			  sendEntity(response, responseFormat, edmEntitySet, edmEntityType, target);
		  }
		  catch(Exception e) {
	    	 sendError(response, responseFormat);
		  }
		
	}
	
	
	
	/**********
	 * Primitive processor.
	 * 
	 */
	
	@Override
	public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException, SerializerException {
		List<UriResource> resourcePaths 				= uriInfo.getUriResourceParts();
	    UriResourceEntitySet uriResourceEntitySet 		= (UriResourceEntitySet) resourcePaths.get(0);
	    UriResourcePrimitiveProperty requestedProperty 	= findRequestedProperty(resourcePaths);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    ODataParamValue params[]  = getKeyPredicates(uriInfo);
	    try {
	    	if(requestedProperty != null) {
	    		T readEntity  				= businessService.findByKey(params);  
	    		EdmProperty	edmProperty	    = requestedProperty.getProperty();
	    		if(readEntity == null) {
	    			throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    		}
	    		else {
	    			Entity oDataEntity 			= oDataHelper.buildEntity(readEntity);
	    			Property property 			= oDataEntity.getProperty(edmProperty.getName());
	    			if (property == null) {
	    				throw new ODataApplicationException("Property not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
	    			}
	    			serializeProperty(property, edmEntitySet, edmProperty, response, responseFormat);
	    		}
	    	}
	    }
	    catch(Exception e) {
	    	if(e instanceof ODataApplicationException) {
	    		throw (ODataApplicationException)e;
	    	}
	    	if(e instanceof ODataLibraryException) {
	    		throw (ODataApplicationException) e;
	    	}
	    	throw new ODataApplicationException(e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
	    }
	}

	

	
	@Override
	public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo,	ContentType requestFormat, ContentType responseFormat)	throws ODataApplicationException, ODataLibraryException {
		
	}

	@Override
	public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo)	throws ODataApplicationException, ODataLibraryException {

	}
	

	private UriResourcePrimitiveProperty findRequestedProperty(List<UriResource> resourcePaths) {
		for(UriResource currentPart : resourcePaths) {
			if(currentPart.getKind() == UriResourceKind.primitiveProperty) {
				return (UriResourcePrimitiveProperty)currentPart;
			}
		}
		return null;
	}

	/***
	 * Helper methods
	 * 
	 */
	
	private void sendEntity(ODataResponse response, ContentType responseFormat, EdmEntitySet edmEntitySet,  EdmEntityType edmEntityType, T object) throws SerializerException, ODataException {
		  Entity actualODataEntity  = oDataHelper.buildEntity(object);
		  ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		  EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
		  ODataSerializer serializer = initODataItem.createSerializer(responseFormat);
		  SerializerResult serializedResponse = serializer.entity(initServiceMetaData, edmEntityType, actualODataEntity, options);
		  response.setContent(serializedResponse.getContent());
		  response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		  response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
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

	private void serializeCollection(ODataRequest request, ODataResponse response, ContentType contentType, EdmEntitySet edmEntitySet, EntityCollection entitySet) throws SerializerException {
		  ODataSerializer serializer = initODataItem.createSerializer(contentType);
		  EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		  ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		  final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
		  EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl).build();
		  SerializerResult serializerResult = serializer.entityCollection(initServiceMetaData, edmEntityType, entitySet, opts);
		  InputStream serializedContent = serializerResult.getContent();
		  response.setContent(serializedContent);
		  sendError(response, contentType);
	}

	
	private void serializeProperty(Property property, EdmEntitySet edmEntitySet, EdmProperty edmProperty, ODataResponse response, ContentType responseFormat) throws SerializerException {
		if(property.getValue() != null) {
			ODataSerializer serializer = initODataItem.createSerializer(responseFormat);
	        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).navOrPropertyPath(property.getName()).build();
	        PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
	        SerializerResult serializerResult = serializer.primitive(initServiceMetaData, (EdmPrimitiveType) edmProperty.getType(), property, options);
	        InputStream propertyStream = serializerResult.getContent();
	        response.setContent(propertyStream);
	        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
		}
		else {
			 response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
		}
	}

	
}

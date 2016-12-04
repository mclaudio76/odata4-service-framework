package mclaudio76.odataspring.core;

import java.io.InputStream;
import java.util.ArrayList;
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

/****
 * 
 * Note: the first segment of the service urls corresponds to entity set.
 *
 * 
 */

public class AbstractODataService<T> implements EntityCollectionProcessor, EntityProcessor {
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

	private void sendEntity(ODataResponse response, ContentType responseFormat, EdmEntitySet edmEntitySet,  EdmEntityType edmEntityType, T object) throws SerializerException, ODataException {
		   // 3. serialize the response (we have to return the created entity)
		  Entity actualODataEntity  = oDataHelper.buildEntity(object);
		  ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		  //expand and select currently not supported
		  EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
		  ODataSerializer serializer = initODataItem.createSerializer(responseFormat);
		  SerializerResult serializedResponse = serializer.entity(initServiceMetaData, edmEntityType, actualODataEntity, options);
		  //4. configure the response object
		  response.setContent(serializedResponse.getContent());
		  response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		  response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)	throws ODataApplicationException, ODataLibraryException {
  	   ODataParamValue[] keys	    = getKeyPredicates(uriInfo);
  	   businessService.delete(keys);
	   response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		// 1. retrieve the Entity Type
	    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
	    // Note: only in our example we can assume that the first segment is the EntitySet
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
		 // 1. Retrieve the entity type from the URI
		  EdmEntitySet edmEntitySet   = getEdmEntitySet(uriInfo);
		  EdmEntityType edmEntityType = edmEntitySet.getEntityType();
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
			  T target	= businessService.update(attributes.toArray(new ODataParamValue[attributes.size()]));
			  sendEntity(response, responseFormat, edmEntitySet, edmEntityType, target);
		  }
		  catch(Exception e) {
	    	 sendError(response, responseFormat);
		  }
		
	}
	
	/// Helper methods
	
	private EdmEntitySet getEdmEntitySet(UriInfoResource uriInfo) throws ODataApplicationException {

        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
         // To get the entity set we have to interpret all URI segments
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
	

	

}

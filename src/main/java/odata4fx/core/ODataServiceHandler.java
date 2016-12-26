package odata4fx.core;

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
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationPropertyBinding;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
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
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.springframework.core.annotation.AnnotationUtils;

import odata4fx.core.annotations.ODataCreateEntity;
import odata4fx.core.annotations.ODataDeleteEntity;
import odata4fx.core.annotations.ODataNavigation;
import odata4fx.core.annotations.ODataReadEntity;
import odata4fx.core.annotations.ODataReadEntityCollection;
import odata4fx.core.annotations.ODataUpdateEntity;



public class ODataServiceHandler implements EntityCollectionProcessor, EntityProcessor {
	private OData 				initODataItem;
	private ServiceMetadata 	initServiceMetaData;
	private ODataEntityHelper 	oDataHelper;
	private GenericEDMProvider  edmProvider;
	private Locale				locale = Locale.ENGLISH;
	 
	public ODataServiceHandler(GenericEDMProvider provider, ODataEntityHelper odataEntityHelper) {
		this.edmProvider = provider;
		this.oDataHelper = odataEntityHelper;
	}
	
	@Override
	public void init(OData odata, ServiceMetadata metadata) {
		this.initODataItem = odata;
		this.initServiceMetaData = metadata;
		
	}
	
	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
		processReadRequest(request, response, uriInfo, responseFormat);
	}
	
	@Override
	public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)	throws ODataApplicationException, ODataLibraryException {
  	   processReadRequest(request, response, uriInfo, responseFormat);
	}
	
	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo,  ContentType requestFormat, ContentType responseFormat)  throws  ODataApplicationException {
		try {
			EdmEntitySet edmEntitySet       = getEdmEntitySet(uriInfo);
			EdmEntityType edmEntityType     = edmEntitySet.getEntityType();
			Class  workEntityClass     		= edmProvider.findActualClass(edmEntitySet.getEntityType().getFullQualifiedName());
			Object businessService 			= instantiateDataService(edmEntitySet.getEntityType());
			InputStream requestInputStream  = request.getBody();
			ODataDeserializer deserializer  = initODataItem.createDeserializer(requestFormat);
			DeserializerResult result 	    = deserializer.entity(requestInputStream, edmEntityType);
			Entity requestEntity 		    = result.getEntity();
			List<ODataParameter> attributes = new ArrayList<>();
			for(Property prop : requestEntity.getProperties()) {
			  attributes.add(new ODataParameter(prop));
			}
			Object newCreatedEntity   = invokeMethod(businessService, workEntityClass, ODataCreateEntity.class,attributes);
			Entity actualODataEntity  = oDataHelper.buildEntity(newCreatedEntity);
			serializeEntity(response, responseFormat, edmEntitySet, edmEntityType, actualODataEntity);
		}
		catch(Exception e) {
		   throw createException(" Method [createEntity] raised an exception "+e.getMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR);
		}
	}

	
	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)	throws ODataApplicationException, ODataLibraryException {
  	   List<ODataParameter> keys	     = getKeyPredicates(uriInfo);
  	   EdmEntitySet edmEntitySet     = getEdmEntitySet(uriInfo);
	   Object businessService 		 = instantiateDataService(edmEntitySet.getEntityType());
	   Class  workEntityClass     	 = edmProvider.findActualClass(edmEntitySet.getEntityType().getFullQualifiedName());
	   invokeMethod(businessService, workEntityClass, ODataDeleteEntity.class,keys);
	   response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}


	

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)	throws ODataApplicationException, ODataLibraryException {
	 // 1. Retrieve the entity type from the URI
	  EdmEntitySet edmEntitySet     = getEdmEntitySet(uriInfo);
	  EdmEntityType edmEntityType   = edmEntitySet.getEntityType();
	  Class  workEntityClass    	= edmProvider.findActualClass(edmEntitySet.getEntityType().getFullQualifiedName());
	  Object businessService 	    = instantiateDataService(edmEntitySet.getEntityType());
	  // 2.1. retrieve the payload from the POST request for the entity to create and deserialize it
	  InputStream requestInputStream = request.getBody();
	  ODataDeserializer deserializer = initODataItem.createDeserializer(requestFormat);
	  DeserializerResult result 	 = deserializer.entity(requestInputStream, edmEntityType);
	  Entity requestEntity 			 = result.getEntity();
	  List<ODataParameter> attributes = new ArrayList<>();
	  for(Property prop : requestEntity.getProperties()) {
		  attributes.add(new ODataParameter(prop));
	  }
	  Object target	= invokeMethod(businessService, workEntityClass, ODataUpdateEntity.class,attributes);
	  Entity actualODataEntity  = oDataHelper.buildEntity(target);
	  serializeEntity(response, responseFormat, edmEntitySet, edmEntityType, actualODataEntity);
	}

	
	/****
	 * Processes a generic read request.
	 * 
	 */
	
	private void processReadRequest(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException {
		 
		 Object currentWorkingEntity		 	 = null;
		 EdmEntityType	currentEdmEntityType	 = null;
		 EdmEntitySet   currentEdmEntitySet		 = null;
		 Collection currentReadCollection	 	 = null;
		 
		 List<UriResource> resourcePaths 	 	 = uriInfo.getUriResourceParts();
		 CountOption countOption 				 = uriInfo.getCountOption();
		 // These system query options may affect how collections of entities are returned from business methods.
		 TopOption 	 topOption 					 = uriInfo.getTopOption();
		 SkipOption	 skipOption					 = uriInfo.getSkipOption();
		 SelectOption selectOption 				 = uriInfo.getSelectOption();
		 OrderByOption orderingOption			 = uriInfo.getOrderByOption();
		 // Expand entities
		 ExpandOption expandOption 				 = uriInfo.getExpandOption();
		 // Filtering
		 FilterOption filtering					 = uriInfo.getFilterOption();
		  
		 int lastIndex						 = resourcePaths.size()-1;
		 for(int uriIndex = 0; uriIndex < resourcePaths.size(); uriIndex++) {
			 UriResource currentResourcePart = resourcePaths.get(uriIndex);
			//System.out.println("Processing part "+uriIndex+" of "+lastIndex);
			// If the resourcePart is an UriResourceEntitySet, we are working on a EntitySet or an Entity directly,
			// i.e we are not navigating across entities (Products(1)->Category->Products()
			if(currentResourcePart instanceof UriResourceEntitySet) {
			   UriResourceEntitySet uriEntitySet = (UriResourceEntitySet) currentResourcePart;
			   EdmEntitySet edmEntitySet   		 = uriEntitySet.getEntitySet();
			   EdmEntityType edmEntityType 		 = edmEntitySet.getEntityType();
			   Object businessService      		 = instantiateDataService(edmEntityType);
			   Class  workEntityClass      		 = edmProvider.findActualClass(edmEntityType.getFullQualifiedName());
			   List<ODataParameter> keys  		 = getKeyPredicates(uriInfo);
			   addSystemQueryOptions(keys,countOption, topOption, skipOption, orderingOption, filtering);
			   // Is it a collection....
			   if(uriEntitySet.isCollection()) {
				  EntityCollection entitySet = new EntityCollection();
				  currentReadCollection  = (Collection<Object>) invokeMethod(businessService,workEntityClass, ODataReadEntityCollection.class, keys);
				  currentEdmEntitySet    = edmEntitySet;
				  currentEdmEntityType   = edmEntityType;
				  // If we reached the last segment, we send to the client the read collection serialized.
				  if(uriIndex == lastIndex) {
					  for(Object localEntity : currentReadCollection) {
						  Entity currentEntity = oDataHelper.buildEntity(localEntity);
						  if(expandOption != null) {
							  applyExpansion(currentEdmEntitySet, currentEntity,localEntity, expandOption);
						  }
						  entitySet.getEntities().add(currentEntity);
						  
					  }
					  if(countOption != null && countOption.getValue()) {
						  entitySet.setCount(currentReadCollection.size());
					  }
					  serializeCollection(request, response, responseFormat, edmEntitySet, entitySet, countOption, selectOption,expandOption, filtering);
					  return;
				  }
				
			   }
			   // ... or a single entity ?
			   else {
				  currentEdmEntitySet    = edmEntitySet;
				  currentEdmEntityType   = edmEntityType; 
			   	  currentWorkingEntity =  invokeMethod(businessService, workEntityClass, ODataReadEntity.class,keys);
   			      // If we reached the last segment, we send to the client the serialized entity.
				  if(uriIndex == lastIndex) {
					  Entity actualODataEntity  = oDataHelper.buildEntity(currentWorkingEntity);
					  if(expandOption != null) {
						  applyExpansion(currentEdmEntitySet, actualODataEntity,currentWorkingEntity, expandOption);
					  }
					  serializeEntity(response, responseFormat, edmEntitySet, edmEntityType, actualODataEntity,selectOption,expandOption, filtering);
				  }
			   }
			}
			if(currentResourcePart instanceof UriResourceNavigation) {
				UriResourceNavigation uriEntityNavigation = (UriResourceNavigation) currentResourcePart;
				EdmEntityType edmEntityType 		 = uriEntityNavigation.getProperty().getType();
				Object businessService      		 = instantiateDataService(edmEntityType);
				List<ODataParameter> params  		 = getParametersForNavigation(uriEntityNavigation);
				addSystemQueryOptions(params,countOption, topOption, skipOption,orderingOption,filtering);
				
				if(currentWorkingEntity == null) {
					throw createException("Required navigation for a NULL Object", HttpStatusCode.INTERNAL_SERVER_ERROR);
				}
				
				// We are navigating from an entity to a collection (relation: one to many).
				// In our example is http://localhost:8080/DataService/ProductStore/Categories(1)/Products.
				// 
				if(uriEntityNavigation.isCollection()) {
					String name							 =  uriEntityNavigation.getProperty().getName();
					EdmBindingTarget edmBindingTarget    =  currentEdmEntitySet.getRelatedBindingTarget(uriEntityNavigation.getProperty().getName()); 
					Class  targetEntityClass      		 =  edmProvider.findActualClass(edmEntityType.getFullQualifiedName());
					String relatedEntitySetName			 =  oDataHelper.getEntitySetName(targetEntityClass);
					EdmEntitySet	 edmEntitySet		 =  edmBindingTarget.getEntityContainer().getEntitySet(relatedEntitySetName);
					currentReadCollection 				 =  (Collection) invokeNavigationMethod(businessService, currentWorkingEntity.getClass(), targetEntityClass, ODataNavigation.class, currentWorkingEntity,params);
					currentEdmEntitySet					 =  edmEntitySet;
					currentEdmEntityType				 =  edmEntityType; 
					// If the query returns a single object, I treat it as an entity request.
					if(uriIndex == lastIndex) {
						if(currentReadCollection.size() == 1) {
							currentWorkingEntity = currentReadCollection.iterator().next();
							Entity actualODataEntity  = oDataHelper.buildEntity(currentWorkingEntity);
							if(expandOption != null) {
							    applyExpansion(currentEdmEntitySet, actualODataEntity,currentWorkingEntity, expandOption);
							}
							serializeEntity(response, responseFormat, edmEntitySet, edmEntityType, actualODataEntity,selectOption,expandOption,filtering);
						}
						else {
							EntityCollection entityCollection = new EntityCollection();
							for(Object localEntity : currentReadCollection) {
								Entity currentEntity = oDataHelper.buildEntity(localEntity);
								if(expandOption != null) {
									applyExpansion(currentEdmEntitySet, currentEntity,localEntity, expandOption);
								}
								entityCollection.getEntities().add(currentEntity);
							}
							if(countOption != null && countOption.getValue()) {
								entityCollection.setCount(currentReadCollection.size());
							}
							serializeCollection(request, response, responseFormat, currentEdmEntitySet, entityCollection, countOption,selectOption,expandOption,filtering);
						}
					}
				}
				else { 
					// We are navigating from an Entity to a RelatedEntity.
					// This may be the case of a one-to-one relationship as well as a one-to-many relationship where a single item is returned;
					// it's the case for example of uri In our example is http://localhost:8080/DataService/ProductStore/Categories(1)/Products(1)
					// Generally speaking, the method for handling a one-to-many relationship is annotated with @ODataNavigation and returns a collection.
					// To handle properly further navigations (i.e In our example is http://localhost:8080/DataService/ProductStore/Categories(1)/Products(1)/Category)
					// if the returned collection contains a single element, we set the currentWorkingEntity reference to the only one item in the collection
					// and we serialize a single entity (not the whole collection).
					EdmBindingTarget edmBindingTarget    =  currentEdmEntitySet.getRelatedBindingTarget(uriEntityNavigation.getProperty().getName()); 
					Class  targetEntityClass      		 =  edmProvider.findActualClass(edmEntityType.getFullQualifiedName());
					String relatedEntitySetName			 =  oDataHelper.getEntitySetName(targetEntityClass);
					EdmEntitySet	 edmEntitySet		 =  edmBindingTarget.getEntityContainer().getEntitySet(relatedEntitySetName);
					Object result						 =  invokeNavigationMethod(businessService, currentWorkingEntity.getClass(), targetEntityClass, ODataNavigation.class, currentWorkingEntity,params);
					if(result instanceof Collection) {
						Collection readCollection		 = (Collection) result;
						if(readCollection.size() == 1) {
							currentWorkingEntity  = readCollection.iterator().next();
						}
						if(readCollection.isEmpty()) {
							throw createException("No data found with required parameters.", HttpStatusCode.NOT_FOUND);
						}
					}
					else {
						currentWorkingEntity = result;
					}
					currentEdmEntitySet					 =  edmEntitySet;
					currentEdmEntityType				 =  edmEntityType;
					 if(uriIndex == lastIndex) {
						Entity actualODataEntity  = oDataHelper.buildEntity(currentWorkingEntity);
						if(expandOption != null && currentWorkingEntity != null) {
						    applyExpansion(currentEdmEntitySet, actualODataEntity,currentWorkingEntity, expandOption);
						}
					    serializeEntity(response, responseFormat, edmEntitySet, edmEntityType, actualODataEntity,selectOption,expandOption,filtering);
					 }
				}
			}
			
 		 }
		 
	}
	
	/***
	 * This method takes care to included expanded property
	 * as items.
	 * We suppport only a direct expansion (master -> detail), not other patterns
	 * @param currentEntity
	 * @param expandOption
	 */
	private void applyExpansion(EdmEntitySet entitySet, Entity entity, Object javaEntity, ExpandOption expandOption) throws ODataApplicationException {
		List<EdmNavigationPropertyBinding> bindings = entitySet.getNavigationPropertyBindings();
		List<Class> expandClassList 				= new ArrayList<>();
		Object businessService      		 		= instantiateDataService(entitySet.getEntityType());
		boolean starExpansion						= false;
		Class   sourceEntityClass					= edmProvider.findActualClass(entitySet.getEntityType().getFullQualifiedName());
		ArrayList<ODataParameter> params			= new ArrayList<>();
		/***
		 * I analyze which properties are required as expanded items. I assume that if a '*' expression
		 * is used, any expandable property is required, so i clear the list of expanded properties classes.
		 * Otherwise, at the end of the first 'for' loop I have a list of required expandable properties.
		 * By the way, Olingo fx ensures that only existing properties may be specified.I check if a specified
		 * property exists nevertless.
		 * 
		 */
		for(ExpandItem expandedItem : expandOption.getExpandItems()) {
			if(expandedItem.isStar()) {
				starExpansion = true;
				expandClassList.clear();
				break;
			}
			List<UriResource> resources = expandedItem.getResourcePath().getUriResourceParts();
			for(UriResource resource : resources) {
				String className = resource.getSegmentValue();
				Class actualClass = edmProvider.findActualClass(className);
				if(actualClass != null && !expandClassList.contains(actualClass)) {
					expandClassList.add(actualClass);
				}
			}
		}
		
		
		for(EdmNavigationPropertyBinding navProperty : bindings) {
			EdmElement property = entitySet.getEntityType().getProperty(navProperty.getPath());
			String navPropName = property.getName();
			Class navPropertyClass = edmProvider.findActualClass(navPropName);
			if(expandClassList.contains(navPropertyClass)) {
				Object result						 =  invokeNavigationMethod(businessService, sourceEntityClass, navPropertyClass, ODataNavigation.class, javaEntity, params);
				Link link = new Link();
				link.setTitle(navPropName);
				link.setInlineEntity(oDataHelper.buildEntity(result));
				entity.getNavigationLinks().add(link);
			}
		}
	}
		
	
	

	
	
	/// Helper methods
	
	private void addSystemQueryOptions(List<ODataParameter> lst, SystemQueryOption ...options) {
		for(SystemQueryOption opt : options) {
			if(opt != null) {
				if(opt instanceof OrderByOption) {
					OrderByOption option = (OrderByOption) opt;
					for(OrderByItem item : option.getOrders()) {
						ODataParameter param = new ODataParameter(item);
						if(param.isValid()) {
							lst.add(param);
						}
					}
				}
				else {
					lst.add(new ODataParameter(opt));
				}
			}
		}
	}
	
	private void serializeEntity(ODataResponse response, ContentType responseFormat, EdmEntitySet edmEntitySet,  EdmEntityType edmEntityType, Entity entity, SystemQueryOption ... systemQueryOptions) throws ODataApplicationException {
		try {
		   //Entity actualODataEntity  = oDataHelper.buildEntity(object);
		  ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
		  //EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();
		  EntitySerializerOptions.Builder builder = EntitySerializerOptions.with().contextURL(contextUrl);
		  for(SystemQueryOption option :systemQueryOptions) {
			  if (option != null) {
				   if (option instanceof SelectOption) {
					  builder = builder.select((SelectOption) option);
				  }
				   if (option instanceof ExpandOption) {
						  builder = builder.expand((ExpandOption) option);
				   }
			  }
		  }
		  ODataSerializer serializer = initODataItem.createSerializer(responseFormat);
		  SerializerResult serializedResponse = serializer.entity(initServiceMetaData, edmEntityType, entity, builder.build());
		  response.setContent(serializedResponse.getContent());
		  response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
		  response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
		}
		catch(SerializerException se) {
			throw createException("An exception occurred during serialization of response ("+se.getMessage()+")", HttpStatusCode.INTERNAL_SERVER_ERROR);
		}
	}

	
	private Object instantiateDataService(EdmEntityType entityType) throws ODataApplicationException {
		Class clz = edmProvider.findActualClass(entityType.getFullQualifiedName());
		if(clz == null) {
			throw createException("No entity with name "+entityType.getFullQualifiedName()+" is registered.", HttpStatusCode.BAD_REQUEST);
		}
		return oDataHelper.getController(clz);
	}

	
	
	private ODataApplicationException createException(String message, HttpStatusCode statusCode) {
		 return new ODataApplicationException(message, statusCode.getStatusCode(),locale);
	}

	
	private EdmEntitySet getEdmEntitySet(UriInfoResource uriInfo) throws ODataApplicationException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
            throw createException("Invalid resource type for first segment.", HttpStatusCode.NOT_IMPLEMENTED);
        }
        UriResourceEntitySet uriResource = (UriResourceEntitySet) resourcePaths.get(0);
        return uriResource.getEntitySet();
    }

	
	
	private List<ODataParameter> getKeyPredicates(UriInfo uriInfo) {
		UriResourceEntitySet uriResourceEntitySet 	= (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0);
		return getKeyPredicates(uriResourceEntitySet);
	}
	
	private List<ODataParameter> getKeyPredicates(UriResourceEntitySet uriResourceEntitySet) {
	    List<UriParameter> keyPredicates 			= uriResourceEntitySet.getKeyPredicates();
	    List<ODataParameter> keys = new ArrayList<>();
	    for(UriParameter uri : keyPredicates)  {
	    	keys.add(new ODataParameter(uri));
	    }
	    return keys;
	}
	
	private List<ODataParameter> getParametersForNavigation(UriResourceNavigation uriNavigation) {
		 List<UriParameter> keyPredicates 			= uriNavigation.getKeyPredicates();
		 List<ODataParameter> keys = new ArrayList<>();
		 for(UriParameter uri : keyPredicates)  {
		   	keys.add(new ODataParameter(uri));
		 }
		 return keys;
	}
	
	
	private void serializeCollection(ODataRequest request, ODataResponse response, ContentType contentType, EdmEntitySet edmEntitySet, EntityCollection entitySet, SystemQueryOption ... systemQueryOptions) throws ODataApplicationException {
		  try {
			  ODataSerializer serializer = initODataItem.createSerializer(contentType);
			  EdmEntityType edmEntityType = edmEntitySet.getEntityType();
			  ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
			  final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
			  //EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl).count(count).build();
			  EntityCollectionSerializerOptions.Builder builder  = EntityCollectionSerializerOptions.with().id(id).contextURL(contextUrl);
			  for(SystemQueryOption option :systemQueryOptions) {
				  if (option != null) {
					  if (option instanceof CountOption) {
						  builder = builder.count((CountOption) option);
					  }
					  if (option instanceof SelectOption) {
						  builder = builder.select((SelectOption) option);
					  }
					  if (option instanceof ExpandOption) {
						  builder = builder.expand((ExpandOption) option);
					  }
				  }
			  }
			  SerializerResult serializerResult = serializer.entityCollection(initServiceMetaData, edmEntityType, entitySet, builder.build());
			  InputStream serializedContent = serializerResult.getContent();
			  response.setContent(serializedContent);
			  response.setStatusCode(HttpStatusCode.OK.getStatusCode());
			  response.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());
		  }
		  catch(Exception e) {
			  createException("Internal error occurred ", HttpStatusCode.INTERNAL_SERVER_ERROR);
		  }
	}
	
	
	
	/// Reflection support for invoking methods.
	/// Verifies if, given a certain annotation, a method with such annotation exists and accepts expected parameters
	private Object invokeMethod(Object businessService, Class<?> workEntityClass, Class<? extends Annotation> annotation, List<ODataParameter> params) throws ODataApplicationException {
		Method targetMethod = null;
		for(Method method : businessService.getClass().getDeclaredMethods()) {
			Class[] mParams  = method.getParameterTypes();
			// Target method must be annotated with required annotation
			Annotation   mAnnotation 	 = AnnotationUtils.findAnnotation(method, annotation);
			boolean annotationPresent = mAnnotation != null;
			if(annotationPresent) {
				boolean matches  		  = annotationPresent; 
				Class<?>   returnType	  = method.getReturnType();
				// Reading collections of entities
				if(annotation.equals(ODataReadEntityCollection.class)) {
					ODataReadEntityCollection actualAnnotation = (ODataReadEntityCollection) mAnnotation; 
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1 && mParams[0].isAssignableFrom(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= Collection.class.isAssignableFrom(returnType); // Must return a Collection
					if(matches) {
						targetMethod = method;
						break;
					}
				}
				else // Read of an entity
				if(annotation.equals(ODataReadEntity.class)) {
					ODataReadEntity actualAnnotation = (ODataReadEntity) mAnnotation;
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1 && mParams[0].isAssignableFrom(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= returnType.equals(workEntityClass); // Must return an object
					if(matches) {
						targetMethod = method;
						break;
					}
				}
				else // Creation of an Entity
				if(annotation.equals(ODataCreateEntity.class)) {
					ODataCreateEntity actualAnnotation = (ODataCreateEntity)  mAnnotation;
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1 && mParams[0].isAssignableFrom(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= returnType.equals(workEntityClass); // Must return an object
					if(matches) {
						targetMethod = method;
						break;
					}
				}
				else // Deletion of an Entity
				if(annotation.equals(ODataDeleteEntity.class)) {
					ODataDeleteEntity actualAnnotation = (ODataDeleteEntity)  mAnnotation;
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1 && mParams[0].isAssignableFrom(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class); // must return nothing
					if(matches) {
						targetMethod = method;
						break;
					}
				}
				else // Update of an entity
				if(annotation.equals(ODataUpdateEntity.class)) {
					ODataUpdateEntity actualAnnotation = (ODataUpdateEntity)  mAnnotation;
					matches			&= actualAnnotation.value().equals(workEntityClass);
					matches			&= mParams.length == 1 && mParams[0].isAssignableFrom(params.getClass()); // Required parameter must by an array of ODataParamValue
					matches			&= returnType.equals(workEntityClass); // Must return an object
					if(matches) {
						targetMethod = method;
						break;
					}
				}
			}
		}
		if(targetMethod == null) {
			throw createException("No method found to process request", HttpStatusCode.BAD_REQUEST);
		}
		try {
			return targetMethod.invoke(businessService, (Object) params);
		}
		catch(Exception e) {
			throw createException("An error occurred while invoking method "+targetMethod.getName()+" on class "+businessService.getClass().getName()+"; original error is "+e.getMessage(),HttpStatusCode.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	private Object invokeNavigationMethod(Object businessService, Class<?> sourceEntityClass, Class<?> destinationEntityClass, Class<? extends Annotation> annotation, Object masterEntity, List<ODataParameter> params) throws ODataApplicationException {
		Method targetMethod = null;
		for(Method method : businessService.getClass().getDeclaredMethods()) {
			Class<?>[] mParams  = method.getParameterTypes();
			// Target method must be annotated with required annotation
			boolean annotationPresent = method.isAnnotationPresent(annotation);
			if(annotationPresent) {
				boolean matches  		  = annotationPresent; 
				Class<?>   returnType	  = method.getReturnType();
				// Reading collections of entities
				if(annotation.equals(ODataNavigation.class)) {
					ODataNavigation actualAnnotation = (ODataNavigation) method.getAnnotation(annotation); 
					matches			&= actualAnnotation.fromEntity().equals(sourceEntityClass);
					matches			&= actualAnnotation.toEntity().equals(destinationEntityClass);
					if(matches) {
						targetMethod = method;
						break;
					}
				}
			}
		}
		if(targetMethod == null) {
			throw createException("No method found to process request", HttpStatusCode.BAD_REQUEST);
		}
		try {
			return targetMethod.invoke(businessService, masterEntity, (Object) params);
		}
		catch(Exception e) {
			throw createException("An error occurred while invoking method "+targetMethod.getName()+" on class "+businessService.getClass().getName()+"; original error is "+e.getMessage(),HttpStatusCode.INTERNAL_SERVER_ERROR);
		}
	}
	

}

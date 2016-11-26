package mclaudio76.odataspring.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

public class GenericEDMProvider extends CsdlAbstractEdmProvider {

	public String NAMESPACE 	 = "";   //"OData.Demo";
	public String CONTAINER_NAME = "";  //"Container";
	
	public String HANDLED_ENTITY_NAME = ""; 	//"Product";
	public String HANDLED_ENTITY_SET_NAME = "";	//"Products";
	private ODataEntityHelper annotationHelper  = new ODataEntityHelper();
	
	public FullQualifiedName ENTITY_FULL_QUALIFIED_NAME = null;
	public FullQualifiedName CONTAINER = null;
	
	private Class ODataEntity = null;
	
	
	public GenericEDMProvider(String nameSpace, String containerName, Class<?> ODataEntity) throws Exception {
		NAMESPACE 						= nameSpace;
		CONTAINER_NAME  				= containerName;
		this.ODataEntity				= ODataEntity;
		HANDLED_ENTITY_NAME 			= annotationHelper.getEntityName(ODataEntity);
		HANDLED_ENTITY_SET_NAME 		= annotationHelper.getEntitySetName(ODataEntity);
		ENTITY_FULL_QUALIFIED_NAME		= new FullQualifiedName(NAMESPACE, HANDLED_ENTITY_NAME);
		CONTAINER       				= new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
	}
	
	
	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {
		  List<CsdlEntitySet> entitySets = new ArrayList<>();
		  entitySets.add(getEntitySet(CONTAINER, HANDLED_ENTITY_SET_NAME));
		  CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		  entityContainer.setName(CONTAINER_NAME);
		  entityContainer.setEntitySets(entitySets);
		  return entityContainer;
	}

	@Override
	public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
	    if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
	        CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
	        entityContainerInfo.setContainerName(CONTAINER);
	        return entityContainerInfo;
	    }

	    return null;
	}

	@Override
	public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
		if(entityContainer.equals(CONTAINER)){
		    if(entitySetName.equals(HANDLED_ENTITY_SET_NAME)){
		      CsdlEntitySet entitySet = new CsdlEntitySet();
		      entitySet.setName(HANDLED_ENTITY_SET_NAME);
		      entitySet.setType(ENTITY_FULL_QUALIFIED_NAME);

		      return entitySet;
		    }
		  }
		return null;
	}

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		// this method is called for one of the EntityTypes that are configured in the Schema
	
	  if(entityTypeName.equals(ENTITY_FULL_QUALIFIED_NAME)){
			CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(HANDLED_ENTITY_NAME);
		    entityType.setProperties(annotationHelper.getClassAttributes(ODataEntity));
		    entityType.setKey(annotationHelper.getClassKeys(ODataEntity));
		    return entityType;
  	   }
 	   return null;
	
	}

	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {
		// create Schema
		  CsdlSchema schema = new CsdlSchema();
		  schema.setNamespace(NAMESPACE);

		  // add EntityTypes
		  List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		  entityTypes.add(getEntityType(ENTITY_FULL_QUALIFIED_NAME));
		  schema.setEntityTypes(entityTypes);

		  // add EntityContainer
		  schema.setEntityContainer(getEntityContainer());

		  // finally
		  List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		  schemas.add(schema);

		  return schemas;
	}

}

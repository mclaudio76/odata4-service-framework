package mclaudio76.odata4fx.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

public class GenericEDMProvider extends CsdlAbstractEdmProvider {

	public String NAMESPACE 	 = "";   //"OData.Demo";
	public String CONTAINER_NAME = "";  //"Container";
	
	private ODataEntityHelper annotationHelper  = new ODataEntityHelper();
	
	public FullQualifiedName CONTAINER = null;
	
	private ArrayList<ExposedEntity> exposedEntities = new ArrayList<ExposedEntity>();
	
	class ExposedEntity {
		String handledEntityName 		= "";
		String handledEntitySetName 	= "";
		Class<?> entityClass 			= null;
		FullQualifiedName entityFQN 	= null;
		
		public ExposedEntity(Class<?> entityClass) throws Exception {
			this.entityClass = entityClass;
			handledEntityName 			= annotationHelper.getEntityName(entityClass);
			handledEntitySetName 		= annotationHelper.getEntitySetName(entityClass);
			entityFQN					= new FullQualifiedName(NAMESPACE, handledEntityName);
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((entityFQN == null) ? 0 : entityFQN.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExposedEntity other = (ExposedEntity) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (entityFQN == null) {
				if (other.entityFQN != null)
					return false;
			} else if (!entityFQN.equals(other.entityFQN))
				return false;
			return true;
		}


		private GenericEDMProvider getOuterType() {
			return GenericEDMProvider.this;
		}
	}
	
	public GenericEDMProvider(String nameSpace, String containerName) throws Exception {
		NAMESPACE 						= nameSpace;
		CONTAINER_NAME  				= containerName;
		CONTAINER       				= new FullQualifiedName(NAMESPACE, CONTAINER_NAME);
	}
	
	
	public GenericEDMProvider addEntity(Class<?> oDataEntity)  throws Exception{
		ExposedEntity entity = new ExposedEntity(oDataEntity);
		if(!exposedEntities.contains(entity)) {
			exposedEntities.add(entity);
		}
		else {
			log(" Entity "+oDataEntity.getName()+" already published as ODataResource.");
		}
		return this;
	}
	
	
	
	@Override
	public CsdlEntityContainer getEntityContainer() throws ODataException {
		  List<CsdlEntitySet> entitySets = new ArrayList<>();
		  for(ExposedEntity entity : exposedEntities) {
			  entitySets.add(getEntitySet(CONTAINER, entity.handledEntitySetName));
		  }
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
			for(ExposedEntity entity : exposedEntities) {
				if(entitySetName.equals(entity.handledEntitySetName)){
				      CsdlEntitySet entitySet = new CsdlEntitySet();
				      entitySet.setName(entity.handledEntityName);
				      entitySet.setType(entity.entityFQN);
				      List<CsdlNavigationPropertyBinding> navigationPropertyBindings = annotationHelper.getNavigationPropertiesForEntitySet(entity.entityClass);
					  if(navigationPropertyBindings != null && !navigationPropertyBindings.isEmpty()) {
					   	entitySet.setNavigationPropertyBindings(navigationPropertyBindings);
					  }     
				      return entitySet;
				}	
			}
		}
		return null;
	}

	@Override
	public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
		for(ExposedEntity entity : exposedEntities) {
		  if(entityTypeName.equals(entity.entityFQN)){
				CsdlEntityType entityType = new CsdlEntityType();
				entityType.setName(entity.handledEntityName);
			    entityType.setProperties(annotationHelper.getClassAttributes(entity.entityClass));
			    entityType.setKey(annotationHelper.getClassKeys(entity.entityClass));
			    List<CsdlNavigationProperty> relations = annotationHelper.getNavigationProperties(entity.entityClass, NAMESPACE);
			    if(relations != null && !relations.isEmpty()) {
			    	entityType.setNavigationProperties(relations);
			    }
			    return entityType;
	  	   }
		}
 	   return null;
	
	}

	@Override
	public List<CsdlSchema> getSchemas() throws ODataException {
		// create Schema
		  CsdlSchema schema = new CsdlSchema();
		  schema.setNamespace(NAMESPACE);
		  List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
		  // add EntityTypes
		  for(ExposedEntity entity : exposedEntities) {
			  entityTypes.add(getEntityType(entity.entityFQN));
		  }
		  schema.setEntityTypes(entityTypes);
		  // add EntityContainer
		  schema.setEntityContainer(getEntityContainer());
		  // finally
		  List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
		  schemas.add(schema);

		  return schemas;
	}
	
	private void log(String mex) {
		System.out.println(this.getClass().getName()+" >> "+mex);
	}


	public Class findActualClass(FullQualifiedName fullQualifiedName) {
		for(ExposedEntity entity : exposedEntities) {
			if(entity.entityFQN.equals(fullQualifiedName)) {
				return entity.entityClass;
			}
		}
		return null;
	}
	
	public Class findActualClass(String entityName) {
		for(ExposedEntity entity : exposedEntities) {
			if(entity.handledEntityName.equals(entityName)) {
				return entity.entityClass;
			}
		}
		return null;
	}

}

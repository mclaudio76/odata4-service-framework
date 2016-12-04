package mclaudio76.odataspring.core;

import java.util.List;

public interface IODataService<T> {
	
	// Retrieve all entities.
	public List<T> listAll(); 
	
	// Retrieve the entity matching all the keys.
	public T findByKey(ODataParamValue ...keys);
	
	// Creates a new entity
	public T create(ODataParamValue ... values);
	
	// Creates a new entity
	public void delete(ODataParamValue ... keys);
	
	// Creates a new entity
	public T update(ODataParamValue ... values);
	
	// Return actual handled entity class name
	public Class<T> getEntityClass();
	
	
}

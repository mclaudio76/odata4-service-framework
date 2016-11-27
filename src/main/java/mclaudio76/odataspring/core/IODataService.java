package mclaudio76.odataspring.core;

import java.util.List;

public interface IODataService<T> {
	
	// Retrieve all entities.
	public List<T> listAll(); 
	
	// Retrieve the entity matching all the keys.
	public T findByKey(ODataParamValue ...keys);
	
	// Creates a new entity
	public T create(ODataParamValue ... values);
	
	// Deletes a new entity
	public void delete(ODataParamValue ... keys);
	
	// Update an existing entity
	public T update(T target, ODataParamValue ... values);
	
	// Return actual handled entity class name
	public Class<T> getEntityClass();
	
	
}

package mclaudio76.odataspring.demo;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;

import mclaudio76.odataspring.core.ODataEntity;
import mclaudio76.odataspring.core.ODataField;
import mclaudio76.odataspring.core.ODataNavigationProperty;

	

@ODataEntity(entityName="Product",entitySetName="Products", controller=ProductService.class)
public class Product {
	
	@ODataField(isKey=true, ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.Int32)
	public Integer ID;
	
	@ODataField(ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.String)
	public String  name;
	
	@ODataField(ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.String)
	public String  description;
	
	@ODataNavigationProperty(name="Category",nullable=true,partner="Products", entityType=Category.class)
	public Category category = null;
	
	public Product() {
		this(0,"","");
	}
	
	public Product(Integer id, String name, String description) {
		this.ID = id;
		this.name = name;
		this.description  = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
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
		Product other = (Product) obj;
		if (ID == null) {
			if (other.ID != null)
				return false;
		} else if (!ID.equals(other.ID))
			return false;
		return true;
	}
	
	
}

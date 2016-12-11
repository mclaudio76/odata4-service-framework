package mclaudio76.odata4fx.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;

import mclaudio76.odata4fx.core.annotations.ODataEntity;
import mclaudio76.odata4fx.core.annotations.ODataField;
import mclaudio76.odata4fx.core.annotations.ODataNavigationProperty;

@ODataEntity(entityName="Category",entitySetName="Categories", controller=ProductStoreService.class)
public class Category {
	
	@ODataField(isKey=true, ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.Int32)
	public Integer categoryID;
	
	@ODataField(ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.String)
	public String  categoryDescription;
	
	@ODataField(ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.String)
	public String  shortName;
	
	@ODataNavigationProperty(entityType=Product.class, name="Products", path="Products", target="Products", nullable=false, partner="Category")
	public List<Product> products = new ArrayList<Product>();

	public Category() {
		this(0,"","");
	}
	
	public Category(Integer id,  String description,String shortName) {
		this.categoryID = id;
		this.categoryDescription  = description;
		this.shortName            = shortName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((categoryDescription == null) ? 0 : categoryDescription.hashCode());
		result = prime * result + ((categoryID == null) ? 0 : categoryID.hashCode());
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
		Category other = (Category) obj;
		if (categoryDescription == null) {
			if (other.categoryDescription != null)
				return false;
		} else if (!categoryDescription.equals(other.categoryDescription))
			return false;
		if (categoryID == null) {
			if (other.categoryID != null)
				return false;
		} else if (!categoryID.equals(other.categoryID))
			return false;
		return true;
	}

	
	
}
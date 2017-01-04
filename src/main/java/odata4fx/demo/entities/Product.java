package odata4fx.demo.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;

import odata4fx.core.annotations.ODataEntity;
import odata4fx.core.annotations.ODataField;
import odata4fx.core.annotations.ODataNavigationProperty;
import odata4fx.demo.service.IProductStoreService;
	
@Entity
@Table(name = "products")
@ODataEntity(entityName="Product",entitySetName="Products", controller=IProductStoreService.class)
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@ODataField(isKey=true, ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.Int32)
	public Integer ID;
	
	@NotNull
	@ODataField(ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.String)
	public String  name;
	
	@NotNull
	@ODataField(ODataTypeKind=EdmTypeKind.PRIMITIVE, ODataType=EdmPrimitiveTypeKind.String)
	public String  description;
	
	@OneToOne
	@ODataNavigationProperty(entityType=Category.class, name="Category", path="Category", target="Categories", nullable=true, partner="Products")
	public Category category = null;
	
	public Product() {
		this(0,"","", null);
	}
	
	public Product(Integer id, String name, String description, Category cat) {
		this.ID = id;
		this.name = name;
		this.description  = description;
		this.category     = cat;
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

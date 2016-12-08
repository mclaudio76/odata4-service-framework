package mclaudio76.odataspring.demo;

import java.util.ArrayList;
import java.util.List;


import mclaudio76.odataspring.core.ODataEntityHelper;
import mclaudio76.odataspring.core.ODataParamValue;
import mclaudio76.odataspring.core.annotations.ODataController;
import mclaudio76.odataspring.core.annotations.ODataCreateEntity;
import mclaudio76.odataspring.core.annotations.ODataDeleteEntity;
import mclaudio76.odataspring.core.annotations.ODataReadEntity;
import mclaudio76.odataspring.core.annotations.ODataReadEntityCollection;
import mclaudio76.odataspring.core.annotations.ODataUpdateEntity;

@ODataController
public class ProductService  {
	
	private ArrayList<Product> products = new ArrayList<>();
	private ODataEntityHelper helper	= new ODataEntityHelper();
	
	public ProductService() {
		products.add(new Product(1, "Alfa  A1", "Racing car", new Category(1, "Category ALFA")));
		products.add(new Product(2, "Beta  B1", "Luxury car", new Category(1, "Category ALFA")));
		products.add(new Product(3, "Gamma G3", "Speedy car", new Category(2, "Category BETA")));
		products.add(new Product(4, "Delta D4", "City car",   new Category(2, "Category BETA")));
	} 
	
	
	@ODataReadEntityCollection(Product.class)
	public List<Product> listAll(ODataParamValue ... filters) {
		return products;
	}

	@ODataReadEntity(Product.class)
	public Product findByKey(ODataParamValue ... keys) {
		for(Product p : products) {
			if(helper.entityMatchesKeys(p, keys)) {
				return p;
			}
		}
		return null;
	}

	@ODataCreateEntity(Product.class)
	public Product create(ODataParamValue... values) {
		Product product = new Product();
		helper.setFieldsValueFromEntity(product, values);
		products.add(product);
		return product;
	}

	@ODataDeleteEntity(Product.class)
	public void delete(ODataParamValue... keys) {
		Product p = findByKey(keys);
		if(p != null) {
			products.remove(p);
		}
	}

	@ODataUpdateEntity(Product.class)
	public Product update(ODataParamValue... values) {
		return null;
	}

	

}

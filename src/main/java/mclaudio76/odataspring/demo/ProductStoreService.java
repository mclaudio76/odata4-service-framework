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
public class ProductStoreService  {
	
	private static ArrayList<Product> products 		= new ArrayList<>();
	private static ArrayList<Category> categories   = new ArrayList<>();	
	private static Boolean inited					= false;
	
	private ODataEntityHelper helper	= new ODataEntityHelper();
	
	public ProductStoreService() {
		synchronized (inited) {
			if(!inited) {
				inited = true;
				products.add(new Product(1, "Alfa  A1", "Racing car", new Category(1, "Category ALFA")));
				products.add(new Product(2, "Beta  B1", "Luxury car", new Category(1, "Category ALFA")));
				products.add(new Product(3, "Gamma G3", "Speedy car", new Category(2, "Category BETA")));
				products.add(new Product(4, "Delta D4", "City car",   new Category(2, "Category BETA")));
				
				categories.add(new Category(1, "Expensive cars"));
				categories.add(new Category(2, "Cheap cars"));
			}	
		}
	} 
	
	
	@ODataReadEntityCollection(Product.class)
	public List<Product> listAllProducts(List<ODataParamValue> params) {
		return products;
	}
	
	

	@ODataReadEntity(Product.class)
	public Product findProductByKey(List<ODataParamValue>  keys) {
		for(Product p : products) {
			if(helper.entityMatchesKeys(p, keys)) {
				return p;
			}
		}
		return null;
	}

	@ODataCreateEntity(Product.class)
	public Product createProduct(List<ODataParamValue> values) {
		Product product = new Product();
		helper.setFieldsValueFromEntity(product, values);
		products.add(product);
		return product;
	}

	@ODataDeleteEntity(Product.class)
	public void deleteProduct(List<ODataParamValue> keys) {
		Product p = findProductByKey(keys);
		if(p != null) {
			products.remove(p);
		}
	}

	@ODataUpdateEntity(Product.class)
	public Product updateProduct(List<ODataParamValue> keys) {
		return null;
	}

	
	/****
	 * Categories
	 */
	@ODataReadEntityCollection(Category.class)
	public List<Category> listAllCategories(List<ODataParamValue> keys) {
		return categories;
	}

	
	@ODataReadEntity(Category.class)
	public Category findCategoryByKey(List<ODataParamValue> keys) {
		for(Category p : categories) {
			if(helper.entityMatchesKeys(p, keys)) {
				return p;
			}
		}
		return null;
	}

	@ODataCreateEntity(Category.class)
	public Category createCategory(List<ODataParamValue> values) {
		Category category = new Category();
		helper.setFieldsValueFromEntity(category, values);
		categories.add(category);
		return category;
	}

	@ODataDeleteEntity(Category.class)
	public void deleteCategory(List<ODataParamValue>  keys) {
		Category p = findCategoryByKey(keys);
		if(p != null) {
			categories.remove(p);
		}
	}

	@ODataUpdateEntity(Product.class)
	public Category updateCategory(List<ODataParamValue> values) {
		return null;
	}
	
	

}

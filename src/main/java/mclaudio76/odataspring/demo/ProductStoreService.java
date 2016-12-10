package mclaudio76.odataspring.demo;

import java.util.ArrayList;
import java.util.List;

import mclaudio76.odata4fx.core.ODataEntityHelper;
import mclaudio76.odata4fx.core.ODataParamValue;
import mclaudio76.odata4fx.core.annotations.ODataController;
import mclaudio76.odata4fx.core.annotations.ODataCreateEntity;
import mclaudio76.odata4fx.core.annotations.ODataDeleteEntity;
import mclaudio76.odata4fx.core.annotations.ODataNavigation;
import mclaudio76.odata4fx.core.annotations.ODataReadEntity;
import mclaudio76.odata4fx.core.annotations.ODataReadEntityCollection;
import mclaudio76.odata4fx.core.annotations.ODataUpdateEntity;

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
				
				Category c1 = new Category(1, "Expensive cars");
				Category c2 = new Category(2, "Great cars");
				
				Product p1 = new Product(1, "Alfa  A1", "Racing car", c1);
				Product p2 = new Product(2, "Beta  B1", "Luxury car", c2);
				Product p3 = new Product(3, "Gamma G3", "Speedy car", c2);
				Product p4 = new Product(4, "Delta D4", "City car",   c1);
				
				c1.products.add(p1);
				c1.products.add(p4);
				
				c2.products.add(p2);
				c2.products.add(p3);
				
				
				products.add(p1);
				products.add(p2);
				products.add(p3);
				products.add(p4);
				
				
				categories.add(c1);
				categories.add(c2);
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

	// Navigation, from product to categories
	
	@ODataNavigation(fromEntity=Product.class, toEntity=Category.class)
	public Category getAssociatedCategory(Product item, List<ODataParamValue> params) {
		return item.category;
	}
	
	@ODataNavigation(fromEntity=Category.class, toEntity=Product.class)
	public List<Product> getAssociatedProducts(Category item, List<ODataParamValue> params) {
		List<Product> result = new ArrayList<Product>();
		for(Product p : item.products) {
			if(helper.entityMatchesKeys(p, params)) {
				result.add(p);
			}
		}
		return result;
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

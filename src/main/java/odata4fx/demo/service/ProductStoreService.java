package odata4fx.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import odata4fx.core.ODataEntityHelper;
import odata4fx.core.ODataParameter;
import odata4fx.demo.entities.Category;
import odata4fx.demo.entities.Product;

	

@Transactional
public class ProductStoreService implements IProductStoreService  {
	
	private static ArrayList<Product> products 		= new ArrayList<>();	
	private static ArrayList<Category> categories   = new ArrayList<>();	
	private static Boolean inited					= false;
	private ODataEntityHelper helper				= new ODataEntityHelper();
	
	
	private EntityManagerFactory entityManagerFactory = null;
	
	
	@PersistenceUnit
	public void setEntityManager(EntityManagerFactory emf) {
		this.entityManagerFactory = emf;
	}
	
	
	public ProductStoreService() {
		synchronized (inited) {
			if(!inited) {
				inited = true;
				
				Category c1 = new Category(1, "Expensive cars","EXPENSIVE");
				Category c2 = new Category(2, "Great cars","GREAT");
				
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
	
	
	
	@Override
	public List<Product> listAllProducts(List<ODataParameter> params) {
		
		Collections.sort(products, new Comparator<Product>() {

			@Override
			public int compare(Product o1, Product o2) {
				return o1.ID.compareTo(o2.ID);
			}
		});
		
		List<Product>  actual = new ArrayList<>(products);
		
		ODataParameter skip 		  = ODataParameter.getSkipOption(params);
		ODataParameter top  		  = ODataParameter.getTopOption(params);
		List<ODataParameter> ordering = ODataParameter.getOrderBy(params);
		String filterExpression		  = ODataParameter.getFilters(params);
		for(ODataParameter item : ordering) {
			System.out.println("Ordering by ["+item.getOrderByProperty()+"] "+(item.isDescending() ? "DESC":"ASC"));
		}
		System.out.println(filterExpression);
		
		if(skip != null) {
			if(skip.getTopValue() < actual.size()) {
				actual = actual.subList(skip.getSkipValue(), actual.size());
			}
			else {
				actual = new ArrayList<>();
			}
		}
		
		if (top != null) {
			if(top.getTopValue() < actual.size()) {
				actual = actual.subList(0, top.getTopValue());
			}
			else {
				actual = new ArrayList<>();
			}
		}
		
		return actual;
	}
	
		
	@Override
	public Product findProductByKey(List<ODataParameter>  keys) {
		for(Product p : products) {
			if(helper.entityMatchesKeys(p, keys)) {
				return p;
			}
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#createProduct(java.util.List)
	 */
	@Override
	public Product createProduct(List<ODataParameter> values) {
		Product product = new Product();
		helper.setFieldsValueFromEntity(product, values);
		product.ID = null;
		EntityManager em = entityManagerFactory.createEntityManager(); 
		em.persist(product);
		em.flush();
		products.add(product);
		return product;
	}
	
	@Override
	public void testSave() {
		Product product = new Product();
		product.description = "Created from scratch";
		product.name        = "Unset name";
		product.ID 			= null;
		EntityManager em = entityManagerFactory.createEntityManager(); 
		em.persist(product);
		em.flush();
	}

	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#deleteProduct(java.util.List)
	 */
	@Override
	public void deleteProduct(List<ODataParameter> keys) {
		Product p = findProductByKey(keys);
		if(p != null) {
			products.remove(p);
		}
	}

	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#updateProduct(java.util.List)
	 */
	@Override
	public Product updateProduct(List<ODataParameter> keys) {
		return null;
	}

	// Navigation, from product to categories
	
	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#getAssociatedCategory(odata4fx.demo.Product, java.util.List)
	 */
	@Override
	public Category getAssociatedCategory(Product item, List<ODataParameter> params) {
		return item.category;
	}
	
	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#getAssociatedProducts(odata4fx.demo.Category, java.util.List)
	 */
	@Override
	public List<Product> getAssociatedProducts(Category item, List<ODataParameter> params) {
		List<Product> result = new ArrayList<Product>();
		for(Product p : item.products) {
			if(helper.entityMatchesKeys(p, params)) {
				result.add(p);
			}
		}
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#listAllCategories(java.util.List)
	 */
	@Override
	public List<Category> listAllCategories(List<ODataParameter> keys) {
		return categories;
	}

	
	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#findCategoryByKey(java.util.List)
	 */
	@Override
	public Category findCategoryByKey(List<ODataParameter> keys) {
		for(Category p : categories) {
			if(helper.entityMatchesKeys(p, keys)) {
				return p;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#createCategory(java.util.List)
	 */
	@Override
	public Category createCategory(List<ODataParameter> values) {
		Category category = new Category();
		helper.setFieldsValueFromEntity(category, values);
		categories.add(category);
		return category;
	}

	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#deleteCategory(java.util.List)
	 */
	@Override
	public void deleteCategory(List<ODataParameter>  keys) {
		Category p = findCategoryByKey(keys);
		if(p != null) {
			categories.remove(p);
		}
	}

	/* (non-Javadoc)
	 * @see odata4fx.demo.IProductStoreService#updateCategory(java.util.List)
	 */
	@Override
	public Category updateCategory(List<ODataParameter> values) {
		return null;
	}


	
	

}

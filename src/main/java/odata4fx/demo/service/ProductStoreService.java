package odata4fx.demo.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import odata4fx.core.ODataEntityHelper;
import odata4fx.core.ODataParameter;
import odata4fx.demo.entities.Category;
import odata4fx.demo.entities.Product;

	

@Transactional
public class ProductStoreService implements IProductStoreService  {
	
		
	
	private ODataEntityHelper helper				= new ODataEntityHelper();
	
	
	@Autowired
	EntityManager entityManager;
	
	// Product section
	
	@Override
	public List<Product> listAllProducts(List<ODataParameter> params) {
		ODataParameter skip 		  = ODataParameter.getSkipOption(params);
		ODataParameter top  		  = ODataParameter.getTopOption(params);
		List<ODataParameter> ordering = ODataParameter.getOrderBy(params);
		String filterExpression		  = ODataParameter.getFilters(params);
		StringBuffer sql			  = new StringBuffer("SELECT p from Product p ");
		boolean	firstOrdering		  = true;
		for(ODataParameter orderClause : ordering) {
			if(firstOrdering) {
				firstOrdering = false;
				sql.append("ORDER BY ");	
			}
			sql.append(orderClause.getOrderByProperty()).append(" ").append(orderClause.isDescending()?" DESC ": " ASC");
		}
		
		Query query = entityManager.createQuery(sql.toString(), Product.class);
		if(skip != null) {
			query.setFirstResult(skip.getSkipValue());
		}
		if(top != null) {
			query.setMaxResults(top.getTopValue());
		}
		return query.getResultList();
	}
	
		
	@Override
	public Product findProductByKey(List<ODataParameter>  keys) {
		StringBuffer sql			  = new StringBuffer("SELECT p from Product p WHERE ");
		for(ODataParameter k : keys) {
			sql.append(k.propertyName).append(" = ").append(" :"+k.propertyName);
		}
		Query query = entityManager.createQuery(sql.toString(), Product.class);
		for(ODataParameter k : keys) {
			query.setParameter(k.propertyName,k.value);
		}
		Product result = (Product) query.getSingleResult();  
		return result;
	}

	
	@Override
	public Product createProduct(List<ODataParameter> values) {
		Product product = new Product();
		helper.setFieldsValueFromEntity(product, values);
		product.ID = null;
		entityManager.persist(product);
		entityManager.flush();
		return product;
	}
	
		
	@Override
	public void deleteProduct(List<ODataParameter> keys) {
		Product target = new Product();
		helper.setFieldsValueFromEntity(target, keys);
		target = entityManager.find(Product.class, target.ID);
		entityManager.remove(target);
		entityManager.flush();
	}
	

	@Override
	public Product updateProduct(List<ODataParameter> params) {
		Product target = new Product();
		helper.setFieldsValueFromEntity(target, params);
		target = entityManager.find(Product.class, target.ID);
		helper.setFieldsValueFromEntity(target, params);
		entityManager.persist(target);
		entityManager.flush();
		return target;
	}

	// Category
	

	@Override
	public List<Category> listAllCategories(List<ODataParameter> params) {
		ODataParameter skip 		  = ODataParameter.getSkipOption(params);
		ODataParameter top  		  = ODataParameter.getTopOption(params);
		List<ODataParameter> ordering = ODataParameter.getOrderBy(params);
		String filterExpression		  = ODataParameter.getFilters(params);
		StringBuffer sql			  = new StringBuffer("SELECT c from Category c ");
		boolean	firstOrdering		  = true;
		for(ODataParameter orderClause : ordering) {
			if(firstOrdering) {
				firstOrdering = false;
				sql.append("ORDER BY ");	
			}
			sql.append(orderClause.getOrderByProperty()).append(" ").append(orderClause.isDescending()?" DESC ": " ASC");
		}
		
		Query query = entityManager.createQuery(sql.toString(), Product.class);
		if(skip != null) {
			query.setFirstResult(skip.getSkipValue());
		}
		if(top != null) {
			query.setMaxResults(top.getTopValue());
		}
		return query.getResultList();
	}

	
	
	@Override
	public Category findCategoryByKey(List<ODataParameter> keys) {
		StringBuffer sql			  = new StringBuffer("SELECT c from Category c WHERE ");
		for(ODataParameter k : keys) {
			sql.append(k.propertyName).append(" = ").append(" :"+k.propertyName);
		}
		Query query = entityManager.createQuery(sql.toString(), Category.class);
		for(ODataParameter k : keys) {
			query.setParameter(k.propertyName,k.value);
		}
		Category result = (Category) query.getSingleResult();  
		return result;
	}

	@Override
	public Category createCategory(List<ODataParameter> values) {
		Category category = new Category();
		helper.setFieldsValueFromEntity(category, values);
		category.categoryID = null;
		entityManager.persist(category);
		entityManager.flush();
		return category;
	}

	@Override
	public void deleteCategory(List<ODataParameter>  keys) {
		Category target = new Category();
		helper.setFieldsValueFromEntity(target, keys);
		target = entityManager.find(Category.class, target.categoryID);
		entityManager.remove(target);
		entityManager.flush();
	}

	
	@Override
	public Category updateCategory(List<ODataParameter> values) {
		Category target = new Category();
		helper.setFieldsValueFromEntity(target, values);
		target = entityManager.find(Category.class, target.categoryID);
		helper.setFieldsValueFromEntity(target, values);
		entityManager.persist(target);
		entityManager.flush();
		return target;
	}


	/// Intra relationship
	
	@Override
	public Category getAssociatedCategory(Product item, List<ODataParameter> params) {
		return item.category;
	}
	
	@Override
	public List<Product> getAssociatedProducts(Category item, List<ODataParameter> params) {
		List<Product> result = new ArrayList<Product>();
		/*for(Product p : item.products) {
			if(helper.entityMatchesKeys(p, params)) {
				result.add(p);
			}
		}*/
		return result;
	}
	
	

}

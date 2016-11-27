package mclaudio76.odataspring.demo;

import java.util.ArrayList;
import java.util.List;

import mclaudio76.odataspring.core.IODataService;
import mclaudio76.odataspring.core.ODataEntityHelper;
import mclaudio76.odataspring.core.ODataParamValue;

public class ProductService implements IODataService<Product> {
	
	private ArrayList<Product> products = new ArrayList<>();
	private ODataEntityHelper helper	= new ODataEntityHelper();
	
	public ProductService() {
		products.add(new Product(1, "Alfa  A1", "Racing car"));
		products.add(new Product(2, "Beta  B1", "Luxury car"));
		products.add(new Product(3, "Gamma G3", "Speedy car"));
		products.add(new Product(4, "Delta D4", "City car"));
	}
	
	@Override
	public List<Product> listAll() {
		return products;
	}

	@Override
	public Product findByKey(ODataParamValue ... keys) {
		for(Product p : products) {
			if(helper.entityMatchesKeys(p, keys)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Product create(ODataParamValue... values) {
		Product product = new Product();
		helper.setFieldsValue(product, values);
		products.add(product);
		return product;
	} 

	@Override
	public void delete(ODataParamValue... keys) {
		Product p = findByKey(keys);
		if(p != null) {
			products.remove(p);
		}
	}

	@Override
	public Product update(Product target, ODataParamValue... values) {
		if(target != null) {
			helper.setFieldsValue(target, values);
		}
		return target;
	}

	@Override
	public Class<Product> getEntityClass() {
		return Product.class;
	}

}

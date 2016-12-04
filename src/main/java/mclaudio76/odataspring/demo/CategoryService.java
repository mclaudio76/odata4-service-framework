package mclaudio76.odataspring.demo;

import java.util.ArrayList;
import java.util.List;

import mclaudio76.odataspring.core.IODataService;
import mclaudio76.odataspring.core.ODataEntityHelper;
import mclaudio76.odataspring.core.ODataParamValue;

public class CategoryService implements IODataService<Category> {
	
	private ArrayList<Category> products = new ArrayList<>();
	private ODataEntityHelper helper	= new ODataEntityHelper();
	
	public CategoryService() {
		
	}
	
	@Override
	public List<Category> listAll() {
		return products;
	}

	@Override
	public Category findByKey(ODataParamValue ... keys) {
		return null;
	}

	@Override
	public Category create(ODataParamValue... values) {
		Category product = new Category();
		helper.setFieldsValueFromEntity(product, values);
		products.add(product);
		return product;
	}

	@Override
	public void delete(ODataParamValue... keys) {
		Category p = findByKey(keys);
		if(p != null) {
			products.remove(p);
		}
	}

	@Override
	public Category update(ODataParamValue... values) {
		return null;
	}

	@Override
	public Class<Category> getEntityClass() {
		return Category.class;
	}

}

package mclaudio76.odataspring.demo;

import java.util.ArrayList;
import java.util.List;

import mclaudio76.odataspring.core.IODataService;
import mclaudio76.odataspring.core.ODataEntityHelper;
import mclaudio76.odataspring.core.ODataParamValue;

public class CategoryService implements IODataService<Category> {
	
	private ArrayList<Category> categories = new ArrayList<>();
	private ODataEntityHelper helper	= new ODataEntityHelper();
	
	public CategoryService() {
		
	}
	
	@Override
	public List<Category> listAll() {
		return categories;
	}

	@Override
	public Category findByKey(ODataParamValue ... keys) {
		for(Category p : categories) {
			if(helper.entityMatchesKeys(p, keys)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public Category create(ODataParamValue... values) {
		Category category = new Category();
		helper.setFieldsValueFromEntity(category, values);
		categories.add(category);
		return category;
	}

	@Override
	public void delete(ODataParamValue... keys) {
		Category p = findByKey(keys);
		if(p != null) {
			categories.remove(p);
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

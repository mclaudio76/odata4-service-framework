package odata4fx.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

public class ODataParameter {
	public Object value;
	public String propertyName 			= "";
	private boolean isSystemQueryOption = false;
	
	private boolean isTop				= false;
	private int 	topValue			= 0;
	private boolean isSkip				= false;
	private int     skipValue			= 0;
	
	
	private boolean isOrderBy			= false;
	private String  orderByProperty		= "";
	private boolean valid				= true;
	private boolean descending			= false;
	
	
	public ODataParameter(UriParameter param) {
		this.propertyName  = param.getName();
		this.value		   = param.getText();
		this.isSystemQueryOption = false;
	}
	
	public ODataParameter(Property paramProperty) {
		this.propertyName = paramProperty.getName();
		this.value		  = paramProperty.getValue();
		this.isSystemQueryOption = false;
	}
	
	public ODataParameter(SystemQueryOption option) {
		this.isSystemQueryOption = true;
		if (option instanceof TopOption) {
			isTop  = true;
			TopOption top = (TopOption) option;
			topValue	  =  top.getValue();
		}
		if (option instanceof SkipOption) {
			isSkip		    = true;
			SkipOption skip = (SkipOption) option;
			skipValue	    =  skip.getValue();
		}
	}
	
	public ODataParameter(OrderByItem ordering) {
		isOrderBy 			= true;
		valid	  			= false;
		isSystemQueryOption = true;
		if (ordering.getExpression() instanceof Member) {
			Member mbr = (Member) ordering.getExpression();
			
			UriResource resource = mbr.getResourcePath().getUriResourceParts().get(0);
			if(resource instanceof  UriResourcePrimitiveProperty) {
				UriResourcePrimitiveProperty property = (UriResourcePrimitiveProperty)resource;
				EdmProperty prop 	 = property.getProperty();
				String value		 = property.getSegmentValue();
				this.orderByProperty = prop.getName();
				this.valid 			 = true;
				this.descending		 = ordering.isDescending();
				
			}
		}
	}
	
	public static List<ODataParameter> systemQueryOptions(List<ODataParameter> lst) {
		ArrayList<ODataParameter> pms = new ArrayList<>();
		for(ODataParameter param : lst) {
			if(param.isSystemQueryOption) {
				pms.add(param);
			}
		}
		return pms;
	}
	
	public static ODataParameter getSkipOption(List<ODataParameter> lst) {
		for(ODataParameter param : lst) {
			if(param.isSkip) {
				return param;
			}
		}
		return null;
	}

	public static ODataParameter getTopOption(List<ODataParameter> lst) {
		for(ODataParameter param : lst) {
			if(param.isTop) {
				return param;
			}
		}
		return null;
	}

	public static List<ODataParameter> getOrderBy(List<ODataParameter> lst) {
		ArrayList<ODataParameter> pms = new ArrayList<>();
		for(ODataParameter param : lst) {
			if(param.isOrderBy) {
				pms.add(param);
			}
		}
		return pms;
	}

	public boolean isSystemQueryOption() {
		return isSystemQueryOption;
	}

	public void setSystemQueryOption(boolean isSystemQueryOption) {
		this.isSystemQueryOption = isSystemQueryOption;
	}

	public boolean isTop() {
		return isTop;
	}

	public void setTop(boolean isTop) {
		this.isTop = isTop;
	}

	public int getTopValue() {
		return topValue;
	}

	public void setTopValue(int topValue) {
		this.topValue = topValue;
	}

	public boolean isSkip() {
		return isSkip;
	}

	public void setSkip(boolean isSkip) {
		this.isSkip = isSkip;
	}

	public int getSkipValue() {
		return skipValue;
	}

	public void setSkipValue(int skipValue) {
		this.skipValue = skipValue;
	}

	public boolean isValid() {
		return valid;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public boolean isOrderBy() {
		return isOrderBy;
	}
	
	public String getOrderByProperty() {
		return orderByProperty;
	}

	public boolean isDescending() {
		return descending;
	}

	
	
	
	
	
}

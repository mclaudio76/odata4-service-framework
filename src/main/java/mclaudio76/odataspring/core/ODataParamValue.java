package mclaudio76.odataspring.core;

import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.server.api.uri.UriParameter;

public class ODataParamValue {
	public Object value;
	public String propertyName = "";
	
	public ODataParamValue(UriParameter param) {
		this.propertyName  = param.getName();
		this.value		   = param.getText();
	}
	
	public ODataParamValue(Property paramProperty) {
		this.propertyName = paramProperty.getName();
		this.value		  = paramProperty.getValue();
	}
	
}

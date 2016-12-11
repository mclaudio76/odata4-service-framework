package mclaudio76.odata4fx.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

public class ODataParameter {
	public Object value;
	public String propertyName 			= "";
	private boolean isSystemQueryOption = false;
	
	private boolean isTop				= false;
	private int 	topValue			= 0;
	private boolean isSkip				= false;
	private int     skipValue			= 0;
	
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
	
	
	
	
	
}

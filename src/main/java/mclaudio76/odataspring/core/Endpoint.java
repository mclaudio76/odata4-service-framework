package mclaudio76.odataspring.core;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;


public class Endpoint<T> extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String nameSpace = "";
	private AbstractODataService<T> businessService = null;
	private Class<T> clz = null;
	
	
	public Endpoint(String nameSpace, IODataService<T> actualBusinessEndPoint) {
		super();
		this.nameSpace 		 = nameSpace;
		this.businessService = new AbstractODataService<>(actualBusinessEndPoint);
		this.clz			 =  actualBusinessEndPoint.getEntityClass();
	}
	
	
	public void process(HttpServletRequest req, HttpServletResponse response) {
		try {
			GenericEDMProvider provider = new GenericEDMProvider(nameSpace, "CONTAINER_"+nameSpace, clz);
			OData odata = OData.newInstance();
	        ServiceMetadata edm = odata.createServiceMetadata(provider, new ArrayList<EdmxReference>());
	        ODataHttpHandler handler = odata.createHandler(edm);
	        handler.register(businessService);
	        handler.process(req, response);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}
	
	protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}
}

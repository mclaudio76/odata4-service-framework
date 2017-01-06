package odata4fx.core;

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
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;

//@Service	
public class Endpoint extends HttpServlet {
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	protected String nameSpace = "";
	protected ArrayList<Class<?>> publishedClasses = new ArrayList<>();
	
	private ODataEntityHelper odataEntityHelper = new ODataEntityHelper();
	
	public Endpoint(String nameSpace) {
		super();
		this.nameSpace 		 = nameSpace;
	}
	
	
	protected ODataEntityHelper getODataEntityHelper() {
		return odataEntityHelper;
	}
	
	public Endpoint addEntity(Class<?> clz) {
		if(!publishedClasses.contains(clz)) {
			System.out.println("Registered class >>"+clz.getName());
			publishedClasses.add(clz);
		}
		return this;
	}
			
	
	public void process(HttpServletRequest req, HttpServletResponse response) {
		try {
			GenericEDMProvider provider = new GenericEDMProvider(nameSpace, "CONTAINER_"+nameSpace);
			for(Class<?> clz : publishedClasses) {
				provider.addEntity(clz);
			}
			OData odata = OData.newInstance();
	        ServiceMetadata edm = odata.createServiceMetadata(provider, new ArrayList<EdmxReference>());
	        ODataHttpHandler handler = odata.createHandler(edm);
	        handler.register(new ODataServiceHandler(provider,getODataEntityHelper()));
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
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}
}

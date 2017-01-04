package odata4fx.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import odata4fx.demo.service.IProductStoreService;

@RestController
public class TestController {

	@Autowired
	IProductStoreService service;
	
	@RequestMapping("/help")
    String test() {
        try {
        	service.testSave();
        	return "Completed";
        }
        catch(Exception e) {
        	e.printStackTrace(System.err);
        	return "KO";
        }
    }
	
}

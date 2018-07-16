package app.controller;

import net.rails.ext.AbsGlobal; 
import net.rails.support.Support; 
import net.rails.web.Controller;
import net.rails.web.Route;

import java.io.IOException;
 
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import app.global.Global;
import java.io.File;
import app.model.Account;
import java.util.Iterator;
import org.quartz.commonj.WorkManagerThreadExecutor;

public class TestController extends Controller {        
	
	protected AbsGlobal  g; 

	public TestController(FilterConfig config,HttpServletRequest request, HttpServletResponse response,Route route) throws Exception {
		super(config, request, response,route);
		g = new Global(this);
		log.debug(Support.config().getLocales().loadYmls() + "");
		request.setAttribute("g", g);
	}
	
	public void indexAction() throws IOException, ServletException{
		text(params.get("errmsg") + "");
	}
	
	@Override
	protected String encoding(String param){    
		if(param == null)
			return null;
		
		return super.encoding(param.trim());
	}

	@Override
	protected AbsGlobal getGlobal() {
		// TODO Auto-generated method stub
		return g;
	}

}
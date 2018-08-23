package app.controller;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.rails.ext.AbsGlobal;
import net.rails.web.Controller;
import net.rails.web.Route;
import app.global.Global;
import java.io.IOException;
import javax.servlet.ServletException;
import java.io.File;

public class RootController extends Controller {
    
    protected AbsGlobal  g; 

	public RootController(FilterConfig config, HttpServletRequest request, HttpServletResponse response, Route route)
			throws Exception {
		super(config, request, response, route);
		g = new Global(this);
		request.setAttribute("g", g);		
	}

	public void indexAction() throws IOException, ServletException {
		toHtmlTpl(); 
	}
	
	public void downloadAction() throws IOException, ServletException {
	    file(new File("/src/data/test.zip"),"test.zip");
	}

	@Override
	protected AbsGlobal getGlobal() {
		return g;
	}

}
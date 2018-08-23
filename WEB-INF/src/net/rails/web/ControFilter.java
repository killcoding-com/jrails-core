package net.rails.web;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.rails.ext.IndexMap;
import net.rails.log.LogPoint;
import net.rails.support.Support;

@WebFilter(
		urlPatterns="*",
		displayName="ControFilter",
		dispatcherTypes={javax.servlet.DispatcherType.REQUEST,javax.servlet.DispatcherType.FORWARD}
		)
public final class ControFilter implements Filter {

	private final static Logger log = LoggerFactory.getLogger(ControFilter.class);
	private FilterConfig config;

	public ControFilter() {
		super();
	}

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}
	
	private void logInfo(HttpServletRequest request){
		LogPoint.markWebUrl();
		if(log.isDebugEnabled()){
			log.debug("Remote Addr: {}",request.getRemoteAddr());
			log.debug("X-Real-IP: {}",request.getHeader("X-Real-IP"));
			String qs = request.getQueryString();
			StringBuffer url = new StringBuffer("Request URL: ");
			url.append(request.getRequestURL());
			if(qs != null){
				url.append("?");
				url.append(request.getQueryString());
			}
			log.debug("{}",url);
		}
		LogPoint.unmark();
	}

	@SuppressWarnings({ "unchecked"})
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		request.setCharacterEncoding(Support.config().env().getApplicationCharset());
		Map<String,Map<String,Object>> domainRoute = (Map<String, Map<String, Object>>) Route.ROUTE.get("domain_route");
		if(domainRoute == null){
			domainRoute = new HashMap<String,Map<String,Object>>();
		}
		Route route = null;
		if (request != null) {
			try {
				String serPath = new String(encoding(request.getServletPath()));				
				serPath = new String(serPath.replaceFirst("^/", ""));
				Pattern p = null;
				Matcher m = null;

				p = Pattern.compile("(^[\\w_-]+[\\.]{1}[\\w]+$)");
				m = p.matcher(serPath);
				if (m.find()){
					logInfo(request);
					chain.doFilter(request, response);
					return;
				}				
				p = Pattern.compile("(^[\\w_-]+(?=/){0,1})");
				m = p.matcher(serPath);
				String c = "";
				if (m.find())
					c = m.group();
				
				if(c.equals("public") || c.equals("WEB-INF")){
					logInfo(request);
					chain.doFilter(request, response);
					return;
				}else if(domainRoute.containsKey(request.getServerName()) && serPath.equals("")){
					Map<String,Object> domain = domainRoute.get(request.getServerName());
					if(domain != null){
						route = new Route((String)domain.get("controller"),(String)domain.get("action"));
						route.putRouteParams(request, (Map<String, Object>) domain.get("params"));
					}					
				}else if(c.equals("root") || c.equals("")){
					logInfo(request);
					putId(request,m.replaceFirst("").replaceFirst("^/", ""));	
					route = new Route(request,"root");
				}else if(c.matches("[A-Z]+\\w+")){
					logInfo(request);
					String a = "";
					serPath = m.replaceFirst("");
					p = Pattern.compile("^/\\w+/{0,1}");
					m = p.matcher(serPath);
					if(m.find()){
						a = m.group().replaceFirst("^/", "").replaceFirst("/$", "");					
						putId(request,m.replaceFirst("").replaceFirst("^/", ""));	
						route = new Route(c,a);	
					}else{
						route = new Route(c,"index");	
					}
				}else if(c.matches("[a-z]+\\w+")){
					logInfo(request);
					putId(request,m.replaceFirst("").replaceFirst("^/", ""));		
					route = new Route(request,c);
				}else{
					logInfo(request);
					throw new PathException("Route (404): " + c);
				}
				
				if(route.getController() == null){
					throw new PathException("Controller (404)");
				}else{				
					execute(route, request, response,
							(Class<? extends Controller>) Class
									.forName(MessageFormat.format(
											"app.controller.{0}Controller",
											route.getController())));
				}
			} catch (Exception e) {
				if(e instanceof ClassNotFoundException){
					log.warn("Controller (404): {}/{}",route.getController() ,route.getAction(), e);
					sendError(response,404);
					route.setActive(false);
				}else if(e instanceof PathException){
					log.warn(e.getMessage(), e);
					sendError(response,404);
					route.setActive(false);
				}else {
					log.warn(e.getMessage(),e);
					sendError(response, 500);
					route.setActive(false);
				}
				return;
			}
		}
	}

	private void putId(HttpServletRequest request, String id){
		if(id != null && !id.equals("")){
			Map<String, String> arg = new IndexMap<String, String>();
			arg.put("id", id);
			arg.put("_ARGS", id);
			request.setAttribute(Route.ROUTE_PARAMS, arg);
		}
	}
	
	protected String encoding(String s) throws Exception{
		String rc = Support.config().env().getServerCharset();
		String tc = Support.config().env().getApplicationCharset();
		if(rc != null && tc != null && !rc.equals(tc))
			return new String(s.getBytes(rc),tc);
		
		return s;
	}

	@SuppressWarnings("unchecked")
	private void execute(Route route, HttpServletRequest request,
			HttpServletResponse response, Class<? extends Controller> c) throws Exception {
		Controller contro = null;
		try {
			final Constructor<?> constructor = c.getConstructor(
					FilterConfig.class, HttpServletRequest.class,
					HttpServletResponse.class, Route.class);
			contro = (Controller) constructor.newInstance(config, request,
					response, route);
			if (contro != null && contro.getRoute().isActive()) {
				List<String> names = new ArrayList<String>();
				for(Method m : c.getMethods()){
					names.add(m.getName());
				}
				Method mtd = null;
				String req = Support.inflect(request.getMethod().toLowerCase()).camelcase();
				String act = route.getAction() + "Action";				
				if(names.contains(act)){
					mtd = c.getDeclaredMethod(act);
					mtd.invoke(contro);
					if(contro.getRoute().isActive()){						
						act = route.getAction() + req;
						if(names.contains(act)){
							mtd = c.getDeclaredMethod(act);
							mtd.invoke(contro);
							if(contro.getRoute().isActive())
								contro.render();
						}
						else{
							if(contro.getRoute().isActive())
								contro.render();
						}
					}
				}else{
					act = route.getAction() + req;
					if(names.contains(act)){
						mtd = c.getDeclaredMethod(act);
						mtd.invoke(contro);
						if(contro.getRoute().isActive())
							contro.render();
					}else{
						throw new PathException("Action (404): " + route.getController() + "/" + route.getAction());
					}
				}
			}
			return;
		} catch (Exception e) {
			if (e instanceof NoSuchMethodException
					&& contro instanceof Controller) {
				execute(route, request, response,
						(Class<? extends Controller>) c.getSuperclass());
			}else{
				throw e;
			}
		}
	}

	private void sendError(HttpServletResponse response, int error) {
		try {
			response.sendError(error);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			return;
		}
	}

	public void destroy() {
		
	}

}

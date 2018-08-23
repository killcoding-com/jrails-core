package net.rails.web;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.rails.active_record.ActiveRecord;
import net.rails.ext.AbsGlobal;
import net.rails.ext.IndexMap;
import net.rails.ext.Json;
import net.rails.log.LogPoint;
import net.rails.support.Support;
import net.rails.support.worker.TokenWorker;
import net.rails.support.worker.UserAgentWorker;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.OutputStream;
import net.rails.tpl.Tpl;
import net.rails.tpl.TplText;
import net.rails.sql.query.Query;
import net.rails.Define;

@SuppressWarnings("unchecked")
public abstract class Controller {

	private String name;
	protected FilterConfig config;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected String serverPath;
	protected String contextPath;
	protected String action;
	protected UserAgentWorker userAgent;
	protected boolean isPost;
	protected boolean ajax = false;
	protected HttpSession session;
	protected Route route;
	protected ServletFileUpload upload;
	protected abstract AbsGlobal getGlobal();
	protected final Map<String, String> headers = new IndexMap<String, String>();
	protected final Map<String, String> cookies = new IndexMap<String, String>();
	protected final Map<String, Object> params = new IndexMap<String, Object>();
	protected final Map<String, Object> queies = new IndexMap<String, Object>();
	
	protected Logger log;

	public Controller(FilterConfig config, HttpServletRequest request,
			HttpServletResponse response, Route route) throws Exception {
		super();
		log = LoggerFactory.getLogger(getClass());
		userAgent = Support.userAgent(request.getHeader("user-agent"));
		this.config = config;
		this.request = request;
		this.response = response;
		this.session = request.getSession();
		this.route = route;

		ajax = request.getHeader("X-Requested-With") != null;
		final Enumeration<String> ens = request.getHeaderNames();
		Map<String, Object> qs = QueryString.parse(request.getQueryString());
		if (qs != null)
			queies.putAll(qs);

		String en = null;
		if (ens != null) {
			while ((en = ens.nextElement()) != null) {
				headers.put(en, request.getHeader(en));
			}
		}
		Cookie[] cks = request.getCookies();
		if (cks != null) {
			for (Cookie ck : cks) {
				cookies.put(ck.getName(), ck.getValue());
			}
		}
		if (route.getParams() != null)
			params.putAll(route.getParams());

		if (request.getAttribute(Route.ROUTE_PARAMS) != null)
			params.putAll((Map<String, Object>) request
					.getAttribute(Route.ROUTE_PARAMS));

		serverPath = request.getServletPath();
		contextPath = request.getContextPath();
		name = route.getController();
		action = route.getAction();
		isPost = this.request.getMethod().toUpperCase().equals("POST") ? true
				: false;

		if (ServletFileUpload.isMultipartContent(request)) {
			upload = new ServletFileUpload();
			parseDataParams();
		} else {
			parseParams(request.getParameterMap());
		}
		
		if (log.isDebugEnabled()) {
			LogPoint.markWebHeader();
			log.debug("Headers: {}",headers);
			LogPoint.isMarkWebUserAgent();
    		log.debug("UA: {}",request.getHeader("user-agent"));
    		log.debug("UA Mobile: {}",userAgent.isMobile());
            log.debug("UA Brower family: {}",userAgent.browser().getFamily());
            log.debug("UA Brower group: {}",userAgent.browser().getGroup());
            log.debug("UA Brower name: {}",userAgent.browser().getName());
            log.debug("UA Brower version: {}",userAgent.browser().getVersion());
            
            log.debug("UA OS family: {}",userAgent.os().getFamily());
            log.debug("UA OS group: {}",userAgent.os().getGroup());
            log.debug("UA OS name: {}",userAgent.os().getName());
            log.debug("UA OS version: {}",userAgent.os().getVersion());
            
            log.debug("UA Engine family: {}",userAgent.engine().getFamily());
            log.debug("UA Engine group: {}",userAgent.engine().getGroup());
            log.debug("UA Engine name: {}",userAgent.engine().getName());
            log.debug("UA Engine version: {}",userAgent.engine().getVersion());
			LogPoint.markWebCookie();
			log.debug("Cookies: {}",cookies);
			LogPoint.markWebParams();
			log.debug("Request Params: {}",params);
			LogPoint.unmark();
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Controller: {}",route.getController());
			log.debug("Action: {}",route.getAction());
			log.debug("Method: {}",request.getMethod());
			log.debug("Ajax: {}",ajax);
			log.debug("Request Queies: {}",queies);
		}
	}

	private void parseFormField(String fieldName, FileItemStream item)
			throws IOException {
		InputStream stream = item.openStream();
		String value = Streams.asString(stream, AbsGlobal.getApplicationCharset());
		Matcher m = Pattern.compile("\\[\\]$").matcher(fieldName);
		boolean keyarr = m.find();
		String key = null;
		if (keyarr)
			key = m.replaceFirst("");
		else
			key = fieldName;
		if (params.containsKey(key)) {
			Object obj = params.get(key);
			List<String> list = null;
			if (obj instanceof List) {
				list = (List<String>) obj;
			} else {
				list = new ArrayList<String>();
				list.add((String) obj);
			}
			list.add(value);
			params.put(key, list);
		} else {
			if (keyarr) {
				List<String> list = new ArrayList<String>();
				list.add((String) value);
				params.put(key, list);
			} else
				params.put(key, value);
		}
	}

	private void parseFileField(String fieldName, String fileName,
			FileItemStream item) throws IOException {
		InputStream value = item.openStream();
		Matcher m = Pattern.compile("\\[\\]$").matcher(fieldName);
		boolean keyarr = m.find();
		String key = null;
		if (keyarr)
			key = m.replaceFirst("");
		else
			key = fieldName;
		if (params.containsKey(key)) {
			Object obj = params.get(key);
			List<ClientFile> list = null;
			if (obj instanceof List) {
				list = (List<ClientFile>) obj;
			} else {
				list = new ArrayList<ClientFile>();
				list.add((ClientFile) obj);
			}
			list.add(new ClientFile(fileName, value));
			params.put(key, list);
		} else {
			if (keyarr) {
				List<ClientFile> list = new ArrayList<ClientFile>();
				if (fileName != null && !fileName.equals(""))
					list.add(new ClientFile(fileName, value));
				else
					list.add(null);

				params.put(key, list);
			} else {
				if (fileName != null && !fileName.equals(""))
					params.put(key, new ClientFile(fileName, value));
				else
					params.put(key, null);
			}
		}
	}
	
	private void parseParams(Map<String, String[]> paramsMap)
			throws UnsupportedEncodingException {
		final List<String> keys = Support.map(paramsMap).keys();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			Matcher m = Pattern.compile("\\[\\]$").matcher(key);
			boolean keyarr = m.find();
			if (keyarr)
				params.put(m.replaceFirst(""),
						parseStringArray(keyarr, paramsMap.get(key)));
			else
				params.put(key, parseStringArray(keyarr, paramsMap.get(key)));
		}
	}

	protected void parseDataParams() throws Exception {
		final FileItemIterator iter = upload.getItemIterator(request);
		upload.setHeaderEncoding(request.getCharacterEncoding());
		while (iter.hasNext()) {
			FileItemStream item = iter.next();
			String fieldName = item.getFieldName();
			if (item.isFormField()) {
				parseFormField(fieldName, item);
			} else {
				parseFileField(fieldName, item.getName(), item);
			}
		}
	}
	
	protected String encoding(String param) {
		if (isPost){
			return param;
		}else{
			try {
				return new String(param.getBytes(AbsGlobal.getServerCharset()), AbsGlobal.getApplicationCharset());
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage(), e);
			}
			return param;
		}
	}

	public Route getRoute() {
		return route;
	}

	public UserAgentWorker getUserAgent() {
		return userAgent;
	}
	
	public void file(InputStream is, String filename,String contentType) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
            OutputStream os = null;
			try{
				String charset = Support.config().env().getApplicationCharset();
				filename = URLEncoder.encode(filename, charset);
				filename = new String(filename.getBytes(charset), "ISO-8859-1");
				TokenWorker token = userAgent.browser();
				String disp = "attachment; filename*=\"" + filename + "\"";
				if (token.getGroup() == null) {
				    disp = "attachment; filename=\"" + filename + "\"";
				} else if (token.getGroup().equals("MSIE")) {
					disp = "attachment; filename=\"" + filename + "\"";
				}else if (token.getGroup().equals("Safari")) {
					disp = "attachment; filename=\"" + filename + "\"";
				}else if (token.getGroup().equals("Trident")) {
					disp = "attachment; filename=\"" + filename + "\"";
				}else if (token.getGroup().equals("Chrome")) {
					disp = "attachment; filename=\"" + filename + "\"";
				} else {
					disp = "attachment; filename=*\"" + filename + "\"";
				}
				response.addHeader("Content-Disposition", disp);
				response.setContentType(contentType);
				
				os = response.getOutputStream();
				byte[] buff = new byte[1024];
				int bytesRead;
				while (-1 != (bytesRead = is.read(buff, 0, buff.length))) {
					os.write(buff, 0, bytesRead);
					os.flush(); 
				}
			}finally{
				is.close();
				if(os != null){
					os.close();
				}
			}
		}
	}
	
	public void file(InputStream is, String filename) throws IOException {
		file(is,filename,"application/x-msdownload");
	}

	public void file(byte[] data, String filename) throws IOException {
		file(new ByteArrayInputStream(data), filename);
	}
	
	public void file(byte[] data, String filename,String contentType) throws IOException {
		file(new ByteArrayInputStream(data), filename,contentType);
	}

	public void file(File file, String filename) throws IOException {
		file(FileUtils.readFileToByteArray(file), filename);
	}
	
	public void file(File file, String filename,String contentType) throws IOException {
		file(FileUtils.readFileToByteArray(file), filename,contentType);
	}

	public void file(File file) throws IOException {
		file(file, file.getName());
	}
	
	public void forwardRoute(String route) throws IOException, ServletException {
		Route r = new Route(request, route);
		forward(r.getController(), r.getAction());
	}

	public void redirectRoute(String route, QueryString qs) throws IOException {
		Route r = new Route(request, route);
		QueryString q = new QueryString();
		if (r.getParams() != null)
			q.putAll(r.getParams());
		if (qs != null)
			q.putAll(qs);
		redirect(r.getController(), r.getAction(), q);
	}

	public void render() throws IOException, ServletException {
		render(name, action);
	}

	public void render(String path, String action) throws IOException,
			ServletException {
		if (route.isActive()) {
			route.setActive(false);
			final String view = MessageFormat.format(
					"/WEB-INF/view/{0}/{1}.jsp", path, action);
			request.getRequestDispatcher(view).forward(request, response);
			return;
		}
	}

	public void forward(String controller, String action) throws IOException,
			ServletException {
		if (route.isActive()) {
			route.setActive(false);
			request.getRequestDispatcher(
					MessageFormat.format("/{0}/{1}/", controller, action))
					.forward(request, response);
			return;
		}
	}

	public void forward(String action) throws IOException, ServletException {
		forward(name, action);
	}

	public void redirect(String controller, String action, QueryString qs)
			throws IOException {
		String qstr = "";
		if (!Support.object(qs).blank())
			qstr = "?" + qs.toQueryString();

		redirect(MessageFormat.format("{0}/{1}/{2}{3}", contextPath,
				controller, action, qstr));
	}

	public void redirect(String action, QueryString qs) throws IOException {
		redirect(name, action, qs);
	}

	public void redirect(String url) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.sendRedirect(url);
			return;
		}
	}

	public void location(int status, String url) {
		response.setStatus(status);
		response.addHeader("Location", url);
		response.addHeader("Connection", "close");
		route.setActive(false);
	}

	public void location(String url) throws IOException {
		out().write(
				"document.write(\"<script>window.location.href='" + url
						+ "';</script>\");");
	}

	public PrintWriter out() throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.setContentType(MessageFormat.format(
					"text/html; charset={0}", AbsGlobal.getApplicationCharset()));
			return response.getWriter();
		} else
			return null;
	}

	public void set(String key, Object value) {
		request.setAttribute(key, value);
	}

	public void bind(ActiveRecord model) {
		List<String> att = null;
		att = model.getAttributes();
		String key = new String();
		final Map<String, Object> m = new IndexMap<String, Object>();
		for (Iterator<String> iterator = att.iterator(); iterator.hasNext();) {
			key = iterator.next();
			m.put(key, model.get(key));
		}
		set(model.getClass().getSimpleName(), m);
	}

	public void bind(Map<String, Object> params) {
		final List<String> att = Support.map(params).keys();
		String key;
		Map<String, Object> m = new IndexMap<String, Object>();
		for (Iterator<String> iterator = att.iterator(); iterator.hasNext();) {
			String val = iterator.next();
			String[] temp = val.split("\\.");
			key = temp[0];
			if (temp.length == 1)
				set(key, params.get(val));
			else {
				m.put(temp[1], params.get(val));
				set(key, m);
			}
		}
	}

	public boolean isAjax() {
		return ajax;
	}

	public FilterConfig getConfig() {
		return config;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public String getName() {
		return name;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public Map<String, Object> getQueies() {
		return queies;
	}

	public Map<String, Object> form(String name, Map<String, Object> params) {
		final List<String> paramKeys = Support.map(params).keys();
		final Map<String, Object> m = new IndexMap<String, Object>();
		for (Iterator<String> iterator = paramKeys.iterator(); iterator
				.hasNext();) {
			String key = iterator.next();
			String regex = MessageFormat.format("^({0}\\[\\w+\\])", name);
			Pattern p = Pattern.compile(regex);
			Matcher mat = p.matcher(key);
			if (mat.find()) {
				p = Pattern.compile("\\[\\w+\\]");
				mat = p.matcher(key);
				if (mat.find())
					m.put(mat.group().replaceFirst("\\[", "")
							.replaceFirst("\\]", ""), params.get(key));
			}
		}
		return m;
	}

	public Map<String, ? extends Object> form(String name) {
		return form(name, params);
	}

	private Object parseStringArray(boolean keyarr, String[] values) {
		if (keyarr || values.length > 1) {
			List<String> arr = new ArrayList<String>();
			for (int i = 0; i < values.length; i++) {
				arr.add(encoding(values[i]));
			}
			return arr;
		} else {
			return encoding(values[0]);
		}
	}
	
	public List<Object> parseArray(String name,String def){
		Object v = params.get(name);
		if(v instanceof List)
			return (List<Object>)v;
		else
			return parseJsonArray(name,def);
	}
	
	public List<Object> parseArray(String name){
		Object v = params.get(name);
		if(v == null)
			return null;
		if(v instanceof List)
			return (List<Object>)v;
		else
			return parseJsonArray(name);
	}
	
	public List<Object> parseJsonArray(String name){
		return (List<Object>)Json.parse(parseString(name));
	}

	public List<Object> parseJsonArray(String name,String def){
		return (List<Object>)Json.parse(parseString(name,def));
	}
	
	public Map<String,Object> parseJson(String name){
		return (Map<String,Object>)Json.parse(parseString(name));
	}
	
	public Map<String,Object> parseJson(String name,String def){
		return (Map<String,Object>)Json.parse(parseString(name,def));
	}

	public String parseString(String name, String def) {
		return Support.string(parseString(name)).def(def);
	}

	public String parseString(String name) {
		return (String) params.get(name);
	}

	public Number parseNumber(String name, Number def) throws ParseException {
		DecimalFormat df = new DecimalFormat();
		return df.parse(parseString(name, def.toString()));
	}

	public Number parseNumber(String name) throws ParseException {
		return parseNumber(name, null);
	}

	public Boolean parseBoolean(String name, Boolean def) {
		return Boolean.parseBoolean(parseString(name));
	}

	public Boolean parseBoolean(String name) {
		return parseBoolean(name, null);
	}

	public Timestamp parseTimestamp(String name, Timestamp def) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(getGlobal().t("formates",
				"datetime"));
		if (Support.string(parseString(name)).blank())
			return def;

		java.util.Date d = df.parse(parseString(name));
		return new Timestamp(d.getTime());
	}

	public Timestamp parseTimestamp(String name) throws ParseException {
		return parseTimestamp(name, null);
	}

	public Date parseDate(String name, Date def) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(getGlobal().t("formates",
				"date"));
		if (Support.string(parseString(name)).blank()) return def;
		
		java.util.Date d = df.parse(parseString(name));
		return new Date(d.getTime());
	}

	public Date parseDate(String name) throws ParseException {
		return parseDate(name, null);
	}

	public Time parseTime(String name, Time def) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(getGlobal().t("formates",
				"time"));
		if (Support.string(parseString(name)).blank()) return def;

		java.util.Date d = df.parse(parseString(name));
		return new Time(d.getTime());
	}

	public Time parseTime(String name) throws ParseException {
		return parseTime(name, null);
	}

	public void text(String text) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.setContentType(MessageFormat.format(
					"text/html; charset={0}",
					Support.config().env().getApplicationCharset()));
			response.getWriter().write(text);
		}
	}
	
	public void text(String contentType,String text) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.setContentType(MessageFormat.format(
					"{0}; charset={1}",
					contentType,
					Support.config().env().getApplicationCharset()));
			response.getWriter().write(text);
		}
	}
	
	public void json(Json<String,Object> json) throws IOException {
		if (route.isActive()) {
			route.setActive(false);
			response.setContentType("application/x-json");
			response.getWriter().write(json.toString());
		}
	}
	
	public String tpl(String tplFile) throws IOException{
		StringBuffer sbf = new StringBuffer("Route: " + route.getController() + "/" + route.getAction());
		sbf.append("\nTplFile: " + tplFile);
		TplText text = new TplText(sbf.toString(),getGlobal(),tplFile);
		text.params().put("Query", Query.class);
		text.params().put("Support", Support.class);
		text.params().put("Log", LoggerFactory.getLogger(getClass()));
		text.params().put("Json", Json.class);
		text.params().put("g",getGlobal());
		Tpl tpl = new Tpl(getGlobal(),text);
		if(tplFile.endsWith(".js")){
			tpl.setCompressed(true);
			tpl.setDocType(Tpl.DOCTYPE_JS);
		}else if(tplFile.endsWith(".css")){
			tpl.setCompressed(true);
			tpl.setDocType(Tpl.DOCTYPE_CSS);
		}else if(tplFile.endsWith(".html")){
			tpl.setCompressed(true);
			tpl.setDocType(Tpl.DOCTYPE_HTML);
		}else{
			tpl.setCompressed(true);
			tpl.setDocType(Tpl.DOCTYPE_OTHER);
		}		
		return tpl.generate();
	}
	
	public String jsTpl() throws IOException{
		String tplFile = getName() + "/" + getRoute().getAction() + ".tpl.js";
		return tpl(tplFile);
	}
	
	public String htmlTpl() throws IOException{
		String tplFile = getName() + "/" + getRoute().getAction() + ".tpl.html";
		return tpl(tplFile);
	}
	
	public void toHtmlTpl() throws IOException {
	    text(htmlTpl());
	}
	
	public void toJsTpl() throws IOException {
	    text(jsTpl());
	}	

	public void sendError(int code) throws IOException {
		if (route.isActive()) {
			log.debug("SendError: {}",code);
			route.setActive(false);
			response.sendError(code);
			return;
		}
	}

	public void sendError(int code, String text) throws IOException {
		if (route.isActive()) {
			log.debug("SendError: {},{}",code,text);
			route.setActive(false);
			response.sendError(code, text);
			return;
		}
	}

}

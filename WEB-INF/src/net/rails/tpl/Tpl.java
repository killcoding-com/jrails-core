package net.rails.tpl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.rails.Define;
import net.rails.active_record.ActiveRecord;
import net.rails.ext.AbsGlobal;
import net.rails.ext.Json;
import net.rails.support.Support;
import net.rails.support.worker.AbsConfigWorker;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.tools.generic.AlternatorTool;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.IteratorTool;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;
import org.apache.velocity.tools.generic.SortTool;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.htmlcompressor.compressor.Compressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class Tpl implements ReferenceInsertionEventHandler,NullSetEventHandler, MethodExceptionEventHandler {

	public final static String DOCTYPE_JS = "js";
	public final static String DOCTYPE_CSS = "css";
	public final static String DOCTYPE_HTML = "html";
	public final static String DOCTYPE_OTHER = "other";
	
	private AbsGlobal g;
	private static String CHARSET;
	private final VelocityContext context = new VelocityContext();	
	private final StringWriter writer = new StringWriter();
	private TplText text;
	private boolean compressed = false;
	private String docType = DOCTYPE_OTHER;
	
	private final static Logger log = LoggerFactory.getLogger(Tpl.class);

	
	static{
		LogChute logChute = new LogChute(){
			public void init(RuntimeServices rs) {
			}
			public void log(int level, String message) {
					log.debug(message);
			}
			public void log(int level, String message, Throwable t) {
					log.error(message,t);
			}
			public boolean isLevelEnabled(int level) {
				return true;
			}				
		};
		CHARSET = Support.config().env().getApplicationCharset();
		List<String> directives = Arrays.asList(
				//include myself class
//				net.rails.tpl.worker.IfnullWorker.class.getName(),
//				net.rails.tpl.worker.IfnotnullWorker.class.getName()
				);
		Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM,logChute);
		Velocity.setProperty(Velocity.RUNTIME_LOG,logChute);
		Velocity.setProperty("resource.loader", "file");
		Velocity.setProperty("file.resource.loader.path",Define.VIEW_PATH);
		Velocity.setProperty("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		Velocity.setProperty("input.encoding",CHARSET);
		Velocity.setProperty("output.encoding", CHARSET);
		Velocity.setProperty("contentType", "text/html;charset=" + CHARSET);
		if(directives.size() > 0)
			Velocity.setProperty("userdirective",Support.array(directives).join(","));
		
		Velocity.init();
	}

	public Tpl(AbsGlobal g,TplText text) throws IOException {
		super();
		this.g = g;
		this.text = text;
	}	

	public String generate() throws ParseErrorException,
					MethodInvocationException, ResourceNotFoundException {
		text.params().put("null",null);
		text.params().put("context", context);
		text.params().put("Escape",new EscapeTool());
		text.params().put("Number",new NumberTool());
		text.params().put("Date",new DateTool());
		text.params().put("Math",new MathTool());
		text.params().put("Alternator",new AlternatorTool());
		text.params().put("Render",new RenderTool());
		text.params().put("List",new ListTool());
		text.params().put("Sorter",new SortTool());
		text.params().put("Iterator",new IteratorTool());
		final List<String> keys = Support.map(text.params()).keys();
		for(String key : keys){
			context.put(key, text.params().get(key));
		}
		EventCartridge ec = new EventCartridge();
		context.put("this",this);
		ec.addEventHandler(this);
		ec.attachToContext(context);
		Velocity.evaluate(context, writer,text.getName(),text.getText().toString());
		if(!(log.isDebugEnabled() || log.isInfoEnabled()) && compressed){
			return compressor(writer.toString());			
		}else{
			return writer.toString();
		}
	}
	
	public void setCompressed(boolean compressed){
		this.compressed = compressed;
	}
	
	public boolean isCompressed(){
		return this.compressed;
	}
	
	public void setDocType(String docType){
		this.docType = docType;
	}
	
	public String getDocType(){
		return docType;
	}
	
	public VelocityContext getContext(){
		return context;
	}
	
	@Override
	public Object referenceInsert(String reference, Object value) {
		if(value == null){
			String[] res = reference.replaceFirst("^\\$", "").replace("{","").replace("}", "").split("\\.");
			switch(res.length){
				case 1:
					if(res[0].equals("models"))
						value = g.locale("models");
					else if((value = g.locale("models",res[0])) == null) {
						value = g.locale(res[0]);
					}
					break;
				case 2:
					if(res[1].equals("fields")){
						value = ActiveRecord.eval(g,res[0]).getAttributes();
					}else if(res[1].equals("attributes")){
						value = g.locale("attributes",res[0]);
					}else if(res[1].equals("config")){
						String model = res[0];
						Map<String,Object> attrCnfs = Support.config().getModels().get(model);						
						Map<String,Object> attrs = (Map<String, Object>) attrCnfs.get("attributes");
						if(attrs == null){
							attrs = new HashMap<String,Object>();
							log.debug("{}.yml require a key 'attribute'",model);
						}
						List<String> attrsKey = Support.map(attrs).keys();
						Map<String,Object> attrCnf = null;
						for(String attr : attrsKey){
							attrCnf = (Map<String, Object>) attrs.get(attr);	
							if(attrCnf == null)
								continue;
							
							Map<String,Object> valCnfs = Support.map(attrCnf).containsKey("^validates_\\w+_of");
							if(valCnfs == null)
								continue;
							
							List<String> ofs = Support.map(valCnfs).keys();
							if(ofs == null)
								continue;
							
							for(String of : ofs){
								Map<String,Object> valCnf = (Map<String, Object>) valCnfs.get(of);
								if(valCnf == null)
									valCnf = new HashMap<String,Object>();
								
								Map<String,String> messages = Support.validateMessage(g, of,model, attr).getMessages();
								valCnf.put("messages",messages);
								attrCnf.put(of, valCnf);
							}
							value = attrs;
						}
					}else {
						value = g.a(res[0],res[1]);
					}
					if(value == null)
						value = g.t(res);
					
					break;
				default:
					value = g.t(res);
					log.debug("{} = {}",reference,value);
					break;
			}
			if(value instanceof Map){
				value = Json.format(value);
			}else if(value instanceof List){
				value = Json.format(value);
			}
			log.debug("referenceInsert: {} = {}",reference,value);
			return value == null ? "" : value;
		}else {			
			log.debug("referenceInsert: {} = {}",reference,value);
			return value;
		}			
	}

	@Override
	public boolean shouldLogOnNullSet(String lhs, String rhs) {
		log.debug("shouldLogOnNullSet: Set({} = {})",lhs,rhs);
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object methodException(Class claz, String method, Exception e)
			throws Exception {
		log.debug("methodException for: {}.{}",claz.getName(),method);
		log.debug(e.getMessage(),e);
		return null;
	}
	
	public Map<String,Object> params(){
		return text.params();
	}
	
	private String compressor(String in){
		if(docType.equals(DOCTYPE_JS)){
			return jsCompressor(in);
		}else if(docType.equals(DOCTYPE_CSS)){
			return cssCompressor(in);
		}else if(docType.equals(DOCTYPE_HTML)){
			return htmlCompressor(in);
		}else
			return in;
		
	}
	
	
	private String jsCompressor(String in){
		final StringBuffer warnings = new StringBuffer();
		final StringBuffer runtimeErrors = new StringBuffer();
		final StringBuffer errors = new StringBuffer();
		try{
			JavaScriptCompressor jscp = new JavaScriptCompressor(new StringReader(in),new ErrorReporter() {				
				@Override
				public void warning(String detail, String sourceName, int lineNumber,
						String lineSource, int columnNumber) {
					warnings.append("\nWarning for: " + text.getName());	
					warnings.append("\nDetail: " + detail);
					warnings.append("\nSourceName: " + sourceName);
					warnings.append("\nLineNumber: " + lineNumber);
					warnings.append("\nLineSource: " + lineSource);
					warnings.append("\nColumnNumber: " + columnNumber);
					log.debug("{}",warnings);
				}
				
				@Override
				public EvaluatorException runtimeError(String detail, String sourceName, int lineNumber,
						String lineSource, int columnNumber) {
					runtimeErrors.append("\nRuntimeError for: " + text.getName());	
					runtimeErrors.append("\nDetail: " + detail);		
					runtimeErrors.append("\nSourceName: " + sourceName);	
					runtimeErrors.append("\nLineNumber: " + lineNumber);	
					runtimeErrors.append("\nLineSource: " + lineSource);	
					runtimeErrors.append("\nColumnNumber: " + columnNumber);	
					log.error("{}",runtimeErrors);
					throw new EvaluatorException(errors.toString());
				}
				
				@Override
				public void error(String detail, String sourceName, int lineNumber,
						String lineSource, int columnNumber) {
					errors.append("\nErrors for: " + text.getName());	
					errors.append("\nDetail: " + detail);
					errors.append("\nSourceName: " + sourceName);
					errors.append("\nLineNumber: " + lineNumber);
					errors.append("\nLineSource: " + lineSource);
					errors.append("\nColumnNumber: " + columnNumber);
					log.error("{}",errors);
				}
			});
			StringWriter out = new StringWriter();
			
			jscp.compress(out,-1,true,false,false,false);
			return out.toString();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			return "alert(\"" + Support.code().js(text.getName()) + " : " + Support.code().js(e.getMessage()) + "\")";
		}
	}
	
	private String cssCompressor(String in){
		try{			
			CssCompressor cp = new CssCompressor(new StringReader(in));
			StringWriter out = new StringWriter();
			cp.compress(out,-1);
			return out.toString();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			return "";
		}
	}
	
	private String htmlCompressor(String in){
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setEnabled(true);                   //if false all compression is off (default is true)
		compressor.setRemoveComments(true);            //if false keeps HTML comments (default is true)
		compressor.setRemoveMultiSpaces(false);         //if false keeps multiple whitespace characters (default is true)
		compressor.setRemoveIntertagSpaces(true);      //removes iter-tag whitespace characters
		compressor.setRemoveQuotes(true);              //removes unnecessary tag attribute quotes
		compressor.setSimpleDoctype(true);             //simplify existing doctype
		compressor.setRemoveScriptAttributes(true);    //remove optional attributes from script tags
		compressor.setRemoveStyleAttributes(true);     //remove optional attributes from style tags
		compressor.setRemoveLinkAttributes(true);      //remove optional attributes from link tags
		compressor.setRemoveFormAttributes(true);      //remove optional attributes from form tags
		compressor.setRemoveInputAttributes(true);     //remove optional attributes from input tags
		compressor.setSimpleBooleanAttributes(true);   //remove values from boolean tag attributes
		compressor.setRemoveJavaScriptProtocol(true);  //remove "javascript:" from inline event handlers
//		compressor.setRemoveHttpProtocol(true);        //replace "http://" with "//" inside tag attributes
//		compressor.setRemoveHttpsProtocol(true);       //replace "https://" with "//" inside tag attributes
		compressor.setPreserveLineBreaks(false);        //keep \n
		compressor.setRemoveSurroundingSpaces("br,p"); //remove spaces around provided tags
		compressor.setCompressCss(true);               //compress inline css 
		compressor.setCompressJavaScript(true);        //compress inline javascript
		compressor.setYuiCssLineBreak(80);             //--line-break param for Yahoo YUI Compressor 
		compressor.setYuiJsDisableOptimizations(true); //--disable-optimizations param for Yahoo YUI Compressor 
		compressor.setYuiJsLineBreak(-1);              //--line-break param for Yahoo YUI Compressor
		compressor.setYuiJsNoMunge(true);              //--nomunge param for Yahoo YUI Compressor 
		compressor.setYuiJsPreserveAllSemiColons(true);//--preserve-semi param for Yahoo YUI Compressor 
		compressor.setJavaScriptCompressor(new Compressor() {
			@Override
			public String compress(String source) {
				return jsCompressor(source);
			}
		});
		return compressor.compress(in);
	}
	
	
}

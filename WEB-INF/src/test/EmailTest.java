package test;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.rails.ext.AbsGlobal;
import org.slf4j.Marker;
import net.rails.support.mail.worker.SendWorker;
import net.rails.support.mail.worker.Mail;
import java.util.Properties;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSession;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import com.sun.mail.util.MailSSLSocketFactory;
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.Map;
import net.rails.support.Support;
import javax.mail.Session;

public class EmailTest extends TestCase {

	public static AbsGlobal g;
	protected final static Logger log = LoggerFactory.getLogger(EmailTest.class);;
	
	public EmailTest(String name) {
		super(name);
	}

	public EmailTest() {
		super();
	}
	
	public void testSendWaiter() {
	    try{

        	g = new GlobalUnit();
    		g.options.put("controller", "User");
    		g.options.put("action", "sendSecurityCode");
    		
            String host = "smtp.killcoding.com";
            String port = "465";
            String username = "account@killcoding.com";
            String password = "Yip62129927";
            String fromAddress = "account@killcoding.com";
            String[] trustedHosts = "smtp.killcoding.com".split(",");
            
            
    		Properties props = new Properties();
    		props.put("mail.transport.protocol", "smtp");
    		props.put("mail.smtp.host",host);
    		props.put("mail.smtp.port",port);
    		props.put("mail.smtp.timeout", "30000");
    		props.put("mail.smtp.from", fromAddress);
    		props.put("mail.smtp.auth","true");
    		//TLS
    		props.put("mail.smtp.starttls.enable", "true");	
    		//SSL
    		props.put("mail.smtp.ssl.enable", "true");
    		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
    		props.put("mail.smtp.socketFactory.fallback", "false");	
    	    props.put("mail.smtp.ssl.checkserveridentity", "false");
            props.put("mail.smtp.ssl.trust", "*");
            
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustedHosts(trustedHosts);
            props.put("mail.smtp.ssl.socketFactory", sf);

    	    SendWorker sendWorker = new SendWorker();
    	    sendWorker.setProperties(props);
    	    sendWorker.setUserName(username);
    	    sendWorker.setPassword(password);
    	    sendWorker.setFromAddress(new InternetAddress(fromAddress));
    	    sendWorker.setValiPwd(true);
    	    
    	    sendWorker.addAddress(new InternetAddress("jackyip8@gmail.com")); 
			sendWorker.setSubject("Welcome to sign up KillCoding");
			sendWorker.setContent("Thanks " + new Date());

            Mail.send(Mail.TO,sendWorker);
        
    	    assertEquals(true, true);
	    }catch(Exception e){
	       // log.error(e.getMessage(),e);
	       e.printStackTrace();
	        assertEquals(true, false);
	    }
	}

}

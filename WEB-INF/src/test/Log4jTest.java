package test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.impl.Log4jLoggerFactory;

import app.model.Account;
import net.rails.Define;
import net.rails.active_record.Adapter;
import net.rails.active_record.DBResource;
import net.rails.ciphertext.Ciphertext;
import net.rails.ciphertext.Ciphertext.DESWorker;
import net.rails.ciphertext.Ciphertext.ThreeDESWorker;
import net.rails.ciphertext.exception.CiphertextException;
import net.rails.ext.AbsGlobal;
import net.rails.sql.query.Query;
import net.rails.support.Support;
//import net.rails.log.Log;
import net.rails.tpl.Tpl;
import net.rails.tpl.TplText;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class Log4jTest 
    extends TestCase
{
	public static AbsGlobal g;
	
	static{
		g = new GlobalUnit();
	}
	
    public Log4jTest( String testName )
    {
        super( testName );
    }
    
    public void testApp() throws IOException
    {
    	try{
    		
        	g.setLocale("default");
        	Logger log = LoggerFactory.getLogger(this.getClass());
//        	System.out.println(log.getName());
//        	MDC.put("log4j.jrails.point", "sql");
//        	System.out.println(MDC.get("log4j.jrails.point"));
        	System.out.println(Boolean.parseBoolean(null));
//        	Boolean.getBoolean(null);
            assertTrue( true );
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    }
    
}
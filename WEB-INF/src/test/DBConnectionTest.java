package test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;

import app.model.Account;
import app.model.Role;
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
public class DBConnectionTest 
    extends TestCase
{
	public static AbsGlobal g;
	
	static{
		g = new GlobalUnit();
	}
	
    public DBConnectionTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( DBConnectionTest.class );
    }
    
    public void testMaxConnections() throws IOException
    {
    	try{
        	g.setLocale("default");
        	for(int i = 0;i < 1;i++){
        		try{
            		Query q = new Query(new Account(g));
                	Account a = q.first();
                	
        			Query q1 = new Query(new Role(g));
                	Role b = q1.first();
//            		Account a = new Account(g);
//                	a.setId(Support.code().id());
//                	a.put("name","jack");
//                	a.put("age","22");
//                	a.onSave();
                	System.out.println(a);
                	System.out.println(b);
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        	}
            assertTrue( true );
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    }

    public void testApp() throws IOException
    {
    	Account a = null;
    	try{
        	g.setLocale("default");
        	a = new Account(g);
//        	a.useTransaction();;
        	a.setId(Support.code().id());
        	a.put("name","jack");
        	a.put("age","22");
        	a.onSave();
        	
        	Account a1  = new Account(g,"1k0padyd2h0z1973");
        	a1.useTransaction();
//        	a1.setId(Support.code().id());
        	a1.put("name","jack ");
        	a1.put("age","33");
        	a1.onSave();
        	
        	int i = 1/0;
        	a.commit();
            assertTrue( true );
    	}catch(Exception e){
    		e.printStackTrace();
    		a.rollback();
    	}

    }
    
    public void testConfig(){
    	Map<String,String> map = Support.config("deployer","java");
    	System.out.println(Support.config("deployer","java"));
    }
    
}

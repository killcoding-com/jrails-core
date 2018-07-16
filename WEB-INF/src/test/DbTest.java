package test;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DbTest 
    extends TestCase
{
	public static AbsGlobal g;
	
	static{
		g = new GlobalUnit();
	}
	
    public DbTest( String testName )
    {
        super( testName );
    }
    
    public void testApp() throws IOException
    {
    	try{
    		
        	g.setLocale("default");
        	Logger log = LoggerFactory.getLogger(this.getClass());
        	log.debug("hi,{}",1234);
        	Query q = new Query(new Account(g));
        	q.skipnil(true);
        	q.and("ni_name",null);
        	q.and("eq_id",null);
        	q.order("name", "ASC");
        	q.group("name");
        	System.out.println(q.first());
//        	
//        	Account a = new Account(g);
//        	a.put("name", "test");
//        	
//        	a.save();
        	
//        	Database db = new Database("Account",Database.READER);
//        	System.out.println(db.getDbcnf());
//        	System.out.println(db.getEnv());
//        	Map<String,Object> o = Support.config().getConfig().get("database");
//        	System.out.println(o);
//        	Account account = new Account(g);
//        	
//        	System.out.println(account.save());
            assertTrue( true );
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    }
    
}
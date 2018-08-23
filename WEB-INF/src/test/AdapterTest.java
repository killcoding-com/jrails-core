package test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;

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
import app.model.Product;

/**
 * Unit test for simple App.
 */
public class AdapterTest 
    extends TestCase
{
	public static AbsGlobal g;
	
	static{
		g = new GlobalUnit();
	}
	
    public AdapterTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( AdapterTest.class );
    }
    
    public void testCacheForQuery() throws IOException
    {
    	try{
        	g.setLocale("default");
        	for(int i = 0;i < 10000;i++){
        		try{
            		Query q = new Query(new Product(g)); 
            		q.and("eq_deleted",false);
            		q.cache(2);
                	Product a = q.first();
                // 	System.out.println(a);
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        	}
            assertTrue( true );
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    }
    
}

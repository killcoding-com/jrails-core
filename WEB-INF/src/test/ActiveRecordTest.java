package test;

import junit.framework.TestCase;
import net.rails.ext.AbsGlobal;
import app.model.Product;
import net.rails.support.Support;
import net.rails.support.worker.CodeWorker;
import net.rails.sql.query.Query;
import java.util.Map;

public class ActiveRecordTest extends TestCase {

	public static AbsGlobal g;

	static {
		g = new GlobalUnit();
	}

	public ActiveRecordTest(String name) {
		super(name);
	}

	public ActiveRecordTest() {
		super();
	}

	public void testCreateProduct() throws Exception {
		//Product extends ActiveRecord
		Product product = new Product(g);
		product.setId(Support.code().id());
		product.setCode("IP6P");
		product.setName("IPhone 6 Plus");
		product.setPrice(8888.00F);
		boolean result = product.save();
		assertEquals(true, result);
	}

	public void testGetFirstProduct() throws Exception {
		Query query = new Query(new Product(g));
		query.and("eq_code","IP6P");
		Product product = query.first();
		System.out.println("Product: " +  product);
		assertNotNull("First Product",product);
	}

}
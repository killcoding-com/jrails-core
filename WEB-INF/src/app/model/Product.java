package app.model;

import app.helper.ProductHelper;
import java.sql.SQLException;
import net.rails.active_record.exception.RecordNotFoundException;
import net.rails.ext.AbsGlobal;

public class Product extends ProductHelper {

	public Product(AbsGlobal g) {
		super(g);
	}

	public Product(AbsGlobal g, Object id) throws SQLException, RecordNotFoundException {
		super(g, id);
	}

}
package app.helper;

import java.sql.SQLException;
import net.rails.active_record.ActiveRecord;
import net.rails.active_record.exception.RecordNotFoundException;
import net.rails.ext.AbsGlobal;

public class ProductHelper extends ActiveRecord {

	public ProductHelper(AbsGlobal g) {
		super(g);
	}

	public ProductHelper(AbsGlobal g, Object id) throws SQLException, RecordNotFoundException {
		super(g, id);
	}

}
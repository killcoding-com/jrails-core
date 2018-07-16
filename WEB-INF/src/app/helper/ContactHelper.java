package app.helper;

import java.sql.SQLException;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.exception.RecordNotFoundException;
import net.rails.ext.AbsGlobal;

public class ContactHelper extends ActiveRecord {

	public ContactHelper(AbsGlobal g) {
		super(g);
	}

	public ContactHelper(AbsGlobal g, Object id) throws SQLException, RecordNotFoundException {
		super(g, id);
	}

}

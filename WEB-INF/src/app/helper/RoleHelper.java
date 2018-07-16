package app.helper;

import java.sql.SQLException;

import net.rails.active_record.ActiveRecord;
import net.rails.active_record.exception.RecordNotFoundException;
import net.rails.ext.AbsGlobal;

public class RoleHelper extends ActiveRecord {

	public RoleHelper(AbsGlobal g) {
		super(g);
	}

	public RoleHelper(AbsGlobal g, Object id) throws SQLException, RecordNotFoundException {
		super(g, id);
	}

}

package app.model;

import java.sql.SQLException;

import app.helper.RoleHelper;
import net.rails.active_record.exception.RecordNotFoundException;
import net.rails.ext.AbsGlobal;

public class Role extends RoleHelper {

	public Role(AbsGlobal g) {
		super(g);
		// TODO Auto-generated constructor stub
	}

	public Role(AbsGlobal g, Object id) throws SQLException, RecordNotFoundException {
		super(g, id);
		// TODO Auto-generated constructor stub
	}

}

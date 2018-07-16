package app.model;

import java.sql.SQLException;

import app.helper.AccountHelper;
import net.rails.active_record.exception.RecordNotFoundException;
import net.rails.ext.AbsGlobal;

public class Account extends AccountHelper {

	public Account(AbsGlobal g) {
		super(g);
		// TODO Auto-generated constructor stub
	}

	public Account(AbsGlobal g, Object id) throws SQLException, RecordNotFoundException {
		super(g, id);
		// TODO Auto-generated constructor stub
	}

}

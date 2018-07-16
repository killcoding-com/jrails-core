package net.rails.sql.worker;

class MysqlFindWorker implements Find{
	
	@Override
	public String getSql(FindWorker fw) {
		final StringBuffer where = fw.createWheres();
		final StringBuffer join = fw.createJoins();
		final StringBuffer select = fw.createSelects();
		final StringBuffer group = fw.createGroups();
		final StringBuffer having = fw.createHavings();
		final StringBuffer order = fw.createOrders();
		final StringBuffer first = fw.createFirsts();
		final StringBuffer last = fw.createLasts();
		if (!where.toString().equals("")) {
			where.insert(0, " WHERE ");
		}
		if (!join.toString().equals("")) {
			join.insert(0, " ");
		}
		if (!group.toString().equals("")) {
			group.insert(0, " GROUP BY ");
		}
		if (!having.toString().equals("")) {
			having.insert(0, " HAVING ");
		}
		if (!order.toString().equals("")) {
			order.insert(0, " ORDER BY ");
		}
		if (!first.toString().equals("")) {
			first.append(" ");
		}
		if (!last.toString().equals("")) {
			last.insert(0," ");
		}
		String limitStr = "";
		if(fw.getOffset() == null && fw.getLimit() != null){
			limitStr = " LIMIT " + fw.getLimit() + " ";
		}
		if(fw.getOffset() != null && fw.getLimit() == null){
			limitStr = " LIMIT " + fw.getOffset() + ",1 ";
		}
		if(fw.getOffset() != null && fw.getLimit() != null){
			limitStr = " LIMIT " + fw.getOffset() + "," + fw.getLimit() + " ";
		}
		String disc = " ";
		if(fw.isDistinct())
			disc = " DISTINCT ";
		
		return String.format("%sSELECT%s%s FROM %s%s%s%s%s%s%s%s",first,disc,select,fw.getAdapter().quoteSchemaAndTableName(), join, where, group, having, order,limitStr,last);
	}
	
}

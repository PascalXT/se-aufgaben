package queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DB;

public class Q5_WITH extends Query {

	public Q5_WITH(String headline) {
		super(headline);

	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String[] headers = new String[] {"Bundesland", "Partei", "�berhangsmandate"};
		List<List<String>> rows = new ArrayList<List<String>>();
		
		while (resultSet.next()) {
			List<String> row = new ArrayList<String>();
			row.add(resultSet.getString(DB.kBundeslandName));
			row.add(resultSet.getString(DB.kParteiKuerzel));
			row.add(resultSet.getString(DB.kAnzahlUeberhangsmandate));
			rows.add(row);
		}
		
		Table table = new Table(headers, rows);
		
		return table.getHtml();
	}

	@Override
	protected ResultSet doQuery() throws SQLException {
		
		String query = "WITH " + db.zweitStimmenNachBundesland() + " AS (" + stmtZweitStimmenNachBundesland() + "), "
			+ db.zweitStimmenNachPartei() + " AS (" + stmtZweitStimmenNachPartei(db.zweitStimmenNachBundesland()) + "), "
			+ db.direktmandate() + " AS ("
				+ stmtDirektmandateTable(db.erstStimmenNachWahlkreis(), db.maxErststimmenNachWahlkreis()) + "), "
			+ db.fuenfProzentParteien() + " AS (" + stmtFuenfProzentParteien(db.zweitStimmenNachBundesland()) + "), "
			+ db.dreiDirektMandatParteien() + " AS (" + stmtDreiDirektmandateParteien(db.direktmandate()) + "), "
			+ db.parteienImBundestag() + " AS ("
				+ stmtParteienImBundestag(db.fuenfProzentParteien(), db.dreiDirektMandatParteien()) + "), "
			+ db.divisoren() + " AS (" + stmtDivisoren() + "), "
			+ db.zugriffsreihenfolgeSitzeNachPartei() + " AS (" + stmtZugriffsreihenfolgeSitzeNachPartei(
					db.parteienImBundestag(), db.zweitStimmenNachPartei(), db.divisoren()) + "), "
			+ db.sitzeNachPartei() + " AS (" + stmtSitzeNachParteiTable(
					db.zweitStimmenNachPartei(), db.parteienImBundestag(), db.zugriffsreihenfolgeSitzeNachPartei()) + ", "
			+ db.zugriffsreihenfolgeSitzeNachLandeslisten() + " AS (" + stmtZugriffsreihenfolgeSitzeNachLandeslisten(
					db.parteienImBundestag(), db.zweitStimmenNachBundesland(), db.divisoren()) + ", "
			+ db.sitzeNachLandeslisten() + " AS (" + stmtSitzeNachLandeslisten(db.parteienImBundestag(),
					db.zweitStimmenNachBundesland(), db.sitzeNachPartei(), db.zugriffsreihenfolgeSitzeNachLandeslisten()) + ", "
			+ db.ueberhangsMandate() + " AS (" + stmtUeberhangsmandate(db.direktmandate(), db.sitzeNachLandeslisten(),
					db.direktMandateProParteiUndBundesland()) + ") "
			+ "SELECT * FROM " + db.ueberhangsMandate();
    
		return db.executeSQL(query);
	}

}
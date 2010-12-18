package queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DB;

public class Q4 extends Query {

	public Q4(String headline) {
		super(headline);
	}

	@Override
	protected ResultSet doQuery() throws SQLException {
		
		return db.executeSQL("" +
				"WITH " +
				"MaxErstStimmen(WahlkreisID, Anzahl) AS ( " +
					"SELECT we." + DB.kForeignKeyWahlkreisID + ", MAX(we." + DB.kWahlergebnis1Anzahl + ") " + 
					"FROM " + db.wahlergebnis1() + " we GROUP BY " + DB.kForeignKeyWahlkreisID + " " +
				"), " +
				"MaxZweitStimmen(WahlkreisID, Anzahl) AS ( " +
					"SELECT we." + DB.kForeignKeyWahlkreisID + ", MAX(we." + DB.kWahlergebnis2Anzahl + ") " + 
					"FROM " + db.wahlergebnis2() + " we GROUP BY " + DB.kForeignKeyWahlkreisID + " " +
				"), " +
				"GewinnerErstStimmen(WahlkreisID, KandidatID) AS ( " +
					"SELECT we." + DB.kForeignKeyWahlkreisID + ", we." + DB.kForeignKeyKandidatID + " " + 
					"FROM " + db.wahlergebnis1() + " we, MaxErstStimmen ms " + 
					"WHERE we." + DB.kForeignKeyWahlkreisID + " = ms.WahlkreisID " + 
					"AND we." + DB.kWahlergebnis1Anzahl + " = ms.Anzahl " +
				"), " +
				"GewinnerZweitStimmen(WahlkreisID, ParteiID) AS ( " +
					"SELECT we." + DB.kForeignKeyWahlkreisID + ", we." + DB.kForeignKeyParteiID + " " + 
					"FROM " + db.wahlergebnis2() + " we, MaxZweitStimmen ms " + 
					"WHERE we." + DB.kForeignKeyWahlkreisID + " = ms.WahlkreisID " + 
					"AND we." + DB.kWahlergebnis2Anzahl + " = ms.Anzahl " +
				") " +
				"SELECT g1.WahlkreisID, p1." + DB.kParteiKuerzel + " AS P1, p2." + DB.kParteiKuerzel + " AS P2 " +
				"FROM GewinnerErstStimmen g1, GewinnerZweitStimmen g2, " + db.partei() + " p1, " + db.partei() + " p2, " + db.kandidat() + " k " +
				"WHERE g1.WahlkreisID = g2.WahlkreisID " + 
				"AND g1.KandidatID = k." + DB.kID + " " +
				"AND k.ParteiID = p1." + DB.kID + " " +
				"AND g2.ParteiID = p2." + DB.kID + " "
		);
	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String[] tableHeaders = new String[] { "Wahlkreis", "Gewinner Erststimmen", "Gewinner Zweitstimmen" };
		List<List<String>> rows = new ArrayList<List<String>>();
		
		while (resultSet.next()) {
			List<String> row = new ArrayList<String>();
			row.add(String.valueOf(resultSet.getInt("WahlkreisID")));
			row.add(resultSet.getString("P1"));
			row.add(resultSet.getString("P2"));
			rows.add(row);
		}
		
		Table table = new Table(tableHeaders, rows);
		
		return table.getHtml();
	}

}

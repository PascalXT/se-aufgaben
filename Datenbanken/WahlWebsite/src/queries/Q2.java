package queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DB;

public class Q2 extends Query {

	public Q2(String headline) {
		super(headline);
	}

	@Override
	protected ResultSet doQuery() throws SQLException {
		
		String zweitStimmenNachBundeslandTable = createZweitStimmenNachBundeslandTable();
		String zweitStimmenNachParteiTable = createZweitStimmenNachParteiTable(zweitStimmenNachBundeslandTable);
    String direktMandateTable = createDirektmandateTable();
	  String fuenfProzentParteienTable = createFuenfProzentParteienTable(zweitStimmenNachBundeslandTable,
	  		zweitStimmenNachParteiTable);
	  String dreiDirektMandateParteienTable = createDreiDirektmandateParteienTable(direktMandateTable);
	  String parteienImBundestagTable = createParteienImBundestagTable(fuenfProzentParteienTable,
	  		dreiDirektMandateParteienTable);
    String sitzeNachParteiTable = createSitzeNachPartei(zweitStimmenNachParteiTable, parteienImBundestagTable);
    String sitzeNachLandesListenTable = createSitzeNachLandeslistenTable(parteienImBundestagTable,
    		zweitStimmenNachBundeslandTable, sitzeNachParteiTable);
    
    String qry = "WITH ListenKandidaten AS (" +
  		"SELECT " + DB.kID + " FROM " + db.kandidat() + " WHERE " + DB.kForeignKeyBundeslandID + " IS NOT NULL " + 
  		"EXCEPT " +
  		"SELECT " + DB.kForeignKeyKandidatID + " FROM " + db.direktmandate() + " " +
  	"), " +
  	"ListenKandidatenMitRang AS ( " + 
  		"SELECT lk." + DB.kID + ", k." + DB.kForeignKeyParteiID + ", b." + DB.kID + " AS BundeslandID, " +
  		"ROW_NUMBER() OVER (PARTITION BY b." + DB.kID + ", k." + DB.kForeignKeyParteiID + " ORDER BY k." + DB.kKandidatListenplatz + ") AS Rang " + 
  		"FROM ListenKandidaten lk, " + db.bundesland() + " b, " + db.kandidat() + " k " +
  		"WHERE lk." + DB.kID + " = k." + DB.kID + " " + 
  		"AND k." + DB.kForeignKeyBundeslandID + " = b." + DB.kID + " " + 
  	"), " +
  	
  	"BundeslandParteiZuDirektmandate AS ( " +
  			"SELECT w." + DB.kForeignKeyBundeslandID + ", d." + DB.kForeignKeyParteiID + ", " + 
  				"Count(d." + DB.kForeignKeyKandidatID + ") as " + DB.kAnzahl + " " +
  			"FROM " + db.direktmandate() + " d, " + db.wahlkreis() + " w " +
  			"WHERE d." + DB.kKandidatDMWahlkreisID + " = w." + DB.kID + " " +
  			"GROUP BY w." + DB.kForeignKeyBundeslandID + ", d." + DB.kForeignKeyParteiID + "), " +
  	
  	"Abgeordnete AS ( " + 
  		"SELECT " + DB.kForeignKeyKandidatID + " FROM " + direktMandateTable + " " + 
  		
  		"UNION " +
  		
  		"SELECT lkr." + DB.kID + " " +
  		"FROM ListenKandidatenMitRang lkr LEFT OUTER JOIN BundeslandParteiZuDirektmandate b ON lkr." + DB.kForeignKeyBundeslandID + " = b." + DB.kForeignKeyBundeslandID + " AND lkr." + DB.kForeignKeyParteiID + " = b." + DB.kForeignKeyParteiID + ", " + sitzeNachLandesListenTable + " s " + 
  		"WHERE s." + DB.kForeignKeyParteiID + " = lkr." + DB.kForeignKeyParteiID + " " + 
  		"AND s." + DB.kForeignKeyBundeslandID + " = lkr." + DB.kForeignKeyBundeslandID + " " + 
  		"AND lkr.Rang <= s." + DB.kAnzahlSitze + " - COALESCE(b." + DB.kAnzahl + ",0)" +
  	") " +
	  "SELECT k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + ", p." + DB.kParteiKuerzel + " " +
	  "FROM Abgeordnete a, " + db.kandidat() + " k LEFT OUTER JOIN " + db.partei() +
	  " p ON k." + DB.kForeignKeyParteiID + " = p." + DB.kID + " " +
	  "WHERE a." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " +
	  "ORDER BY k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + "";
	
	  return db.executeSQL(qry);
	}
	
	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String[] headers = new String[] {"", "Abgeordneter", "Partei"};
		List<List<String>> rows = new ArrayList<List<String>>();
		
		int i = 0;
		while (resultSet.next()) {
			i++;
			List<String> row = new ArrayList<String>();
			row.add(i + "");
			row.add(resultSet.getString(DB.kKandidatVorname) + " " + resultSet.getString(DB.kKandidatNachname));
			row.add(resultSet.getString(DB.kParteiKuerzel));
			rows.add(row);
		}
		
		Table table = new Table(headers, rows);
		
		return table.getHtml();
	}

}

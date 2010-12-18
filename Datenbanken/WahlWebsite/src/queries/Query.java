package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.DB;

public abstract class Query {

	protected String headline;

	
	protected DB db;
	
	public Query(String headline) {
		this.headline = headline;
	}

	public void setDatabase(DB db) {
		this.db = db;
	}

	public String generateHtmlOutput() {
		try {
			ResultSet resultSet = doQuery();
			String body = generateBody(resultSet);
			return "<html><body><h1>" + headline + "</h1>" + body + "</body></html>";
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected abstract ResultSet doQuery() throws SQLException;
	
	protected abstract String generateBody(ResultSet resultSet) throws SQLException;

	protected String createZweitStimmenNachBundeslandTable() throws SQLException {
		 	db.createOrReplaceTemporaryTable(db.zweitStimmenNachBundesland(), DB.kForeignKeyParteiID
	        + " BIGINT, " + DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
	    db.executeUpdate("INSERT INTO " + db.zweitStimmenNachBundesland() + " SELECT w2."
	        + DB.kForeignKeyParteiID + ", wk." + DB.kForeignKeyBundeslandID + ", sum(w2."
	        + DB.kWahlergebnis2Anzahl + ") as " + DB.kAnzahlStimmen + "" + " FROM "
	        + db.wahlergebnis2() + " w2" + ", " + db.wahlkreis() + " wk" + " WHERE w2."
	        + DB.kForeignKeyWahlkreisID + " = wk." + DB.kID + " GROUP BY " + "wk."
	        + DB.kForeignKeyBundeslandID + ", w2." + DB.kForeignKeyParteiID);
	    return db.zweitStimmenNachBundesland();
	}

	protected String createZweitStimmenNachParteiTable() throws SQLException {

    db.createOrReplaceTemporaryTable(db.zweitStimmenNachPartei(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.zweitStimmenNachPartei() + " SELECT "
        + DB.kForeignKeyParteiID + ", SUM(" + DB.kAnzahlStimmen + ") FROM "
        + db.zweitStimmenNachBundesland() + " GROUP BY " + DB.kForeignKeyParteiID);

		return db.zweitStimmenNachPartei();
	}

	protected String createDirektmandateTable() throws SQLException {

    db.createOrReplaceTemporaryTable(db.direktmandate(), DB.kForeignKeyKandidatID + " BIGINT, "
        + DB.kForeignKeyParteiID + " BIGINT, " + DB.kKandidatDMWahlkreisID + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.direktmandate() + " " +
        "WITH maxErgebnis(wahlkreisID, maxStimmen) AS ( " + 
        	"SELECT k." + DB.kKandidatDMWahlkreisID + ", MAX(v." + DB.kWahlergebnis1Anzahl + ") " + 
        	"FROM " + db.wahlergebnis1() + " v, " + db.kandidat() + " k " + 
        	"WHERE v." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " + 
        	"GROUP BY k." + DB.kKandidatDMWahlkreisID + " " + 
        ") " + 
        "SELECT k." + DB.kID + " AS " + DB.kForeignKeyKandidatID + ", k." + DB.kForeignKeyParteiID + ", k." + DB.kKandidatDMWahlkreisID + " " +
        "FROM maxErgebnis e, " + db.wahlergebnis1() + " v, " + db.kandidat() + " k " + 
        "WHERE e.wahlkreisID = v." + DB.kForeignKeyWahlkreisID + " " + 
        "AND e.maxStimmen = v." + DB.kWahlergebnis1Anzahl + " " + 
        "AND k." + DB.kID + " = v." + DB.kForeignKeyKandidatID
    );
    return db.direktmandate();
	}

	protected String createFuenfProzentParteienTable(String zweitStimmenNachBundeslandTable) throws SQLException {

		db.createOrReplaceTemporaryTable(db.fuenfProzentParteien(), DB.kForeignKeyParteiID + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.fuenfProzentParteien() + " SELECT p." + DB.kID + " as "
        + DB.kForeignKeyParteiID + " FROM " + db.partei() + " p, " + db.wahlergebnis2() + " v"
        + " WHERE v." + DB.kForeignKeyParteiID + " = p." + DB.kID + " GROUP BY p." + DB.kID
        + " HAVING CAST(SUM(v." + DB.kWahlergebnis2Anzahl + ") AS FLOAT)" + " / (SELECT SUM("
        + DB.kAnzahlStimmen + ") FROM " + zweitStimmenNachBundeslandTable + ")" + " >= 0.05");
    db.printResultSet(db.executeSQL("SELECT p." + DB.kParteiKuerzel + " FROM " + db.partei()
        + " p, " + db.fuenfProzentParteien() + " fpp" + " WHERE p." + DB.kID + " = fpp."
        + DB.kForeignKeyParteiID));
    return db.fuenfProzentParteien();
	}

	protected String createDreiDirektmandateParteienTable(String direktMandateTable) throws SQLException {

		db.createOrReplaceTemporaryTable(db.dreiDirektMandatParteien(), DB.kForeignKeyParteiID
        + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.dreiDirektMandatParteien() + " SELECT dm."
        + DB.kForeignKeyParteiID + " FROM " + direktMandateTable + " dm " + " GROUP BY dm."
        + DB.kForeignKeyParteiID + " HAVING COUNT(*) >= 3");
    return db.dreiDirektMandatParteien();
	}
	
	protected String createParteienImBundestagTable(String fuenfProzentParteienTable, String dreiDirektMandateTable) throws SQLException {
		db.createOrReplaceTemporaryTable(db.parteienImBundestag(), DB.kForeignKeyParteiID + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.parteienImBundestag() + " SELECT * FROM "
        + fuenfProzentParteienTable + " UNION " + " SELECT * FROM " + dreiDirektMandateTable);
    return db.parteienImBundestag();
	}
	
	protected String createSitzeNachParteiTable(String zweitStimmenNachParteiTable, String parteienImBundestagTable) throws SQLException {
		db.createOrReplaceTemporaryTable(db.sitzeNachPartei(), DB.kForeignKeyParteiID + " BIGINT, "
        + DB.kAnzahlSitze + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.sitzeNachPartei() + " "
        + "WITH Divisoren (wert) as (SELECT ROW_NUMBER() OVER (order by w." + DB.kID + ") - 0.5 FROM "
        + db.wahlkreis() + " w " + "UNION SELECT ROW_NUMBER() OVER (order by w." + DB.kID
        + ") + (SELECT COUNT(*) FROM " + db.wahlkreis() + ") - 0.5 FROM " + db.wahlkreis() + " w), "
        + "Zugriffsreihenfolge (" + DB.kForeignKeyParteiID + ", " + DB.kAnzahlStimmen
        + ", DivWert, Rang) as " + "(SELECT p." + DB.kForeignKeyParteiID + ", z." + DB.kAnzahlStimmen
        + ", (z." + DB.kAnzahlStimmen + " / d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY (z."
        + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + parteienImBundestagTable + " p, "
        + zweitStimmenNachParteiTable + " z, Divisoren d " + "WHERE p." + DB.kForeignKeyParteiID + " = z."
        + DB.kForeignKeyParteiID + " ORDER BY DivWert desc) " + "SELECT " + DB.kForeignKeyParteiID
        + ", COUNT(Rang) as " + DB.kAnzahlSitze + " FROM Zugriffsreihenfolge " + " WHERE Rang <= 598 "
        + " GROUP BY ParteiID");
    return db.sitzeNachPartei();
	}
	
	protected String createSitzeNachLandeslistenTable(String parteienImBundestagTable, String zweitStimmenNachBundeslandTable, String sitzeNachParteiTable) throws SQLException {
		db.createOrReplaceTemporaryTable(db.sitzeNachLandeslisten(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlSitze + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.sitzeNachLandeslisten() + " " + "WITH Divisoren (wert) as ( "
        + "SELECT ROW_NUMBER() OVER (order by w.ID) - 0.5 FROM " + db.wahlkreis() + " w "
        + "UNION SELECT ROW_NUMBER() OVER (order by w.ID) + (SELECT COUNT(*) FROM " + db.wahlkreis()
        + ") - 0.5 FROM " + db.wahlkreis() + " w " + "), " + " " + "Zugriffsreihenfolge ("
        + DB.kForeignKeyParteiID + ", " + DB.kForeignKeyBundeslandID
        + ", AnzahlStimmen, DivWert, Rang) as " + "(SELECT p." + DB.kForeignKeyParteiID + ", z."
        + DB.kForeignKeyBundeslandID + ", z." + DB.kAnzahlStimmen + ", (z." + DB.kAnzahlStimmen
        + " / d.wert) as DivWert, ROW_NUMBER() OVER (PARTITION BY p." + DB.kForeignKeyParteiID + " ORDER BY (z."
        + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + parteienImBundestagTable + " p, "
        + zweitStimmenNachBundeslandTable + " z, Divisoren d " + "WHERE p." + DB.kForeignKeyParteiID
        + " = z." + DB.kForeignKeyParteiID + " ORDER BY " + DB.kForeignKeyParteiID + ", DivWert desc) "
        + " " + "SELECT z." + DB.kForeignKeyParteiID + ", " + DB.kForeignKeyBundeslandID
        + ", COUNT(Rang) as " + DB.kAnzahlSitze + " " + "FROM Zugriffsreihenfolge z, "
        + sitzeNachParteiTable + " s " + "WHERE z." + DB.kForeignKeyParteiID + " = s."
        + DB.kForeignKeyParteiID + " AND z.Rang <= s." + DB.kAnzahlSitze + " " + "GROUP BY z."
        + DB.kForeignKeyParteiID + ", z." + DB.kForeignKeyBundeslandID + ", s."
        + DB.kForeignKeyParteiID);
    return db.sitzeNachLandeslisten();
	}	


public String createUeberhangsmandateTable(String direktMandateTable, String sitzeNachLandeslistenTable) throws SQLException {
	db.createOrReplaceTemporaryTable(db.ueberhangsMandate(), 
			DB.kForeignKeyBundeslandID + " BIGINT, " +
			DB.kBundeslandName + " VARCHAR(255), " + 
			DB.kForeignKeyParteiID + " BIGINT, " +
			DB.kParteiKuerzel + " VARCHAR(64), " +
			DB.kAnzahlUeberhangsmandate + " BIGINT ");

	db.executeUpdate("" + 
	"INSERT INTO " + db.ueberhangsMandate() + " " + 
	"WITH DirektMandateProParteiUndBundesland AS (" +
		"SELECT k." + DB.kForeignKeyParteiID + ", w." + DB.kForeignKeyBundeslandID + ", COUNT(*) AS AnzahlDirektmandate " +
		"FROM " + direktMandateTable + " dm, " + db.kandidat() + " k, " + db.wahlkreis() + " w " +
		"WHERE dm." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " + 
		"AND w." + DB.kID + " = k." + DB.kKandidatDMWahlkreisID + " " + 
		"GROUP BY k." + DB.kForeignKeyParteiID + ", w." + DB.kForeignKeyBundeslandID + 
	") " +
	"SELECT b." + DB.kID + " AS " + DB.kForeignKeyBundeslandID + ", b." + DB.kBundeslandName + ", p." + DB.kID + " AS " + DB.kForeignKeyParteiID + ", p." + DB.kParteiKuerzel + ", " +
		"dmpb.AnzahlDirektmandate - s." + DB.kAnzahlSitze + " AS " + DB.kAnzahlUeberhangsmandate + " " +
	"FROM DirektMandateProParteiUndBundesland dmpb, " + sitzeNachLandeslistenTable + " s, " +
		db.partei() + " p, " + db.bundesland() + " b " + 
	"WHERE dmpb." + DB.kForeignKeyBundeslandID + " = s." + DB.kForeignKeyBundeslandID + " " + 
		"AND dmpb." + DB.kForeignKeyParteiID + " = s." + DB.kForeignKeyParteiID + " " +
		"AND dmpb." + DB.kForeignKeyParteiID + " = p." + DB.kID + " " + 
		"AND dmpb." + DB.kForeignKeyBundeslandID + " = b." + DB.kID + " " +
		"AND dmpb.AnzahlDirektmandate - s." + DB.kAnzahlSitze + " > 0" 
	);
	
	return db.ueberhangsMandate();
}

}

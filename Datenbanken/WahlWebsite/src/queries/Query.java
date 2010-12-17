package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.DB;

public abstract class Query {
	
	protected String headline;
	
	protected DB database;
	
	public Query(String headline) {
		this.headline = headline;
	}

	public void setDatabase(DB database) {
		this.database = database;
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
	
	protected abstract String generateBody(ResultSet resultSet) throws SQLException;
	
	protected abstract ResultSet doQuery() throws SQLException;
	
	protected String createZweitStimmenNachBundeslandTable() throws SQLException {
		 	database.createOrReplaceTemporaryTable(database.zweitStimmenNachBundesland(), DB.kForeignKeyParteiID
	        + " BIGINT, " + DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
	    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachBundesland() + " SELECT w2."
	        + DB.kForeignKeyParteiID + ", wk." + DB.kForeignKeyBundeslandID + ", sum(w2."
	        + DB.kWahlergebnis2Anzahl + ") as " + DB.kAnzahlStimmen + "" + " FROM "
	        + database.wahlergebnis2() + " w2" + ", " + database.wahlkreis() + " wk" + " WHERE w2."
	        + DB.kForeignKeyWahlkreisID + " = wk." + DB.kID + " GROUP BY " + "wk."
	        + DB.kForeignKeyBundeslandID + ", w2." + DB.kForeignKeyParteiID);
	    return database.zweitStimmenNachBundesland();
	}
	
	protected String createZweitStimmenNachParteiTable() throws SQLException {

    database.createOrReplaceTemporaryTable(database.zweitStimmenNachPartei(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachPartei() + " SELECT "
        + DB.kForeignKeyParteiID + ", SUM(" + DB.kAnzahlStimmen + ") FROM "
        + database.zweitStimmenNachBundesland() + " GROUP BY " + DB.kForeignKeyParteiID);

    return database.zweitStimmenNachPartei();
	}
	
	protected String createDirektmandateTable() throws SQLException {
    database.createOrReplaceTemporaryTable(database.direktmandate(), DB.kForeignKeyKandidatID + " BIGINT, "
        + DB.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.direktmandate()
        + " WITH maxErgebnis(wahlkreisID, maxStimmen) as (SELECT" + " k." + DB.kKandidatDMWahlkreisID
        + ", MAX(v." + DB.kWahlergebnis1Anzahl + ")" + " FROM " + database.wahlergebnis1() + " v, "
        + database.kandidat() + " k" + " WHERE v." + DB.kForeignKeyKandidatID + " = k." + DB.kID
        + " GROUP BY k." + DB.kKandidatDMWahlkreisID + ")" + " SELECT k." + DB.kID + " as "
        + DB.kForeignKeyKandidatID + ", k." + DB.kForeignKeyParteiID + " FROM maxErgebnis e, "
        + database.wahlergebnis1() + " v, " + database.kandidat() + " k" + " WHERE e.wahlkreisID = v."
        + DB.kForeignKeyWahlkreisID + " AND e.maxStimmen = v." + DB.kWahlergebnis1Anzahl + " AND k."
        + DB.kID + " = v." + DB.kForeignKeyKandidatID);
    return database.direktmandate();
	}
	
	protected String createFuenfProzentParteienTable(String zweitStimmenNachBundeslandTable) throws SQLException {
		database.createOrReplaceTemporaryTable(database.fuenfProzentParteien(), DB.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.fuenfProzentParteien() + " SELECT p." + DB.kID + " as "
        + DB.kForeignKeyParteiID + " FROM " + database.partei() + " p, " + database.wahlergebnis2() + " v"
        + " WHERE v." + DB.kForeignKeyParteiID + " = p." + DB.kID + " GROUP BY p." + DB.kID
        + " HAVING CAST(SUM(v." + DB.kWahlergebnis2Anzahl + ") AS FLOAT)" + " / (SELECT SUM("
        + DB.kAnzahlStimmen + ") FROM " + zweitStimmenNachBundeslandTable + ")" + " >= 0.05");
    database.printResultSet(database.executeSQL("SELECT p." + DB.kParteiKuerzel + " FROM " + database.partei()
        + " p, " + database.fuenfProzentParteien() + " fpp" + " WHERE p." + DB.kID + " = fpp."
        + DB.kForeignKeyParteiID));
    return database.fuenfProzentParteien();
	}
	
	protected String createDreiDirektmandateParteienTable(String direktMandateTable) throws SQLException {
		database.createOrReplaceTemporaryTable(database.dreiDirektMandatParteien(), DB.kForeignKeyParteiID
        + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.dreiDirektMandatParteien() + " SELECT dm."
        + DB.kForeignKeyParteiID + " FROM " + direktMandateTable + " dm " + " GROUP BY dm."
        + DB.kForeignKeyParteiID + " HAVING COUNT(*) >= 3");
    return database.dreiDirektMandatParteien();
	}
	
	protected String createParteienImBundestagTable(String fuenfProzentParteienTable, String dreiDirektMandateTable) throws SQLException {
		database.createOrReplaceTemporaryTable(database.parteienImBundestag(), DB.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.parteienImBundestag() + " SELECT * FROM "
        + fuenfProzentParteienTable + " UNION " + " SELECT * FROM " + dreiDirektMandateTable);
    return database.parteienImBundestag();
	}
	
	protected String createSitzeNachParteiTable(String zweitStimmenNachParteiTable, String parteienImBundestagTable) throws SQLException {
		database.createOrReplaceTemporaryTable(database.sitzeNachPartei(), DB.kForeignKeyParteiID + " BIGINT, "
        + DB.kAnzahlSitze + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.sitzeNachPartei() + " "
        + "WITH Divisoren (wert) as (SELECT ROW_NUMBER() OVER (order by w." + DB.kID + ") - 0.5 FROM "
        + database.wahlkreis() + " w " + "UNION SELECT ROW_NUMBER() OVER (order by w." + DB.kID
        + ") + (SELECT COUNT(*) FROM " + database.wahlkreis() + ") - 0.5 FROM " + database.wahlkreis() + " w), "
        + "Zugriffsreihenfolge (" + DB.kForeignKeyParteiID + ", " + DB.kAnzahlStimmen
        + ", DivWert, Rang) as " + "(SELECT p." + DB.kForeignKeyParteiID + ", z." + DB.kAnzahlStimmen
        + ", (z." + DB.kAnzahlStimmen + " / d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY (z."
        + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + parteienImBundestagTable + " p, "
        + zweitStimmenNachParteiTable + " z, Divisoren d " + "WHERE p." + DB.kForeignKeyParteiID + " = z."
        + DB.kForeignKeyParteiID + " ORDER BY DivWert desc) " + "SELECT " + DB.kForeignKeyParteiID
        + ", COUNT(Rang) as " + DB.kAnzahlSitze + " FROM Zugriffsreihenfolge " + " WHERE Rang <= 598 "
        + " GROUP BY ParteiID");
    return database.sitzeNachPartei();
	}
	
	protected String createSitzeNachLandeslistenTable(String parteienImBundestagTable, String zweitStimmenNachBundeslandTable, String sitzeNachParteiTable) throws SQLException {
		database.createOrReplaceTemporaryTable(database.sitzeNachLandeslisten(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlSitze + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.sitzeNachLandeslisten() + " " + "WITH Divisoren (wert) as ( "
        + "SELECT ROW_NUMBER() OVER (order by w.ID) - 0.5 FROM " + database.wahlkreis() + " w "
        + "UNION SELECT ROW_NUMBER() OVER (order by w.ID) + (SELECT COUNT(*) FROM " + database.wahlkreis()
        + ") - 0.5 FROM " + database.wahlkreis() + " w " + "), " + " " + "Zugriffsreihenfolge ("
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
    return database.sitzeNachLandeslisten();
	}	
}

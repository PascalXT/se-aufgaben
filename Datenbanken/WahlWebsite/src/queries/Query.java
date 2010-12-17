package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;

public abstract class Query {

	protected String headline;

	protected Database database;

	public Query(String headline) {
		this.headline = headline;
	}

	public void setDatabase(Database database) {
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
		database.createOrReplaceTable(database.zweitStimmenNachBundesland(), Database.kForeignKeyParteiID
					+ " BIGINT, " + Database.kForeignKeyBundeslandID + " BIGINT, " + Database.kAnzahlStimmen + " BIGINT");
		database.executeUpdate("INSERT INTO " + database.zweitStimmenNachBundesland() + " SELECT w2."
					+ Database.kForeignKeyParteiID + ", wk." + Database.kForeignKeyBundeslandID + ", sum(w2."
					+ Database.kWahlergebnis2Anzahl + ") as " + Database.kAnzahlStimmen + "" + " FROM "
					+ database.wahlergebnis2() + " w2" + ", " + database.wahlkreis() + " wk" + " WHERE w2."
					+ Database.kForeignKeyWahlkreisID + " = wk." + Database.kID + " GROUP BY " + "wk."
					+ Database.kForeignKeyBundeslandID + ", w2." + Database.kForeignKeyParteiID);
		return database.zweitStimmenNachBundesland();
	}

	protected String createZweitStimmenNachParteiTable() throws SQLException {

		database.createOrReplaceTable(database.zweitStimmenNachPartei(), Database.kForeignKeyParteiID
				+ " BIGINT, " + Database.kAnzahlStimmen + " BIGINT");
		database.executeUpdate("INSERT INTO " + database.zweitStimmenNachPartei() + " SELECT "
				+ Database.kForeignKeyParteiID + ", SUM(" + Database.kAnzahlStimmen + ") FROM "
				+ database.zweitStimmenNachBundesland() + " GROUP BY " + Database.kForeignKeyParteiID);

		return database.zweitStimmenNachPartei();
	}

	protected String createDirektmandateTable() throws SQLException {
		database.createOrReplaceTable(database.direktmandate(), Database.kForeignKeyKandidatID + " BIGINT, "
				+ Database.kForeignKeyParteiID + " BIGINT");
		database.executeUpdate("INSERT INTO " + database.direktmandate()
				+ " WITH maxErgebnis(wahlkreisID, maxStimmen) as (SELECT" + " k." + Database.kKandidatDMWahlkreisID
				+ ", MAX(v." + Database.kWahlergebnis1Anzahl + ")" + " FROM " + database.wahlergebnis1() + " v, "
				+ database.kandidat() + " k" + " WHERE v." + Database.kForeignKeyKandidatID + " = k." + Database.kID
				+ " GROUP BY k." + Database.kKandidatDMWahlkreisID + ")" + " SELECT k." + Database.kID + " as "
				+ Database.kForeignKeyKandidatID + ", k." + Database.kForeignKeyParteiID + " FROM maxErgebnis e, "
				+ database.wahlergebnis1() + " v, " + database.kandidat() + " k" + " WHERE e.wahlkreisID = v."
				+ Database.kForeignKeyWahlkreisID + " AND e.maxStimmen = v." + Database.kWahlergebnis1Anzahl + " AND k."
				+ Database.kID + " = v." + Database.kForeignKeyKandidatID);
		return database.direktmandate();
	}

	protected String createFuenfProzentParteienTable(String zweitStimmenNachBundeslandTable) throws SQLException {
		database.createOrReplaceTable(database.fuenfProzentParteien(), Database.kForeignKeyParteiID + " BIGINT");
		database.executeUpdate("INSERT INTO " + database.fuenfProzentParteien() + " SELECT p." + Database.kID + " as "
				+ Database.kForeignKeyParteiID + " FROM " + database.partei() + " p, " + database.wahlergebnis2() + " v"
				+ " WHERE v." + Database.kForeignKeyParteiID + " = p." + Database.kID + " GROUP BY p." + Database.kID
				+ " HAVING CAST(SUM(v." + Database.kWahlergebnis2Anzahl + ") AS FLOAT)" + " / (SELECT SUM("
				+ Database.kAnzahlStimmen + ") FROM " + zweitStimmenNachBundeslandTable + ")" + " >= 0.05");
		database.printResultSet(database.executeSQL("SELECT p." + Database.kParteiKuerzel + " FROM " + database.partei()
				+ " p, " + database.fuenfProzentParteien() + " fpp" + " WHERE p." + Database.kID + " = fpp."
				+ Database.kForeignKeyParteiID));
		return database.fuenfProzentParteien();
	}

	protected String createDreiDirektmandateParteienTable(String direktMandateTable) throws SQLException {
		database.createOrReplaceTable(database.dreiDirektMandatParteien(), Database.kForeignKeyParteiID
				+ " BIGINT");
		database.executeUpdate("INSERT INTO " + database.dreiDirektMandatParteien() + " SELECT dm."
				+ Database.kForeignKeyParteiID + " FROM " + direktMandateTable + " dm " + " GROUP BY dm."
				+ Database.kForeignKeyParteiID + " HAVING COUNT(*) >= 3");
		return database.dreiDirektMandatParteien();
	}

	protected String createParteienImBundestagTable(String fuenfProzentParteienTable, String dreiDirektMandateTable)
			throws SQLException {
		database.createOrReplaceTable(database.parteienImBundestag(), Database.kForeignKeyParteiID + " BIGINT");
		database.executeUpdate("INSERT INTO " + database.parteienImBundestag() + " SELECT * FROM "
				+ fuenfProzentParteienTable + " UNION " + " SELECT * FROM " + dreiDirektMandateTable);
		return database.parteienImBundestag();
	}

	protected String createSitzeNachParteiTable(String zweitStimmenNachParteiTable, String parteienImBundestagTable)
			throws SQLException {
		database.createOrReplaceTable(database.sitzeNachPartei(), Database.kForeignKeyParteiID + " BIGINT, "
				+ Database.kAnzahlSitze + " BIGINT");
		database.executeUpdate("INSERT INTO " + database.sitzeNachPartei() + " "
				+ "WITH Divisoren (wert) as (SELECT ROW_NUMBER() OVER (order by w." + Database.kID + ") - 0.5 FROM "
				+ database.wahlkreis() + " w " + "UNION SELECT ROW_NUMBER() OVER (order by w." + Database.kID
				+ ") + (SELECT COUNT(*) FROM " + database.wahlkreis() + ") - 0.5 FROM " + database.wahlkreis() + " w), "
				+ "Zugriffsreihenfolge (" + Database.kForeignKeyParteiID + ", " + Database.kAnzahlStimmen
				+ ", DivWert, Rang) as " + "(SELECT p." + Database.kForeignKeyParteiID + ", z." + Database.kAnzahlStimmen
				+ ", (z." + Database.kAnzahlStimmen + " / d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY (z."
				+ Database.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + parteienImBundestagTable + " p, "
				+ zweitStimmenNachParteiTable + " z, Divisoren d " + "WHERE p." + Database.kForeignKeyParteiID + " = z."
				+ Database.kForeignKeyParteiID + " ORDER BY DivWert desc) " + "SELECT " + Database.kForeignKeyParteiID
				+ ", COUNT(Rang) as " + Database.kAnzahlSitze + " FROM Zugriffsreihenfolge " + " WHERE Rang <= 598 "
				+ " GROUP BY ParteiID");
		return database.sitzeNachPartei();
	}

	protected String createSitzeNachLandeslistenTable(String parteienImBundestagTable,
			String zweitStimmenNachBundeslandTable, String sitzeNachParteiTable) throws SQLException {
		database.createOrReplaceTable(database.sitzeNachLandeslisten(), Database.kForeignKeyParteiID
				+ " BIGINT, " + Database.kForeignKeyBundeslandID + " BIGINT, " + Database.kAnzahlSitze + " BIGINT");
		database.executeUpdate("INSERT INTO " + database.sitzeNachLandeslisten() + " " + "WITH Divisoren (wert) as ( "
				+ "SELECT ROW_NUMBER() OVER (order by w.ID) - 0.5 FROM " + database.wahlkreis() + " w "
				+ "UNION SELECT ROW_NUMBER() OVER (order by w.ID) + (SELECT COUNT(*) FROM " + database.wahlkreis()
				+ ") - 0.5 FROM " + database.wahlkreis() + " w " + "), " + " " + "Zugriffsreihenfolge ("
				+ Database.kForeignKeyParteiID + ", " + Database.kForeignKeyBundeslandID
				+ ", AnzahlStimmen, DivWert, Rang) as " + "(SELECT p." + Database.kForeignKeyParteiID + ", z."
				+ Database.kForeignKeyBundeslandID + ", z." + Database.kAnzahlStimmen + ", (z." + Database.kAnzahlStimmen
				+ " / d.wert) as DivWert, ROW_NUMBER() OVER (PARTITION BY p." + Database.kForeignKeyParteiID + " ORDER BY (z."
				+ Database.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + parteienImBundestagTable + " p, "
				+ zweitStimmenNachBundeslandTable + " z, Divisoren d " + "WHERE p." + Database.kForeignKeyParteiID
				+ " = z." + Database.kForeignKeyParteiID + " ORDER BY " + Database.kForeignKeyParteiID + ", DivWert desc) "
				+ " " + "SELECT z." + Database.kForeignKeyParteiID + ", " + Database.kForeignKeyBundeslandID
				+ ", COUNT(Rang) as " + Database.kAnzahlSitze + " " + "FROM Zugriffsreihenfolge z, "
				+ sitzeNachParteiTable + " s " + "WHERE z." + Database.kForeignKeyParteiID + " = s."
				+ Database.kForeignKeyParteiID + " AND z.Rang <= s." + Database.kAnzahlSitze + " " + "GROUP BY z."
				+ Database.kForeignKeyParteiID + ", z." + Database.kForeignKeyBundeslandID + ", s."
				+ Database.kForeignKeyParteiID);
		return database.sitzeNachLandeslisten();
	}
	
	public String createUeberhangsmandateTable(String direktMandateTable, String sitzeNachLandeslistenTable) throws SQLException {
			database.createOrReplaceTable(database.ueberhangsMandate(), 
					Database.kBundeslandName + " VARCHAR(255), " + 
					Database.kParteiKuerzel + " VARCHAR(64), " +
					Database.kAnzahlUeberhangsmandate + " BIGINT ");

			database.executeUpdate("" + 
			"INSERT INTO " + database.ueberhangsMandate() + " " + 
			"WITH DirektMandateProParteiUndBundesland AS (" +
				"SELECT k." + Database.kForeignKeyParteiID + ", w." + Database.kForeignKeyBundeslandID + ", COUNT(*) AS AnzahlDirektmandate " +
				"FROM " + direktMandateTable + " dm, " + database.kandidat() + " k, " + database.wahlkreis() + " w " +
				"WHERE dm." + Database.kForeignKeyKandidatID + " = k." + Database.kID + " " + 
				"AND w." + Database.kID + " = k." + Database.kForeignKeyDMWahlkreisID + " " + 
				"GROUP BY k." + Database.kForeignKeyParteiID + ", w." + Database.kForeignKeyBundeslandID + 
			") " +
			"SELECT b." + Database.kBundeslandName + ", p." + Database.kParteiKuerzel + ", " +
				"dmpb.AnzahlDirektmandate - s." + Database.kAnzahlSitze + " AS " + Database.kAnzahlUeberhangsmandate + " " +
			"FROM DirektMandateProParteiUndBundesland dmpb, " + sitzeNachLandeslistenTable + " s, " +
				database.partei() + " p, " + database.bundesland() + " b " + 
			"WHERE dmpb." + Database.kForeignKeyBundeslandID + " = s." + Database.kForeignKeyBundeslandID + " " + 
				"AND dmpb." + Database.kForeignKeyParteiID + " = s." + Database.kForeignKeyParteiID + " " +
				"AND dmpb." + Database.kForeignKeyParteiID + " = p." + Database.kID + " " + 
				"AND dmpb." + Database.kForeignKeyBundeslandID + " = b." + Database.kID + " " +
				"AND dmpb.AnzahlDirektmandate - s." + Database.kAnzahlSitze + " > 0" 
			);
			
			return database.ueberhangsMandate();
	}
}

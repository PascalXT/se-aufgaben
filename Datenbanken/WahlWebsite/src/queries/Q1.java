package queries;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;

public class Q1 extends Query {
	
	public Q1(String headline) {
		super(headline);
	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String tableRows = "<tr><th>Partei</th><th>Anzahl Sitze</th></tr>";
		
		// see http://code.google.com/apis/chart/docs/gallery/pie_charts.html
		String chartUrl = "http://chart.apis.google.com/chart?cht=p&chs=420x250";
		String chartData = "&chd=t:";
		String chartLabels = "&chl=";
		while(resultSet.next()) {
			String partei = resultSet.getString(Database.kParteiKuerzel);
			int sitze = resultSet.getInt(Database.kAnzahlSitze);
			
			tableRows += "<tr><td>" + partei + "</td><td>" + sitze + "</td></tr>";
			
			chartData += (sitze * 100 / 598) + ",";
			try {
				chartLabels += URLEncoder.encode(partei + " (" + sitze + ")", "UTF-8") + "|";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		chartUrl += chartData.substring(0, chartData.length() - 1);
		chartUrl += chartLabels.substring(0, chartLabels.length() - 1);
	
		return "<p> <img src=\"" + chartUrl + "\" alt=\"\"/> </p>" + "<table>" + tableRows + "</table>";
	}

	@Override
	protected ResultSet doQuery() throws SQLException {
		// Aggregate election results to Bundesland level.
	    database.createOrReplaceTemporaryTable(database.zweitStimmenNachBundesland(), Database.kForeignKeyParteiID
	        + " BIGINT, " + Database.kForeignKeyBundeslandID + " BIGINT, " + Database.kAnzahlStimmen + " BIGINT");
	    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachBundesland() + " SELECT w2."
	        + Database.kForeignKeyParteiID + ", wk." + Database.kForeignKeyBundeslandID + ", sum(w2."
	        + Database.kWahlergebnis2Anzahl + ") as " + Database.kAnzahlStimmen + "" + " FROM "
	        + database.wahlergebnis2() + " w2" + ", " + database.wahlkreis() + " wk" + " WHERE w2."
	        + Database.kForeignKeyWahlkreisID + " = wk." + Database.kID + " GROUP BY " + "wk."
	        + Database.kForeignKeyBundeslandID + ", w2." + Database.kForeignKeyParteiID);

	    database.createOrReplaceTemporaryTable(database.zweitStimmenNachPartei(), Database.kForeignKeyParteiID
	        + " BIGINT, " + Database.kAnzahlStimmen + " BIGINT");
	    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachPartei() + " SELECT "
	        + Database.kForeignKeyParteiID + ", SUM(" + Database.kAnzahlStimmen + ") FROM "
	        + database.zweitStimmenNachBundesland() + " GROUP BY " + Database.kForeignKeyParteiID);

	    database.printTable(database.zweitStimmenNachBundesland());
	    database.printTable(database.zweitStimmenNachPartei());
	    database.printResultSet(database.executeSQL("SELECT " + Database.kForeignKeyParteiID + ", SUM("
	        + Database.kAnzahlStimmen + ") FROM " + database.zweitStimmenNachBundesland() + " GROUP BY "
	        + Database.kForeignKeyParteiID));

	    // +++++++++++++++++ DIREKTMANDATE +++++++++++++++++ //
	    database.createOrReplaceTemporaryTable(database.direktmandate(), Database.kForeignKeyKandidatID + " BIGINT, "
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
	    database.printResultSet(database.executeSQL("SELECT COUNT(*) FROM " + database.direktmandate()));

	    // +++++++++++++++++ 5 PROZENT PARTEIEN +++++++++++++++++ //
	    database.createOrReplaceTemporaryTable(database.fuenfProzentParteien(), Database.kForeignKeyParteiID + " BIGINT");
	    database.executeUpdate("INSERT INTO " + database.fuenfProzentParteien() + " SELECT p." + Database.kID + " as "
	        + Database.kForeignKeyParteiID + " FROM " + database.partei() + " p, " + database.wahlergebnis2() + " v"
	        + " WHERE v." + Database.kForeignKeyParteiID + " = p." + Database.kID + " GROUP BY p." + Database.kID
	        + " HAVING CAST(SUM(v." + Database.kWahlergebnis2Anzahl + ") AS FLOAT)" + " / (SELECT SUM("
	        + Database.kAnzahlStimmen + ") FROM " + database.zweitStimmenNachBundesland() + ")" + " >= 0.05");
	    database.printResultSet(database.executeSQL("SELECT p." + Database.kParteiKuerzel + " FROM " + database.partei()
	        + " p, " + database.fuenfProzentParteien() + " fpp" + " WHERE p." + Database.kID + " = fpp."
	        + Database.kForeignKeyParteiID));

	    // +++++++++++++++++ PARTEIEN MIT MINDESTENS 3 DIREKTMANDATEN +++++++++++++++++ //
	    database.createOrReplaceTemporaryTable(database.dreiDirektMandatParteien(), Database.kForeignKeyParteiID
	        + " BIGINT");
	    database.executeUpdate("INSERT INTO " + database.dreiDirektMandatParteien() + " SELECT dm."
	        + Database.kForeignKeyParteiID + " FROM " + database.direktmandate() + " dm " + " GROUP BY dm."
	        + Database.kForeignKeyParteiID + " HAVING COUNT(*) >= 3");
	    database.printResultSet(database.executeSQL("SELECT p." + Database.kParteiKuerzel + " FROM " + database.partei()
	        + " p, " + database.dreiDirektMandatParteien() + " ddmp" + " WHERE p." + Database.kID + " = ddmp."
	        + Database.kForeignKeyParteiID));

	    // +++++++++++++++++ PARTEIEN IM BUNDESTAG +++++++++++++++++ //
	    database.createOrReplaceTemporaryTable(database.parteienImBundestag(), Database.kForeignKeyParteiID + " BIGINT");
	    database.executeUpdate("INSERT INTO " + database.parteienImBundestag() + " SELECT * FROM "
	        + database.fuenfProzentParteien() + " UNION " + " SELECT * FROM " + database.dreiDirektMandatParteien());
	    database.printResultSet(database.executeSQL("SELECT p." + Database.kParteiKuerzel + " FROM " + database.partei()
	        + " p, " + database.dreiDirektMandatParteien() + " ddmp" + " WHERE p." + Database.kID + " = ddmp."
	        + Database.kForeignKeyParteiID));

	    // +++++++++++++++++ ANZAHL PROPORZSITZE +++++++++++++++++ //
	    ResultSet anzahlProporzSitzeResultSet = database.executeSQL("WITH AlleinigeDirektmandate AS (" + " SELECT dm."
	        + Database.kForeignKeyKandidatID + " FROM " + database.direktmandate() + " dm" + " EXCEPT " + " SELECT dm."
	        + Database.kForeignKeyKandidatID + " FROM " + database.direktmandate() + " dm, "
	        + database.parteienImBundestag() + " pib" + " WHERE dm." + Database.kForeignKeyParteiID + " = pib."
	        + Database.kForeignKeyParteiID + ")"
	        + " SELECT 598 - COUNT(*) AS AnzahlProporzSitze FROM AlleinigeDirektmandate");
	    anzahlProporzSitzeResultSet.next();
	    int anzahlProporzSitze = anzahlProporzSitzeResultSet.getInt("AnzahlProporzSitze");
	    System.out.println("AnzahlProporzSitze: " + anzahlProporzSitze);

	    // +++++++++++++++++ Sitze nach Partei +++++++++++++++++++ //
	    database.createOrReplaceTemporaryTable(database.sitzeNachPartei(), Database.kForeignKeyParteiID + " BIGINT, "
	        + Database.kAnzahlSitze + " BIGINT");
	    database.executeUpdate("INSERT INTO " + database.sitzeNachPartei() + " "
	        + "WITH Divisoren (wert) as (SELECT ROW_NUMBER() OVER (order by w." + Database.kID + ") - 0.5 FROM "
	        + database.wahlkreis() + " w " + "UNION SELECT ROW_NUMBER() OVER (order by w." + Database.kID
	        + ") + (SELECT COUNT(*) FROM " + database.wahlkreis() + ") - 0.5 FROM " + database.wahlkreis() + " w), "
	        + "Zugriffsreihenfolge (" + Database.kForeignKeyParteiID + ", " + Database.kAnzahlStimmen
	        + ", DivWert, Rang) as " + "(SELECT p." + Database.kForeignKeyParteiID + ", z." + Database.kAnzahlStimmen
	        + ", (z." + Database.kAnzahlStimmen + " / d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY (z."
	        + Database.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + database.parteienImBundestag() + " p, "
	        + database.zweitStimmenNachPartei() + " z, Divisoren d " + "WHERE p." + Database.kForeignKeyParteiID + " = z."
	        + Database.kForeignKeyParteiID + " ORDER BY DivWert desc) " + "SELECT " + Database.kForeignKeyParteiID
	        + ", COUNT(Rang) as " + Database.kAnzahlSitze + " FROM Zugriffsreihenfolge " + " WHERE Rang <= 598 "
	        + " GROUP BY ParteiID");

	    // +++++++++++++++++ Sitze nach Landeslisten +++++++++++++++++++ //
	    database.createOrReplaceTemporaryTable(database.sitzeNachLandeslisten(), Database.kForeignKeyParteiID
	        + " BIGINT, " + Database.kForeignKeyBundeslandID + " BIGINT, " + Database.kAnzahlSitze + " BIGINT");
	    database.executeUpdate("INSERT INTO " + database.sitzeNachLandeslisten() + " " + "WITH Divisoren (wert) as ( "
	        + "SELECT ROW_NUMBER() OVER (order by w.ID) - 0.5 FROM " + database.wahlkreis() + " w "
	        + "UNION SELECT ROW_NUMBER() OVER (order by w.ID) + (SELECT COUNT(*) FROM " + database.wahlkreis()
	        + ") - 0.5 FROM " + database.wahlkreis() + " w " + "), " + " " + "Zugriffsreihenfolge ("
	        + Database.kForeignKeyParteiID + ", " + Database.kForeignKeyBundeslandID
	        + ", AnzahlStimmen, DivWert, Rang) as " + "(SELECT p." + Database.kForeignKeyParteiID + ", z."
	        + Database.kForeignKeyBundeslandID + ", z." + Database.kAnzahlStimmen + ", (z." + Database.kAnzahlStimmen
	        + " / d.wert) as DivWert, ROW_NUMBER() OVER (PARTITION BY p." + Database.kForeignKeyParteiID + " ORDER BY (z."
	        + Database.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + database.parteienImBundestag() + " p, "
	        + database.zweitStimmenNachBundesland() + " z, Divisoren d " + "WHERE p." + Database.kForeignKeyParteiID
	        + " = z." + Database.kForeignKeyParteiID + " ORDER BY " + Database.kForeignKeyParteiID + ", DivWert desc) "
	        + " " + "SELECT z." + Database.kForeignKeyParteiID + ", " + Database.kForeignKeyBundeslandID
	        + ", COUNT(Rang) as " + Database.kAnzahlSitze + " " + "FROM Zugriffsreihenfolge z, "
	        + database.sitzeNachPartei() + " s " + "WHERE z." + Database.kForeignKeyParteiID + " = s."
	        + Database.kForeignKeyParteiID + " AND z.Rang <= s." + Database.kAnzahlSitze + " " + "GROUP BY z."
	        + Database.kForeignKeyParteiID + ", z." + Database.kForeignKeyBundeslandID + ", s."
	        + Database.kForeignKeyParteiID);
	    
	    String qry = String.format("SELECT %s, SUM(%s) AS %s FROM %s sitze, %s partei WHERE sitze.%s = partei.%s GROUP BY %s", 
	    		Database.kParteiKuerzel, 
	    		Database.kAnzahlSitze, 
	    		Database.kAnzahlSitze, 
	    		database.sitzeNachLandeslisten(), 
	    		database.partei(),
	    		Database.kForeignKeyParteiID,
	    		Database.kID,
	    		Database.kParteiKuerzel);
	    
	    return database.executeSQL(qry);
	}
	
	
	
}

package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.DB;

public abstract class Query {

	protected String headline;
  protected final int kCurrentElectionYear = 2009;
  protected final int kPreviousElectionYear = 2005;
	
	protected DB db;
	
	String withQuery = "";
	
	public Query(String headline) {
		this.headline = headline;
	}

	public void setDatabase(DB db) {
		this.db = db;
	}
	
	public String replaceUmlaute(String input) {
		input = input.replace("�", "&uuml;");
		input = input.replace("�", "&Uuml;");
		input = input.replace("�", "&ouml;");
		input = input.replace("�", "&Ouml;");
		input = input.replace("�", "&auml;");
		input = input.replace("�", "&Auml;");
		input = input.replace("�", "&szlig;");
		input = input.replace("�", "&eacute;");
		input = input.replace("�", "&egrave;");
		
		return input;
	}

	public String generateHtmlOutput() {
		try {
			final long startTime = System.currentTimeMillis();
			ResultSet resultSet = doQuery();
			String body = generateBody(resultSet);
			body = replaceUmlaute(body);
			String html = "<html>";
			html += "<head>" + getHtmlHeader() + "</head>";
			html += "<body><div class=\"container\"><h1>" + headline + "</h1><hr/>";
			html += "<div class=\"span-6\">" + getNavigation() + "</div>";
			html += "<div class=\"span-18 last\">" + body + "</div>";
			html += "<hr/><p>Die Berechnung hat " + (System.currentTimeMillis() - startTime) + " Millisekunden gedauert.<p>";
			html += "</div></body></html>";
			return html;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String getNavigation() {
		String navigation = "<h3>Navigation</h3>";
		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q1\">Q1: Sitzverteilung</a><br/>";
		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q2\">Q2: Abgeordnete</a><br/>";
		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q3\">Q3: Wahlkreisinfo</a><br/>";
		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q4\">Q4: Wahlkreisergebnisse</a><br/>";
		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q5\">Q5: �berhangsmandate</a><br/>";
		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q6\">Q6: Knappste Sieger</a><br/>";
		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q7\">Q7: Wahlkreisinfo (Einzelstimmen)</a><br/>";
		return navigation;
	}

	private String getHtmlHeader() {
		String header = "";
		header += "<link rel=\"stylesheet\" href=\"css/screen.css\" type=\"text/css\" media=\"screen, projection\">";
		header += "<link rel=\"stylesheet\" href=\"css/custom.css\" type=\"text/css\" media=\"screen, projection\">";
		return header;
	}
	
	protected abstract ResultSet doQuery() throws SQLException;
	
	protected abstract String generateBody(ResultSet resultSet) throws SQLException;

	protected String stmtZweitStimmenNachBundesland() {
		return ""
			+ "SELECT w2." + DB.kForeignKeyParteiID + ", wk." + DB.kForeignKeyBundeslandID + ", "
			 			 + "sum(w2." + DB.kWahlergebnis2Anzahl + ") as " + DB.kAnzahlStimmen + " "
			+ "FROM " + db.zweitStimmenNachWahlkreis() + " w2" + ", " + db.wahlkreis() + " wk" + " "
			+ "WHERE w2." + DB.kForeignKeyWahlkreisID + " = wk." + DB.kID + " "
			+   "AND w2." + DB.kJahr + " = " + kCurrentElectionYear + " "
			+ "GROUP BY " + "wk." + DB.kForeignKeyBundeslandID + ", w2." + DB.kForeignKeyParteiID;
	}
	
	protected String createZweitStimmenNachBundeslandTable() throws SQLException {
		 	db.createOrReplaceTemporaryTable(db.zweitStimmenNachBundesland(), DB.kForeignKeyParteiID + " BIGINT, "
		 			+ DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
	    db.executeUpdate("INSERT INTO " + db.zweitStimmenNachBundesland() + " " + stmtZweitStimmenNachBundesland());
	    return db.zweitStimmenNachBundesland();
	}
	
	protected String stmtZweitStimmenNachPartei(String zweitStimmenNachBundeslandTable) {
		return ""
			+ "SELECT " + DB.kForeignKeyParteiID + ", SUM(" + DB.kAnzahlStimmen + ") AS " + DB.kAnzahlStimmen + " "
			+ "FROM " + zweitStimmenNachBundeslandTable + " "
			+ "GROUP BY " + DB.kForeignKeyParteiID;
	}

	protected String createZweitStimmenNachParteiTable(String zweitStimmenNachBundeslandTable) throws SQLException {
    db.createOrReplaceTemporaryTable(db.zweitStimmenNachPartei(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
    db.executeUpdate(""
    		+ "INSERT INTO " + db.zweitStimmenNachPartei() + " "
    			+ "(" + DB.kForeignKeyParteiID + ", " + DB.kAnzahlStimmen + ") "
    		+ stmtZweitStimmenNachPartei(zweitStimmenNachBundeslandTable));
		return db.zweitStimmenNachPartei();
	}

	protected String createDirektmandateTable() throws SQLException {
		return createDirektmandateTable(db.erstStimmenNachWahlkreis());
	}
	
	protected String stmtMaxErststimmenNachWahlkreis(String erstStimmenNachWahlkreisTable) {
		return "" +
  	"SELECT k." + DB.kKandidatDMWahlkreisID + " AS " + DB.kForeignKeyWahlkreisID + ", " + 
  				 "MAX(v." + DB.kAnzahl + ") AS maxStimmen " + 
  	"FROM " + erstStimmenNachWahlkreisTable + " v, " + db.kandidat() + " k " + 
  	"WHERE v." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " +
  		"AND v." + DB.kJahr + " = " + kCurrentElectionYear + " " +
  	"GROUP BY k." + DB.kKandidatDMWahlkreisID;
	}

	protected String stmtDirektmandateTable(String erstStimmenNachWahlkreisTable,
			String maxErststimmenNachWahlkreisTable) {
		return ""
		  + "SELECT k." + DB.kID + " AS " + DB.kForeignKeyKandidatID + ", k." + DB.kForeignKeyParteiID + ", "
		  			 + "k." + DB.kKandidatDMWahlkreisID + " "
			+ "FROM " + maxErststimmenNachWahlkreisTable + " e, " + erstStimmenNachWahlkreisTable + " v, " + db.kandidat() + " k "
			+ "WHERE e.wahlkreisID = v." + DB.kForeignKeyWahlkreisID + " " + 
				  "AND e.maxStimmen = v." + DB.kAnzahl + " " + 
				  "AND k." + DB.kID + " = v." + DB.kForeignKeyKandidatID + " " +
				  "AND v." + DB.kJahr + " = " + kCurrentElectionYear;
	}
	
	protected String createDirektmandateTable(String erstStimmenNachWahlkreisTable) throws SQLException {
    db.createOrReplaceTemporaryTable(db.direktmandate(), DB.kForeignKeyKandidatID + " BIGINT, "
        + DB.kForeignKeyParteiID + " BIGINT, " + DB.kKandidatDMWahlkreisID + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.direktmandate()
    		+	" WITH " + db.maxErststimmenNachWahlkreis() 
    		+ " AS ( " + stmtMaxErststimmenNachWahlkreis(erstStimmenNachWahlkreisTable) + ") "
    		+	stmtDirektmandateTable(erstStimmenNachWahlkreisTable, db.maxErststimmenNachWahlkreis())
    );
    return db.direktmandate();
	}

	protected String stmtFuenfProzentParteien(String zweitStimmenNachBundeslandTable) {
		return ""
			+ "SELECT p." + DB.kID + " as " + DB.kForeignKeyParteiID + " "
			+ "FROM " + db.partei() + " p, " + db.zweitStimmenNachWahlkreis() + " v "
	    + "WHERE v." + DB.kForeignKeyParteiID + " = p." + DB.kID + " "
	    	+ "AND v." + DB.kJahr + "=" + kCurrentElectionYear + " "
	    + "GROUP BY p." + DB.kID + " "
	    + "HAVING CAST(SUM(v." + DB.kWahlergebnis2Anzahl + ") AS FLOAT)" + " / "
	    	+ "(SELECT SUM(" + DB.kAnzahlStimmen + ") "	+ "FROM " + zweitStimmenNachBundeslandTable + ")"
	    	+ " >= 0.05";
	}
	
	protected String createFuenfProzentParteienTable(String zweitStimmenNachBundeslandTable) throws SQLException {
		db.createOrReplaceTemporaryTable(db.fuenfProzentParteien(), DB.kForeignKeyParteiID + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.fuenfProzentParteien() + " " 
    		+ stmtFuenfProzentParteien(zweitStimmenNachBundeslandTable));

//    db.printResultSet(db.executeSQL("SELECT p." + DB.kParteiKuerzel + " FROM " + db.partei()
//        + " p, " + db.fuenfProzentParteien() + " fpp" + " WHERE p." + DB.kID + " = fpp."
//        + DB.kForeignKeyParteiID));
    return db.fuenfProzentParteien();
	}

	
	protected String stmtDreiDirektmandateParteien(String direktMandateTable) {
		return "SELECT dm."
    	+ DB.kForeignKeyParteiID + " FROM " + direktMandateTable + " dm " + " GROUP BY dm."
    	+ DB.kForeignKeyParteiID + " HAVING COUNT(*) >= 3";
	}
	protected String createDreiDirektmandateParteienTable(String direktMandateTable) throws SQLException {

		db.createOrReplaceTemporaryTable(db.dreiDirektMandatParteien(), DB.kForeignKeyParteiID
        + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.dreiDirektMandatParteien() + " "
    		+ stmtDreiDirektmandateParteien(direktMandateTable));
    return db.dreiDirektMandatParteien();
	}
	
	protected String stmtParteienImBundestag(String fuenfProzentParteienTable, String dreiDirektMandateTable) {
		return  " SELECT * FROM " + fuenfProzentParteienTable + " UNION " + " SELECT * FROM " + dreiDirektMandateTable;
	}
	
	protected String createParteienImBundestagTable(String fuenfProzentParteienTable, String dreiDirektMandateTable) throws SQLException {
		db.createOrReplaceTemporaryTable(db.parteienImBundestag(), DB.kForeignKeyParteiID + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.parteienImBundestag()
    		+ " " + stmtParteienImBundestag(fuenfProzentParteienTable, dreiDirektMandateTable));
    return db.parteienImBundestag();
	}
	
	protected String stmtDivisoren() {
		return ""
			+ "SELECT (ROW_NUMBER() OVER (order by w." + DB.kID + ")  - 0.5) as Wert "
			+ "FROM " + db.wahlkreis() + " w "
			+ "UNION "
			+ "SELECT (ROW_NUMBER() OVER (order by w." + DB.kID + ") + (SELECT COUNT(*) FROM " + db.wahlkreis() + ") - 0.5) AS Wert "
			+ "FROM " + db.wahlkreis() + " w";
	}
	
	protected String stmtZugriffsreihenfolgeSitzeNachPartei(String parteienImBundestagTable,
			String zweitStimmenNachParteiTable, String DivisorenTable) {
		return ""
			+ "SELECT p." + DB.kForeignKeyParteiID + ", z." + DB.kAnzahlStimmen + ", "
			+ "(z." + DB.kAnzahlStimmen + " / d.wert) as DivWert, "
			+ "ROW_NUMBER() OVER (ORDER BY (z." + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang "
			+ "FROM " + parteienImBundestagTable + " p, " + zweitStimmenNachParteiTable + " z, " + DivisorenTable + " d "
			+ "WHERE p." + DB.kForeignKeyParteiID + " = z."
		  + DB.kForeignKeyParteiID + " "
		  + "ORDER BY DivWert desc";
	}
	
	protected String stmtSitzeNachParteiTable(String zweitStimmenNachParteiTable, String parteienImBundestagTable,
			String zugriffsreihenfolgeSitzeNachParteiTable) {
		return ""
		+ "SELECT " + DB.kForeignKeyParteiID + ", COUNT(Rang) as " + DB.kAnzahlSitze + " "
		+ "FROM " + zugriffsreihenfolgeSitzeNachParteiTable + " "
		+ "WHERE Rang <= 598 "
    + "GROUP BY " + DB.kForeignKeyParteiID;
	}
	
	protected String createSitzeNachPartei(String zweitStimmenNachParteiTable, String parteienImBundestagTable) throws SQLException {
		db.createOrReplaceTemporaryTable(db.sitzeNachPartei(), DB.kForeignKeyParteiID + " BIGINT, "
        + DB.kAnzahlSitze + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.sitzeNachPartei() + " "
        + "WITH " + db.divisoren() + " AS (" + stmtDivisoren() + "), "
        + db.zugriffsreihenfolgeSitzeNachPartei() + " AS ("
         + stmtZugriffsreihenfolgeSitzeNachPartei(parteienImBundestagTable, zweitStimmenNachParteiTable, db.divisoren())
         + ") "
        + stmtSitzeNachParteiTable(
        		zweitStimmenNachParteiTable, parteienImBundestagTable, db.zugriffsreihenfolgeSitzeNachPartei()));
    return db.sitzeNachPartei();
	}
	
	protected String stmtZugriffsreihenfolgeSitzeNachLandeslisten(String parteienImBundestagTable,
			String zweitStimmenNachBundeslandTable, String divisorenTable) {
		return ""
		+ "SELECT p." + DB.kForeignKeyParteiID + ", z." + DB.kForeignKeyBundeslandID + ", z." + DB.kAnzahlStimmen + ", "
		  		 + "(z." + DB.kAnzahlStimmen + " / d.wert) as DivWert, "
		  		 + "ROW_NUMBER() OVER (PARTITION BY p." + DB.kForeignKeyParteiID + " "
		  		 	 + "ORDER BY (z." + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang "
		+ "FROM " + parteienImBundestagTable + " p, " + zweitStimmenNachBundeslandTable + " z, " + divisorenTable + " d "
		+ "WHERE p." + DB.kForeignKeyParteiID + " = z." + DB.kForeignKeyParteiID + " "
		+ "ORDER BY " + DB.kForeignKeyParteiID + ", DivWert DESC";
	}
	
	protected String stmtSitzeNachLandeslisten(String parteienImBundestagTable, 
			String zweitStimmenNachBundeslandTable, String sitzeNachParteiTable,
			String zugriffsreihenfolgeSitzeNachLandeslistenTable) {
		return ""
			+ "SELECT z." + DB.kForeignKeyParteiID + ", " + DB.kForeignKeyBundeslandID 
					 + ", COUNT(Rang) as " + DB.kAnzahlSitze + " "
	    + "FROM " + zugriffsreihenfolgeSitzeNachLandeslistenTable +" z, " + sitzeNachParteiTable + " s "
	    + "WHERE z." + DB.kForeignKeyParteiID + " = s." + DB.kForeignKeyParteiID + " "
	    	+ "AND z.Rang <= s." + DB.kAnzahlSitze + " "
	    + "GROUP BY z." + DB.kForeignKeyParteiID + ", z." + DB.kForeignKeyBundeslandID + ", s." + DB.kForeignKeyParteiID;
	}
	
	protected String createSitzeNachLandeslistenTable(String parteienImBundestagTable, 
			String zweitStimmenNachBundeslandTable, String sitzeNachParteiTable) throws SQLException {
		db.createOrReplaceTemporaryTable(db.sitzeNachLandeslisten(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlSitze + " BIGINT");
    db.executeUpdate("INSERT INTO " + db.sitzeNachLandeslisten() + " "
    		+ "WITH " + db.divisoren() + " AS (" + stmtDivisoren() + "), "
    		+ db.zugriffsreihenfolgeSitzeNachLandeslisten() + " AS ("
    			+ stmtZugriffsreihenfolgeSitzeNachLandeslisten(
    					parteienImBundestagTable, zweitStimmenNachBundeslandTable, db.divisoren()) + ") "
    		+ stmtSitzeNachLandeslisten(parteienImBundestagTable, zweitStimmenNachBundeslandTable, sitzeNachParteiTable, 
    				db.zugriffsreihenfolgeSitzeNachLandeslisten()));
    return db.sitzeNachLandeslisten();
	}	

	protected String stmtDirektMandateProParteiUndBundesland(String direktMandateTable) {
		return "" +
			"SELECT k." + DB.kForeignKeyParteiID + ", w." + DB.kForeignKeyBundeslandID + ", COUNT(*) AS AnzahlDirektmandate " +
			"FROM " + direktMandateTable + " dm, " + db.kandidat() + " k, " + db.wahlkreis() + " w " +
			"WHERE dm." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " + 
			"AND w." + DB.kID + " = k." + DB.kKandidatDMWahlkreisID + " " + 
			"GROUP BY k." + DB.kForeignKeyParteiID + ", w." + DB.kForeignKeyBundeslandID;
	}
	
	protected String stmtUeberhangsmandate(String direktMandateTable, String sitzeNachLandeslistenTable,
			String direktMandateProParteiUndBundeslandTable) {
		return ""
			+ "SELECT b." + DB.kID + " AS " + DB.kForeignKeyBundeslandID + ", b." + DB.kBundeslandName + ", "
				+ "p." + DB.kID + " AS " + DB.kForeignKeyParteiID + ", p." + DB.kParteiKuerzel + ", "
				+ "dmpb.AnzahlDirektmandate - s." + DB.kAnzahlSitze + " AS " + DB.kAnzahlUeberhangsmandate + " "
			+ "FROM " + direktMandateProParteiUndBundeslandTable + " dmpb, " + sitzeNachLandeslistenTable + " s, "
				+ db.partei() + " p, " + db.bundesland() + " b "
			+ "WHERE dmpb." + DB.kForeignKeyBundeslandID + " = s." + DB.kForeignKeyBundeslandID + " "
				+ "AND dmpb." + DB.kForeignKeyParteiID + " = s." + DB.kForeignKeyParteiID + " "
				+ "AND dmpb." + DB.kForeignKeyParteiID + " = p." + DB.kID + " "
				+ "AND dmpb." + DB.kForeignKeyBundeslandID + " = b." + DB.kID + " "
				+ "AND dmpb.AnzahlDirektmandate - s." + DB.kAnzahlSitze + " > 0";
	}

	protected String createUeberhangsmandateTable(String direktMandateTable, String sitzeNachLandeslistenTable) throws SQLException {
		db.createOrReplaceTemporaryTable(db.ueberhangsMandate(), 
				DB.kForeignKeyBundeslandID + " BIGINT, " +
				DB.kBundeslandName + " VARCHAR(255), " + 
				DB.kForeignKeyParteiID + " BIGINT, " +
				DB.kParteiKuerzel + " VARCHAR(64), " +
				DB.kAnzahlUeberhangsmandate + " BIGINT ");
	
		db.executeUpdate(""
				+ "INSERT INTO " + db.ueberhangsMandate() + " "
				+ "WITH " + db.direktMandateProParteiUndBundesland() + " AS ("
					+ stmtDirektMandateProParteiUndBundesland(direktMandateTable) + ") "
				+ stmtUeberhangsmandate(direktMandateTable, sitzeNachLandeslistenTable,
						db.direktMandateProParteiUndBundesland()));
		
		return db.ueberhangsMandate();
	}
	
	
	public String createWahlkreissiegerTable() throws SQLException {
		db.createOrReplaceTemporaryTable(db.wahlkreissieger(), 
				DB.kForeignKeyWahlkreisID + " BIGINT, " +
				DB.kForeignKeyBundeslandID + " BIGINT, " +
				"P1 VARCHAR(64), " +
				"P2 VARCHAR(64) ");

		db.executeUpdate("INSERT INTO " + db.wahlkreissieger() + " " +
				"WITH " +
				"MaxErstStimmen(WahlkreisID, Anzahl) AS ( " +
					"SELECT we." + DB.kForeignKeyWahlkreisID + ", MAX(we." + DB.kAnzahl + ") " + 
					"FROM " + db.erstStimmenNachWahlkreis() + " we " +
					"WHERE we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"GROUP BY " + DB.kForeignKeyWahlkreisID + " " +
				"), " +
				"MaxZweitStimmen(WahlkreisID, Anzahl) AS ( " +
					"SELECT we." + DB.kForeignKeyWahlkreisID + ", MAX(we." + DB.kWahlergebnis2Anzahl + ") " + 
					"FROM " + db.zweitStimmenNachWahlkreis() + " we " +
					"WHERE we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"GROUP BY " + DB.kForeignKeyWahlkreisID + " " +
				"), " +
				"GewinnerErstStimmen(WahlkreisID, KandidatID) AS ( " +
					"SELECT we." + DB.kForeignKeyWahlkreisID + ", we." + DB.kForeignKeyKandidatID + " " + 
					"FROM " + db.erstStimmenNachWahlkreis() + " we, MaxErstStimmen ms " + 
					"WHERE we." + DB.kForeignKeyWahlkreisID + " = ms.WahlkreisID " + 
					"AND we." + DB.kAnzahl + " = ms.Anzahl " +
					"AND we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
				"), " +
				"GewinnerZweitStimmen(WahlkreisID, ParteiID) AS ( " +
					"SELECT we." + DB.kForeignKeyWahlkreisID + ", we." + DB.kForeignKeyParteiID + " " + 
					"FROM " + db.zweitStimmenNachWahlkreis() + " we, MaxZweitStimmen ms " + 
					"WHERE we." + DB.kForeignKeyWahlkreisID + " = ms.WahlkreisID " + 
					"AND we." + DB.kWahlergebnis2Anzahl + " = ms.Anzahl " +
					"AND we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
				") " +
				"SELECT g1.WahlkreisID, wk." + DB.kForeignKeyBundeslandID + ", p1." + DB.kParteiKuerzel + " AS P1, p2." + DB.kParteiKuerzel + " AS P2 " +
				"FROM GewinnerErstStimmen g1, GewinnerZweitStimmen g2, " + db.partei() + " p1, " + db.partei() + " p2, " + db.kandidat() + " k, " + db.wahlkreis() + " wk " + 
				"WHERE g1.WahlkreisID = g2.WahlkreisID " + 
				"AND g1.KandidatID = k." + DB.kID + " " +
				"AND k.ParteiID = p1." + DB.kID + " " +
				"AND g2.ParteiID = p2." + DB.kID + " " + 
				"AND wk." + DB.kID + " = g1.WahlkreisID "
		);
		
		return db.wahlkreissieger();
	}

}

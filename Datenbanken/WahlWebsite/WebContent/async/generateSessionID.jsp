<%@ page language="java" contentType="text/html" pageEncoding="UTF-8" 
	import="java.util.UUID,org.json.simple.JSONObject,flags.Flags,flags.FlagDefinition,flags.FlagErrorException,database.DB,java.sql.*;"
%>


<%
DB db = null;
try {
	String configFile = gitIgnore.Config.getConfigFile();
	String[] args = { configFile };
	Flags.setFlags(FlagDefinition.kFlagDefinition, args);
	db = DB.getDatabaseByFlags();
} catch (FlagErrorException e) {
	e.printStackTrace();
	System.exit(0);
} 
%>

<%

JSONObject json = new JSONObject();

String persoID = request.getParameter("persoID");
String wk = request.getParameter("wk");
String wb = request.getParameter("wb");
if (persoID == null || wk == null || wb == null) {
	json.put("success", false);
	json.put("error", "PersoID, Wahlkreis oder Wahlbezirk Parameter fehlen");
} else {

	boolean found = db.executeSQL("" + 
		"SELECT * FROM " + db.wahlberechtigter() + " " + 
		"WHERE " + DB.kID + " = '" + persoID + "' " + 
		"AND " + DB.kWahlberechtigterGewaehlt + " = '0' " + 
		"AND " + DB.kForeignKeyWahlkreisID + " = " + wk + " " +  
		"AND " + DB.kForeignKeyWahlbezirkID + " = " + wb
	).next();
	
	if (found == false) {
		json.put("error", "Perso-ID ungültig (hat entweder bereits gewählt oder in diesem Wahlkreis nicht wahlberechtigt)");
		json.put("success", false);
		
	} else {
		String sessionID = UUID.randomUUID().toString();
		db.executeUpdate("INSERT INTO " + db.sessionIDs() + " " +
			"(" + DB.kID + ", " + DB.kForeignKeyWahlkreisID + ", " + DB.kForeignKeyWahlbezirkID + ")" + 
			"VALUES ('" + sessionID + "', '" + wk + "', '" + wb + "')"
		);
		db.executeUpdate("" + 
			"UPDATE " + db.wahlberechtigter() + " " + 
			"SET " + DB.kWahlberechtigterGewaehlt + " = '1' " + 
			"WHERE " + DB.kID + " = '" + persoID + "'"
		);
		json.put("sessionID", sessionID);
		json.put("success", true);
	}
}

out.print(json);
out.flush();
%>
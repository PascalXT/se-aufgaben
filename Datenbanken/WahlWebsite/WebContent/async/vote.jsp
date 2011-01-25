<%@ page language="java" contentType="text/html" pageEncoding="UTF-8" 
	import="org.json.simple.JSONObject,flags.Flags,flags.FlagDefinition,flags.FlagErrorException,database.DB,java.sql.*;"

%><%

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

%><%

String sessionID = request.getParameter("sessionID");
String erststimme = request.getParameter("erststimme");
String zweitstimme = request.getParameter("zweitstimme");

JSONObject json = new JSONObject();

ResultSet rs = db.executeSQL("" + 
	"SELECT * FROM " + db.sessionIDs() + " WHERE " + DB.kID + " = '" + sessionID + "'"
);

if (rs.next() == false) {
	json.put("success", false);
	json.put("error", "Wählpasswort ungültig");
} else {
	int wk = rs.getInt(DB.kForeignKeyWahlkreisID);
	int wb = rs.getInt(DB.kForeignKeyWahlbezirkID);
	
	// check if votes are correct for this Wahlkreis
	boolean erststimmeValid = db.executeSQL("SELECT * FROM " + db.kandidat() + " k, " + db.partei() + " p " +  
		"WHERE k." + DB.kKandidatDMWahlkreisID + " = " + wk + " " + 
		"AND k." + DB.kKandidatDMParteiID + " = p." + DB.kID + " " +
		"AND p." + DB.kParteiKuerzel + " = '" + erststimme + "'").next();
	
	if (erststimmeValid == false) {
		json.put("success", false);
		json.put("error", "Abgegebene Stimme stimmt nicht mit dem im Wählpasswort verknüpften Wahlkreis antretenden Parteien überein. Stimme wurde nicht gezählt. Wählpasswort weiterhin gültig.");
	}
	else {
		db.executeUpdate("DELETE FROM " + db.sessionIDs() + " WHERE " + DB.kID + " = '" + sessionID + "'");
		
		//TODO insert statement
		
		json.put("success", true);
	}
}

out.print(json);
out.flush();


%>
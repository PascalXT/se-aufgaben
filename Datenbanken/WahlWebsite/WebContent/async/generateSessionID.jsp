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

String persoID = request.getParameter("persoID");
boolean found = db.executeSQL("" + 
	"SELECT * FROM " + db.wahlberechtigter() + " " + 
	"WHERE " + DB.kID + " = '" + persoID + "' " + 
	"AND " + DB.kWahlberechtigterGewaehlt + " = '0'"
).next();

JSONObject json = new JSONObject();

if (found == true) {
	String sessionID = UUID.randomUUID().toString();
	db.executeUpdate("INSERT INTO " + db.sessionIDs() + " VALUES '" + sessionID + "'");
	db.executeUpdate("" + 
		"UPDATE " + db.wahlberechtigter() + " " + 
		"SET " + DB.kWahlberechtigterGewaehlt + " = '1' " + 
		"WHERE " + DB.kID + " = '" + persoID + "'"
	);
	json.put("sessionID", sessionID);
} 

json.put("success", found);
out.print(json);
out.flush();
%>
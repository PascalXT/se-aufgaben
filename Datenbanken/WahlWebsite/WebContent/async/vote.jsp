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
String wk = request.getParameter("wk");
String erststimme = request.getParameter("erststimme");
String zweitstimme = request.getParameter("zweitstimme");

JSONObject json = new JSONObject();

boolean found = db.executeSQL("" + 
	"SELECT * FROM " + db.sessionIDs() + " WHERE " + DB.kID + " = '" + sessionID + "'"
).next();

if (found == false) {
	json.put("success", false);
	json.put("error", "Session-ID ungültig");
} else {
	db.executeUpdate("DELETE FROM " + db.sessionIDs() + " WHERE " + DB.kID + " = '" + sessionID + "'");
	json.put("success", true);
}

out.print(json);
out.flush();


%>
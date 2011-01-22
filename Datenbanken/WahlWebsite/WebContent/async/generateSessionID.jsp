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
String sessionID = UUID.randomUUID().toString();
db.executeUpdate("INSERT INTO " + db.sessionIDs() + " VALUES '" + sessionID + "'");
JSONObject json = new JSONObject();
json.put("sessionID", sessionID);
out.print(json);
out.flush();
%>
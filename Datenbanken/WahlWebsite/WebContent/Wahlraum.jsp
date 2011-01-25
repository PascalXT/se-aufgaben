<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	import="flags.Flags,flags.FlagDefinition,flags.FlagErrorException,database.DB,java.sql.*;"
	%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
String wk = request.getParameter("wk");
String wb = request.getParameter("wb");
%>
<title>Wahlraum Administration</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="css/screen.css" type="text/css" media="screen, projection">
<link rel="stylesheet" href="css/custom.css" type="text/css" media="screen, projection">
<script src="js/jquery-1.4.4.min.js" type="text/javascript"></script>
<script>
$(function(){
	
	$(".voters select option").dblclick(function() {
		$("input[name='persoID']").val($(this).val());
	});
	
	$(".createSessionID button").click(function() {
		$("#ajaxload").show();
		var persoID = $("input[name='persoID']").val();
		var wk = <%= wk %>;
		var wb = <%= wb %>;
		$.post('/WahlWebsite/async/generateSessionID.jsp', { persoID:persoID, wk:wk, wb:wb }, function(jsonResponse) {
			var json = jQuery.parseJSON(jsonResponse);
			if (json.success == true) {
				$("#sessionID").val(json.sessionID);
			} else {
				alert('Fehler ' + json.error);
			}
			$("#ajaxload").hide();
		});
	});
});
</script>
</head>
<body id="Wahlraum">
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

ResultSet wahlkreis = db.executeSQL("SELECT * FROM " + db.wahlkreis() + " WHERE " + DB.kID + " = " + wk);
wahlkreis.next();
%>
<h1>Wahlraum Administration</h1>
<h2>Wahlkreis: <%= wahlkreis.getString(DB.kID) %> <%= wahlkreis.getString(DB.kWahlkreisName) %></h2>
<h2>Wahlbezirk: <%= wb %></h2>
<hr/>

<div class="voters">
	<select multiple="multiple">
	<%
	ResultSet voters = db.executeSQL("SELECT * FROM " + db.wahlberechtigter() + " " + 
		"WHERE " + DB.kForeignKeyWahlbezirkID + " = " + wb + " " + 
		"AND " + DB.kForeignKeyWahlkreisID + " = " + wk);
	while (voters.next()) {
	%>
		<option><%= voters.getString(DB.kID) %></option>
	<% } %>
	</select>
</div>

<div class="createSessionID">
	<p> <label for="persoID">Personalausweis-Nummer:</label> <input name="persoID" type="text"/> </p>
	<p> <button type="button">Erzeuge Wählpasswort</button> </p>
	<p> <input id="sessionID" type="text"/> </p>
	
	<p id="ajaxload" style="display:none">
		<img src="/WahlWebsite/img/ajaxload.gif" alt=""/>
	</p>
	
</div>

<p>
<a href="/WahlWebsite/Wahlzettel.jsp?wk=<%= wk %>" target="_blank">zum Wahlzettel</a>
</p>

</body>

</html>


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	import="flags.Flags,flags.FlagDefinition,flags.FlagErrorException,database.DB,java.sql.*;"
	%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Wahllokal wählen</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="css/screen.css" type="text/css" media="screen, projection">
<link rel="stylesheet" href="css/custom.css" type="text/css" media="screen, projection">
<script src="js/jquery-1.4.4.min.js" type="text/javascript"></script>
<script>
$(function(){
	$("select[name='wahlkreis']").change(function() {
		var wk = $("select[name='wahlkreis'] option:selected").val();
		window.location = "/WahlWebsite/ChooseWahllokal.jsp?wk=" + wk;
	});
	$("select[name='wahlbezirk']").change(function() {
		var wk = $("select[name='wahlkreis'] option:selected").val();
		var wb = $("select[name='wahlbezirk'] option:selected").val();
		window.location = "/WahlWebsite/Wahlraum.jsp?wk=" + wk + "&wb=" + wb;
	});
});
</script>
</head>
<body id="ChooseWahllokal">
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
<h1>Wähle Wahllokal</h1>
<hr/>

<div>
	<label for="wahlkreis">Wahlkreis:</label>
	<select name="wahlkreis">
	<% 
	String wk = request.getParameter("wk");
	ResultSet resultSetWahlkreise = db.executeSQL("SELECT * FROM " + db.wahlkreis());
	while (resultSetWahlkreise.next()) { 
		String id = resultSetWahlkreise.getString(DB.kID);
	%>
		<option value="<%= id %>"<%= id.equals(wk) ? " selected=\"selected\"" : "" %>>
			<%= resultSetWahlkreise.getString(DB.kWahlkreisName) + " (" + id + ")" %>
		</option>
	<% 
	} // end while 
	%>
	</select>
</div>

<% 
if (wk != null) {
%>
<div>
	<label for="wahlbezirk">Wahlbezirk:</label>
	<select name="wahlbezirk">
	<% 
		String qry = "SELECT * FROM " + db.wahlbezirk() + " WHERE " + DB.kForeignKeyWahlkreisID + " = " + wk;
		ResultSet resultSetWahlbezirke = db.executeSQL(qry);
		while (resultSetWahlbezirke.next()) {
	%>
		<option value="<%= resultSetWahlbezirke.getString(DB.kID) %>">
			<%= resultSetWahlbezirke.getString(DB.kID) %>
		</option>
	<% 
		} // end while
	%>
	</select>
	</div>
<% 
} // end if 
%>


</body>

</html>


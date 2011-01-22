<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	import="flags.Flags,flags.FlagDefinition,flags.FlagErrorException,database.DB,java.sql.*;"
	%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Wahlzettel</title>
<%
String wk = request.getParameter("wk");
if (wk == null) {
	out.print("wk parameter not set!");
	System.exit(0);
}
%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="css/screen.css" type="text/css" media="screen, projection">
<link rel="stylesheet" href="css/custom.css" type="text/css" media="screen, projection">
<script src="js/jquery-1.4.4.min.js" type="text/javascript"></script>
<script>
$(function(){
	var btn = ".voteButton button";
	$(btn).click(function() {
		var wk = <%= wk %>;
		var erststimme = $("input[name='erststimme']:checked").val();
		var zweitstimme = $("input[name='zweitstimme']:checked").val();
		var sessionID = $("input[name='sessionID']").val();
		if (typeof erststimme === 'undefined' || typeof zweitstimme === 'undefined') {
			alert('Bitte beide Stimmen abgeben!');
		} else if (sessionID === '') {
			alert('Bitte Session-ID angeben!');
		} else {
			$(btn).hide();
			$("#ajaxload").show();
			$.post('/WahlWebsite/async/vote.jsp', {
				sessionID:sessionID, 
				wk:wk,
				erststimme:erststimme, 
				zweitstimme:zweitstimme
			}, function(jsonResponse) {
				var json = jQuery.parseJSON(jsonResponse);
				$("#ajaxload").hide();
				if (json.success === false) {
					$(btn).show();
					alert('Error: ' + success.error);
				} else {
					alert('Ihre Stimme wurde abgegeben');
				}
			});
		}
	});
});
</script>
</head>
<body id="Wahlzettel">
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
<img src="img/wahlzettel-header.png" alt=""/>
<h2 id="WahlkreisInfo">
für die Wahl zum Deutschen Bundestag im Wahlkreis <%= wahlkreis.getString(DB.kID) %> <%= wahlkreis.getString(DB.kWahlkreisName) %>
am 27. September 2009
</h2>
<img src="img/wahlzettel-subheader.png" alt=""/>
<%
ResultSet wahlzettel = db.executeSQL("" +
		"WITH " + 
		"ZweitstimmenParteien(ID, Kuerzel, Name) AS ( " + 
			"SELECT p." + DB.kID + ", p.Kuerzel, p.Name " + 
			"FROM " + db.wahlkreis() + " wk, " + db.bundesland() + " b, " + db.partei() + " p " + 
			"WHERE wk." + DB.kID + " = " + wk + " " + 
			"AND wk." + DB.kForeignKeyBundeslandID + " = b." + DB.kID + " " + 
			"AND EXISTS ( " + 
				"SELECT * FROM " + db.kandidat() + " k0 " + 
				"WHERE k0." + DB.kForeignKeyBundeslandID + " = b." + DB.kID + " " + 
				"AND k0." + DB.kKandidatDMParteiID + " = p." + DB.kID + " " + 
				"AND k0." + DB.kKandidatListenplatz + " IS NOT NULL " + 
			") " + 
		"), " + 
		"ErststimmenKandidaten(ParteiID, Vorname, Nachname) AS ( " + 
			"SELECT k." + DB.kKandidatDMParteiID + ", k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + " " + 
			"FROM " + db.kandidat() + " k " + 
			"WHERE k." + DB.kKandidatDMWahlkreisID + " = " + wk + " " + 
		") " + 
		"SELECT p.Kuerzel, p.Name, k.Vorname, k.Nachname " + 
		"FROM ZweitstimmenParteien p  " + 
		"FULL OUTER JOIN ErststimmenKandidaten k ON k.ParteiID = p.ID "
	);
%>

<div id="WahlzettelTableContainer">
	<table id="WahlzettelTable">
		<thead>
			<tr>
				<th colspan="3" id="erststimme">
					<div> <img src="img/erststimme-pfeil.png" alt=""/> </div>
					<div> <img src="img/erststimme-text.png" alt=""/> </div>
				</th>
				<th class="spacer">
					&nbsp;
				</th>
				<th colspan="3" id="zweitstimme">
					<div> <img src="img/zweitstimme-pfeil.png" alt=""/> </div>
					<div> <img src="img/zweitstimme-text.png" alt=""/> </div>
				</th>
			</tr>
		</thead>
		<tbody>
			<%
			for (int num = 1; wahlzettel.next(); num++) {
				String vorname = wahlzettel.getString("Vorname");
				String nachname = wahlzettel.getString("Nachname");
				String parteiKuerzel = wahlzettel.getString("Kuerzel");
				if (parteiKuerzel == null)
					parteiKuerzel = "Parteilos";
				String parteiName = wahlzettel.getString("Name");
			%>
			<tr>
				<td class="erstNum">
					<%= num %>
				</td>
				<td class="kandidat">
					<% if (vorname != null && nachname != null) { %>
						<h2> <strong><%= nachname %></strong>, <%= vorname %> </h2>
						<%= parteiKuerzel %> 
					<% } %>
				</td>
				<td class="radioErststimme">
					<% if (vorname != null && nachname != null) { %>
						<input type="radio" name="erststimme" value="<%= parteiKuerzel %>"/>
					<% } %>
				</td>
				<td class="spacer">
					&nbsp;
				</td>
				<td class="radioZweitstimme zweitstimmenBlau">
					<% if (parteiName != null) { %>
						<input type="radio" name="zweitstimme" value="<%= parteiKuerzel %>"/>
					<% } %>
				</td>
				<td class="partei zweitstimmenBlau">
					<% if (parteiName != null) { %>
						<h2> <strong><%= parteiKuerzel %></strong> </h2>
						<%= parteiName %>
					<% } %>
				</td>
				<td class="zweitNum">
					<%= num %>
				</td>
			</tr>
			<%	
			} // end for
			%>
		</tbody>
	</table>
</div>

<div class="voteButton">
	<label for="sessionID">Session ID:</label>
	<input name="sessionID" type="text"/>
	<p>Sobald geklickt, gibt es kein Zurück mehr!</p>
	<button type="button">Wählen</button>
	<p id="ajaxload" style="display:none">
		<img src="/WahlWebsite/img/ajaxload.gif" alt=""/>
	</p>
</div>

</body>
</html>
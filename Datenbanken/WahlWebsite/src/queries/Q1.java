package queries;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;

public class Q1 extends Query {
	
	public Q1() {
		super("");
	}
	
	public Q1(String headline) {
		super(headline);
	}
	
	@Override
	protected ResultSet doQuery() throws SQLException {
			
		String zweitStimmenNachBundeslandTable = createZweitStimmenNachBundeslandTable();
		String zweitStimmenNachParteiTable = createZweitStimmenNachParteiTable();

    database.printTable(zweitStimmenNachBundeslandTable);
    database.printTable(zweitStimmenNachParteiTable);

    database.printResultSet(database.executeSQL("SELECT " + Database.kForeignKeyParteiID + ", SUM("
        + Database.kAnzahlStimmen + ") FROM " + zweitStimmenNachBundeslandTable + " GROUP BY "
        + Database.kForeignKeyParteiID));

    String direktMandateTable = createDirektmandateTable();
	  database.printResultSet(database.executeSQL("SELECT COUNT(*) FROM " + direktMandateTable));

	  String fuenfProzentParteienTable = createFuenfProzentParteienTable(zweitStimmenNachBundeslandTable);
	  
	  String dreiDirektMandateParteienTable = createDreiDirektmandateParteienTable(direktMandateTable);

    database.printResultSet(database.executeSQL("SELECT p." + Database.kParteiKuerzel + " FROM " + database.partei()
        + " p, " + dreiDirektMandateParteienTable + " ddmp" + " WHERE p." + Database.kID + " = ddmp."
        + Database.kForeignKeyParteiID));

	  String parteienImBundestagTable = createParteienImBundestagTable(fuenfProzentParteienTable, dreiDirektMandateParteienTable);
	  
    String sitzeNachParteiTable = createSitzeNachParteiTable(zweitStimmenNachParteiTable, parteienImBundestagTable);
    
    String sitzeNachLandesListenTable = createSitzeNachLandeslistenTable(parteienImBundestagTable, zweitStimmenNachBundeslandTable, sitzeNachParteiTable);
    
    
    String qry = String.format("SELECT %s, SUM(%s) AS %s FROM %s sitze, %s partei WHERE sitze.%s = partei.%s GROUP BY %s", 
    		Database.kParteiKuerzel, 
    		Database.kAnzahlSitze, 
    		Database.kAnzahlSitze, 
    		sitzeNachLandesListenTable, 
    		database.partei(),
    		Database.kForeignKeyParteiID,
    		Database.kID,
    		Database.kParteiKuerzel);
    
    return database.executeSQL(qry);
	}
	
	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String tableRows = "<tr><th>Partei</th><th>Anzahl Sitze</th></tr>";
		
		int sum = 0;
		
		// see http://code.google.com/apis/chart/docs/gallery/pie_charts.html
		String chartUrl = "http://chart.apis.google.com/chart?cht=p&chs=420x250";
		String chartData = "&chd=t:";
		String chartLabels = "&chl=";
		while(resultSet.next()) {
			String partei = resultSet.getString(Database.kParteiKuerzel);
			int sitze = resultSet.getInt(Database.kAnzahlSitze);
			sum += sitze;
			tableRows += "<tr><td>" + partei + "</td><td>" + sitze + "</td></tr>";
			
			chartData += (sitze * 100 / 598) + ",";
			try {
				chartLabels += URLEncoder.encode(partei + " (" + sitze + ")", "UTF-8") + "|";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		tableRows += "<tr><td><strong>Summe</strong></td><td><strong>" + sum + "</strong></td></tr>";
		chartUrl += chartData.substring(0, chartData.length() - 1);
		chartUrl += chartLabels.substring(0, chartLabels.length() - 1);
	
		return "<p> <img src=\"" + chartUrl + "\" alt=\"\"/> </p>" + "<table>" + tableRows + "</table>";
	}
	
}

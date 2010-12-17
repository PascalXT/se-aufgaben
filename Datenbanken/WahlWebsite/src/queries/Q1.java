package queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import queries.GoogleChart.ChartType;
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
		
		List<Integer> data = new ArrayList<Integer>();
		List<String> labels = new ArrayList<String>();
		
		while(resultSet.next()) {
			String partei = resultSet.getString(Database.kParteiKuerzel);
			int sitze = resultSet.getInt(Database.kAnzahlSitze);
			sum += sitze;
			tableRows += "<tr><td>" + partei + "</td><td>" + sitze + "</td></tr>";
			
			data.add(sitze * 100 / 598);
			labels.add(partei + " (" + sitze + ")");
			

		}
		GoogleChart chart = new GoogleChart(ChartType.PIE, 400, 240, data, labels);
		
		tableRows += "<tr><td><strong>Summe</strong></td><td><strong>" + sum + "</strong></td></tr>";

		return "<p>" +  chart.getHtml() + "</p>" + "<table>" + tableRows + "</table>";
	}
	
}

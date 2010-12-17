package queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.Database;

public class Q5 extends Query {

	public Q5(String headline) {
		super(headline);

	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String[] headers = new String[] {"Bundesland", "Partei", "Überhangsmandate"};
		List<List<String>> rows = new ArrayList<List<String>>();
		
		while (resultSet.next()) {
			List<String> row = new ArrayList<String>();
			row.add(resultSet.getString(Database.kBundeslandName));
			row.add(resultSet.getString(Database.kParteiKuerzel));
			row.add(resultSet.getString(Database.kAnzahlUeberhangsmandate));
			rows.add(row);
		}
		
		Table table = new Table(headers, rows);
		
		return table.getHtml();
	}

	@Override
	protected ResultSet doQuery() throws SQLException {
		
		String zweitStimmenNachBundeslandTable = createZweitStimmenNachBundeslandTable();
		String zweitStimmenNachParteiTable = createZweitStimmenNachParteiTable();
    String direktMandateTable = createDirektmandateTable();
	  String fuenfProzentParteienTable = createFuenfProzentParteienTable(zweitStimmenNachBundeslandTable);  
	  String dreiDirektMandateParteienTable = createDreiDirektmandateParteienTable(direktMandateTable);
	  String parteienImBundestagTable = createParteienImBundestagTable(fuenfProzentParteienTable, dreiDirektMandateParteienTable);  
    String sitzeNachParteiTable = createSitzeNachParteiTable(zweitStimmenNachParteiTable, parteienImBundestagTable);
    String sitzeNachLandesListenTable = createSitzeNachLandeslistenTable(parteienImBundestagTable, zweitStimmenNachBundeslandTable, sitzeNachParteiTable);
    
		String ueberhangsmandateTable = createUeberhangsmandateTable(direktMandateTable, sitzeNachLandesListenTable);

		return db.executeSQL("SELECT * FROM " + ueberhangsmandateTable);
	}

}

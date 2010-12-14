package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;

public class Q2 extends Query {

	public Q2(String headline) {
		super(headline);
	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		// TODO Auto-generated method stub
		return null;
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
    
    
    database.executeSQL("SELECT * FROM ");
    
    return null;
	}

}

package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.DB;

public class Q2 extends Query {

	public Q2(String headline) {
		super(headline);
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
    
    String qry = "WITH Abgeordnete AS (";
    qry += String.format("SELECT %s FROM %s ", DB.kForeignKeyKandidatID, direktMandateTable);
    qry += "UNION ";
    qry += String.format("SELECT k.%s FROM %s k ", DB.kID, database.kandidat());
    qry += String.format("WHERE k.%s NOT IN (SELECT %s FROM %s) ", DB.kID, DB.kForeignKeyKandidatID, direktMandateTable);
    qry += String.format("AND k.%s <= ", DB.kKandidatListenplatz);
    qry += String.format("(SELECT SUM(AnzahlSitze) FROM %s s WHERE s.%s = k.%s AND s.%s = k.%s) ", 
    		sitzeNachLandesListenTable, 
    		DB.kForeignKeyParteiID, 
    		DB.kForeignKeyParteiID, 
    		DB.kForeignKeyBundeslandID, 
    		DB.kForeignKeyBundeslandID);
    qry += "- ";
    qry += String.format("(SELECT COUNT(*) FROM %s dm, %s k0 WHERE dm.%s = k.%s AND k0.%s = dm.%s AND k0.%s = k.%s) ", 
    		direktMandateTable, 
    		database.kandidat(), 
    		DB.kForeignKeyParteiID, 
    		DB.kForeignKeyParteiID, 
    		DB.kID, 
    		DB.kForeignKeyKandidatID, 
    		DB.kForeignKeyBundeslandID, 
    		DB.kForeignKeyBundeslandID);
    qry += ") ";
    qry += String.format("SELECT k.%s, k.%s, p.%s FROM Abgeordnete a, %s k, %s p WHERE a.%s = k.%s AND (k.%s IS NULL OR k.%s = p.%s) ORDER BY p.%s",
    		DB.kKandidatVorname, 
    		DB.kKandidatNachname, 
    		DB.kParteiKuerzel, 
    		database.kandidat(),
    		database.partei(),
    		DB.kForeignKeyKandidatID,
    		DB.kID,
    		DB.kForeignKeyParteiID,
    		DB.kForeignKeyParteiID,
    		DB.kID,
    		DB.kParteiKuerzel);

    return database.executeSQL(qry);
	}
	
	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String tableRows = "<tr><th></th><th>Abgeordneter</th><th>Partei</th></tr>";
		
		for(int seat = 1; resultSet.next(); seat++) {
			String name = resultSet.getString(DB.kKandidatVorname) + " " + resultSet.getString(DB.kKandidatNachname);
			String partei = resultSet.getString(DB.kParteiKuerzel);
			tableRows += "<tr><td>" + seat + "</td><td>" + name + "</td><td>" + partei + "</td></tr>";
		}
		
		return "<table>" + tableRows + "</table>";
		
		
	}

}

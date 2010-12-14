package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;

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
    qry += String.format("SELECT %s FROM %s ", Database.kForeignKeyKandidatID, direktMandateTable);
    qry += "UNION ";
    qry += String.format("SELECT k.%s FROM %s k ", Database.kID, database.kandidat());
    qry += String.format("WHERE k.%s NOT IN (SELECT %s FROM %s) ", Database.kID, Database.kForeignKeyKandidatID, direktMandateTable);
    qry += String.format("AND k.%s <= ", Database.kKandidatListenplatz);
    qry += String.format("(SELECT SUM(AnzahlSitze) FROM %s s WHERE s.%s = k.%s AND s.%s = k.%s) ", 
    		sitzeNachLandesListenTable, 
    		Database.kForeignKeyParteiID, 
    		Database.kForeignKeyParteiID, 
    		Database.kForeignKeyBundeslandID, 
    		Database.kForeignKeyBundeslandID);
    qry += "- ";
    qry += String.format("(SELECT COUNT(*) FROM %s dm, %s k0 WHERE dm.%s = k.%s AND k0.%s = dm.%s AND k0.%s = k.%s) ", 
    		direktMandateTable, 
    		database.kandidat(), 
    		Database.kForeignKeyParteiID, 
    		Database.kForeignKeyParteiID, 
    		Database.kID, 
    		Database.kForeignKeyKandidatID, 
    		Database.kForeignKeyBundeslandID, 
    		Database.kForeignKeyBundeslandID);
    qry += ") ";
    qry += String.format("SELECT k.%s, k.%s, p.%s FROM Abgeordnete a, %s k, %s p WHERE a.%s = k.%s AND (k.%s IS NULL OR k.%s = p.%s) ORDER BY p.%s",
    		Database.kKandidatVorname, 
    		Database.kKandidatNachname, 
    		Database.kParteiKuerzel, 
    		database.kandidat(),
    		database.partei(),
    		Database.kForeignKeyKandidatID,
    		Database.kID,
    		Database.kForeignKeyParteiID,
    		Database.kForeignKeyParteiID,
    		Database.kID,
    		Database.kParteiKuerzel);

    return database.executeSQL(qry);
	}
	
	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String tableRows = "<tr><th></th><th>Abgeordneter</th><th>Partei</th></tr>";
		
		for(int seat = 1; resultSet.next(); seat++) {
			String name = resultSet.getString(Database.kKandidatVorname) + " " + resultSet.getString(Database.kKandidatNachname);
			String partei = resultSet.getString(Database.kParteiKuerzel);
			tableRows += "<tr><td>" + seat + "</td><td>" + name + "</td><td>" + partei + "</td></tr>";
		}
		
		return "<table>" + tableRows + "</table>";
		
		
	}

}

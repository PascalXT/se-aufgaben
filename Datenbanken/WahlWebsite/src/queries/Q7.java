package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.DB;

public class Q7 extends Q3 {
	public Q7(String headline, int wahlkreisID) {
		super(headline, wahlkreisID);
	}

	String tempErstStimmenNachWahlkreis() {
		return db.tabellenName("TempErstStimmenNachWahlkreis");
	}

	String tempZweitStimmenNachWahlkreis() {
		return db.tabellenName("TempZweitStimmenNachWahlkreis");
	}
	
	protected String createTempZweitStimmenNachWahlkreisTable(int wahlkreisID) throws SQLException {
	 	db.createOrReplaceTemporaryTable(tempZweitStimmenNachWahlkreis(), DB.kForeignKeyParteiID + " BIGINT, "
	 			+ DB.kForeignKeyWahlkreisID + " BIGINT, " + DB.kJahr + " BIGINT, " + DB.kAnzahl + " BIGINT");
    db.executeUpdate(""
    		+ "INSERT INTO " + tempZweitStimmenNachWahlkreis() + "(" + DB.kForeignKeyParteiID + ", "
    			+ DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", " + DB.kAnzahl + ") "
    		+ "SELECT s." + DB.kForeignKeyParteiID + ", s." + DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", COUNT(*) "
    		+ "FROM " + db.stimme() + " s "
    		+ "WHERE " + DB.kForeignKeyParteiID + " is not null "
    			+ "AND s." + DB.kForeignKeyWahlkreisID + "=" + wahlkreisID + " "
    		+ "GROUP BY s." + DB.kForeignKeyParteiID + ", s." + DB.kForeignKeyWahlkreisID + ", s." + DB.kJahr);
    return tempZweitStimmenNachWahlkreis();
	}
	
	protected String createTempErstStimmenNachWahlkreisTable(int wahlkreisID) throws SQLException {
	 	db.createOrReplaceTemporaryTable(tempErstStimmenNachWahlkreis(), DB.kForeignKeyKandidatID + " BIGINT, "
	 			+ DB.kForeignKeyWahlkreisID + " BIGINT, " + DB.kJahr + " BIGINT, " + DB.kAnzahl + " BIGINT");
    db.executeUpdate(""
    		+ "INSERT INTO " + tempErstStimmenNachWahlkreis() + "(" + DB.kForeignKeyKandidatID + ", "
    			+ DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", " + DB.kAnzahl + ") "
    		+ "SELECT s." + DB.kForeignKeyKandidatID + ", s." + DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", COUNT(*) "
    		+ "FROM " + db.stimme() + " s "
    		+ "WHERE " + DB.kForeignKeyKandidatID + " is not null "
    			+ "AND s." + DB.kForeignKeyKandidatID + "=" + wahlkreisID + " "
    		+ "GROUP BY s." + DB.kForeignKeyKandidatID + ", s." + DB.kForeignKeyWahlkreisID + ", s." + DB.kJahr);
    return tempErstStimmenNachWahlkreis();
	}
	
	@Override
	protected ResultSet doQuery() throws SQLException {
		// Aggregate Wahlkreis data.	
		//TODO: WahlkreisDaten Tabelle muss ebenfalls dynamisch erstellt werden können.
		return doQuery(createTempErstStimmenNachWahlkreisTable(wahlkreisID),
				createTempZweitStimmenNachWahlkreisTable(wahlkreisID));
	}
}

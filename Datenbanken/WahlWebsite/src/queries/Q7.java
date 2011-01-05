package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.DB;

public class Q7 extends Q3 {
	public Q7(String headline, int wahlkreisID) {
		super(headline, wahlkreisID);
	}

	final String kTempZweitStimmenNachWahlkreis = db.tabellenName("TempZweitStimmenNachWahlkreis");
	final String kTempErstStimmenNachWahlkreis = db.tabellenName("kTempErstStimmenNachWahlkreis");
	
	protected String createTempZweitStimmenNachWahlkreisTable() throws SQLException {
	 	db.createOrReplaceTemporaryTable(kTempZweitStimmenNachWahlkreis, DB.kForeignKeyParteiID + " BIGINT, "
	 			+ DB.kForeignKeyWahlkreisID + " BIGINT, " + DB.kJahr + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
    db.executeUpdate(""
    		+ "INSERT INTO " + kTempZweitStimmenNachWahlkreis + "(" + DB.kForeignKeyParteiID + ", "
    			+ DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", " + DB.kAnzahlStimmen + ") "
    		+ "SELECT s." + DB.kForeignKeyParteiID + ", s." + DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", COUNT(*) "
    		+ "FROM " + db.stimme() + " s "
    		+ "GROUP BY s." + DB.kForeignKeyParteiID + ", s." + DB.kForeignKeyWahlkreisID + ", s." + DB.kJahr);
    return kTempZweitStimmenNachWahlkreis;
	}
	
	protected String createTempErstStimmenNachWahlkreisTable() throws SQLException {
	 	db.createOrReplaceTemporaryTable(kTempErstStimmenNachWahlkreis, DB.kForeignKeyKandidatID + " BIGINT, "
	 			+ DB.kForeignKeyWahlkreisID + " BIGINT, " + DB.kJahr + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
    db.executeUpdate(""
    		+ "INSERT INTO " + kTempErstStimmenNachWahlkreis + "(" + DB.kForeignKeyKandidatID + ", "
    			+ DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", " + DB.kAnzahlStimmen + ") "
    		+ "SELECT s." + DB.kForeignKeyKandidatID + ", s." + DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", COUNT(*) "
    		+ "FROM " + db.stimme() + " s "
    		+ "GROUP BY s." + DB.kForeignKeyKandidatID + ", s." + DB.kForeignKeyWahlkreisID + ", s." + DB.kJahr);
    return kTempErstStimmenNachWahlkreis;
	}
	
	@Override
	protected ResultSet doQuery() throws SQLException {
		// Aggregate Wahlkreis data.	
		return doQuery(createTempErstStimmenNachWahlkreisTable(), createTempZweitStimmenNachWahlkreisTable());
	}
}

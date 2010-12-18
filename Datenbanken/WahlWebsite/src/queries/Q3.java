package queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import database.DB;

public class Q3 extends Query {
	
	private int wahlkreisID;
	
	private String wahlkreisName;
	
	private List<List<String>> q2rows;
	private List<List<String>> q3rows;
	
	public Q3(String headline, int wahlkreisID) {
		super(headline);
		this.wahlkreisID = wahlkreisID;
	}
	
	@Override
	protected ResultSet doQuery() throws SQLException {
		
		ResultSet rsWahlkreisName = db.executeSQL("" + 
				"SELECT " + DB.kWahlkreisName + " FROM " + db.wahlkreis() + " " +
				"WHERE " + DB.kID + " = " + wahlkreisID
		); 
		rsWahlkreisName.next();
		wahlkreisName = rsWahlkreisName.getString(DB.kWahlkreisName);
		
    String direktMandateTable = createDirektmandateTable();
    
		// Q3.1 - Wahlbeteiligung
    
		// TODO
    
		// Q3.2 - gewählter Direktkandidat
		
    ResultSet rs2 = db.executeSQL("" +
    		"SELECT k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + ", p." + DB.kParteiKuerzel + " " + 
    		"FROM " + direktMandateTable + " dm, " + db.kandidat() + " k, " + db.partei() + " p " + 
    		"WHERE dm." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " + 
    		"AND dm." + DB.kForeignKeyParteiID + " = p." + DB.kID + " " + 
    		"AND dm." + DB.kKandidatDMWahlkreisID + " = " + wahlkreisID + " "
    );
    q2rows = new ArrayList<List<String>>();
    rs2.next();
    q2rows.add(new ArrayList<String>(Arrays.asList(new String[] {
    		rs2.getString(DB.kKandidatVorname) + " " + rs2.getString(DB.kKandidatNachname),
    		rs2.getString(DB.kParteiKuerzel)
    })));

    
		// Q3.3 - prozentuale und absolute Anzahl an Stimmen für jede Partei
		
    ResultSet rs3 = db.executeSQL("" + 
    		"WITH ZweitStimmenWahlkreis AS ( " + 
    			"SELECT " + DB.kForeignKeyParteiID + ", " + DB.kWahlergebnis2Anzahl + " " +
    			"FROM " + db.wahlergebnis2() + " WHERE " + DB.kForeignKeyWahlkreisID + " = " + wahlkreisID + " " + 
    		"), " +
    		"SummeZweitStimmenWahlkreis AS ( " + 
    			"SELECT SUM(" + DB.kWahlergebnis2Anzahl + ") AS Summe " + 
    			"FROM " + db.wahlergebnis2() + " WHERE " + DB.kForeignKeyWahlkreisID + " = " + wahlkreisID + " " + 
    			"GROUP BY " + DB.kForeignKeyWahlkreisID + " " + 
    		") " +
    		"SELECT " + 
    			"p." + DB.kParteiKuerzel + ", " +
    			"COALESCE(w2." + DB.kWahlergebnis2Anzahl + ", 0) AS Absolut, " +
    			"CAST(COALESCE(w2." + DB.kWahlergebnis2Anzahl + ", 0) AS FLOAT) / (SELECT Summe FROM SummeZweitStimmenWahlkreis) AS Prozentual " +
    		"FROM ZweitStimmenWahlkreis w2 RIGHT OUTER JOIN " + db.partei() + " p ON p." + DB.kID + " = w2." + DB.kForeignKeyParteiID + " " +
    		"ORDER BY Absolut DESC"
    );
    
		q3rows = new ArrayList<List<String>>();
		while(rs3.next()) {
			List<String> row = new ArrayList<String>();
			row.add(rs3.getString(DB.kParteiKuerzel));
			row.add(String.valueOf(rs3.getInt("Absolut")));
			row.add(String.valueOf(rs3.getFloat("Prozentual")));
			q3rows.add(row);
		}
    
		// Q3.4 - Vergleich Stimmen zum Vorjahr
		
		// TODO
		
		return null;
	}
	
	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String html = "<h2>Wahlkreis " + wahlkreisName + " (" + wahlkreisID + ")</h2>";
		
		html += "<h3>Gewählter Direktkandidat</h3>";
    String[] q2headers = new String[] {"Name", "Partei"};
		Table q2table = new Table(q2headers, q2rows);
		html += q2table.getHtml();
    
		html += "<h3>Stimmen für jede Partei</h3>";
    String[] q3headers = new String[] {"Partei", "Stimmen Absolut", "Stimmen Prozentual"};
    Table q3table = new Table(q3headers, q3rows);
    html += q3table.getHtml();
    
    
		return html;
	}

	

}

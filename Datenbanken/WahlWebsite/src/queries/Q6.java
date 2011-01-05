package queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DB;

public class Q6 extends Query {

	public Q6(String headline) {
		super(headline);
	}

	@Override
	protected ResultSet doQuery() throws SQLException {
		return db.executeSQL("" +
				"WITH " +
				"MaxStimmen(WahlkreisID, Anzahl) AS ( " +
					"SELECT we.WahlkreisID, MAX(we.Anzahl) " +
					"FROM " + db.erstStimmenNachWahlkreis() + " we " +
					"WHERE we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"GROUP BY we.WahlkreisID " +
				"), " +
				"Erster(WahlkreisID, KandidatID, ParteiID, Anzahl) AS ( " +
					"SELECT we.WahlkreisID, we.KandidatID, k.ParteiID, we.Anzahl " +
					"FROM " + db.erstStimmenNachWahlkreis() + " we, MaxStimmen ms, " + db.kandidat() + " k " +
					"WHERE we.WahlkreisID = ms.WahlkreisID " +
					"AND we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"AND we.KandidatID = k.ID " +
					"AND we.Anzahl = ms.Anzahl " +
				"), " +
				"RestKandidaten(KandidatID) AS ( " +
					"SELECT KandidatID " +
					"FROM " + db.erstStimmenNachWahlkreis() + " we " + 
					"WHERE we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"EXCEPT " +
					"SELECT KandidatID FROM Erster " +
				"), " +
				"MaxStimmenRest(WahlkreisID, Anzahl) AS ( " +
					"SELECT we.WahlkreisID, MAX(we.Anzahl) " +
					"FROM " + db.erstStimmenNachWahlkreis() + " we, RestKandidaten r " +
					"WHERE we.KandidatID = r.KandidatID " +
						"AND we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"GROUP BY we.WahlkreisID " +
				"), " +
				"Zweiter(WahlkreisID, KandidatID, ParteiID, Anzahl) AS ( " +
					"SELECT k.DMWahlkreisID, k.ID, k.ParteiID, we.Anzahl " +
					"FROM " + db.kandidat() + " k, " + db.erstStimmenNachWahlkreis() + " we, MaxStimmenRest ms " +
					"WHERE we.Anzahl = ms.Anzahl " +
					"AND we.WahlkreisID = ms.WahlkreisID " +
					"AND we.KandidatID = k.ID " +
					"AND we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
				"), " +
				"KnappsteSieger(GewinnerID, Differenz, VerliererID, WahlkreisID) AS ( " +
					"SELECT e.ParteiID, e.Anzahl - z.Anzahl AS Differenz, z.ParteiID, e.WahlkreisID " +
					"FROM Erster e, Zweiter z " +
					"WHERE e.WahlkreisID = z.WahlkreisID " +
					"ORDER BY e.ParteiID, Differenz " +
				"), " +
				"KnappsteSiegerRang(Rang, GewinnerID, Differenz, VerliererID, WahlkreisID) AS ( " +
					"SELECT ROW_NUMBER() OVER (PARTITION BY kn.GewinnerID ORDER BY kn.Differenz), " +
						"kn.GewinnerID, kn.Differenz, kn.VerliererID, kn.WahlkreisID " +
					"FROM KnappsteSieger kn " +
				"), " +
				"ParteienOhneSieg(ParteiID) AS ( " +
					"SELECT ID FROM " + db.partei() + " " +
					"EXCEPT " +
					"SELECT GewinnerID FROM KnappsteSiegerRang " +
				"), " +
				"KnappsteSiegerOutput(Rang, Vorname, Nachname, Partei, Wahlkreis, Differenz, Typ) AS ( " +
						"SELECT knr.Rang, k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + ", p." + DB.kParteiKuerzel + ", wk." + DB.kWahlkreisName + ", knr.Differenz, 'Gewinner' " +
						"FROM KnappsteSiegerRang knr, " + db.partei() + " p, " + db.kandidat() + " k, " + db.wahlkreis() + " wk " +
						"WHERE Rang <= 10 AND knr.GewinnerID = p." + DB.kID + " " +
						"AND knr.WahlkreisID = k." + DB.kKandidatDMWahlkreisID + " " +
						"AND k." + DB.kForeignKeyParteiID + " = p." + DB.kID + " " + 
						"AND wk." + DB.kID + " = knr.WahlkreisID " +
				"), " +
				"KnappsteVerlierer(ParteiID, KandidatID, AbstandZumErsten, WahlkreisID) AS ( " +
					"SELECT pos.ParteiID, k.ID, e.Anzahl - we." + DB.kAnzahl + ", w." + DB.kID + " " +
					"FROM ParteienOhneSieg pos, Erster e, " + db.wahlkreis() + " w, " + db.erstStimmenNachWahlkreis() + " we, " + db.kandidat() + " k " +
					"WHERE we." + DB.kForeignKeyWahlkreisID + " = w." + DB.kID + " " +
					"AND we." + DB.kWahlergebnis1Jahr + " = " + kCurrentElectionYear + " " +
					"AND e.WahlkreisID = w." + DB.kID + " " +
					"AND we." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " +
					"AND k." + DB.kKandidatDMWahlkreisID + " = w." + DB.kID + " " +
					"AND k." + DB.kForeignKeyParteiID + " = pos.ParteiID " +
				"), " +
				"KnappsteVerliererRang(Rang, ParteiID, KandidatID, AbstandZumErsten, WahlkreisID) AS ( " +
					"SELECT ROW_NUMBER() OVER (PARTITION BY kv.ParteiID ORDER BY kv.AbstandZumErsten ASC), kv.ParteiID, kv.KandidatID, kv.AbstandZumErsten, kv.WahlkreisID " +
					"FROM KnappsteVerlierer kv " + 
				"), " +
				"KnappsteVerliererOutput(Rang, Vorname, Nachname, Partei, Wahlkreis, Differenz, Typ) AS ( " +
					"SELECT kvr.Rang, k." + DB.kKandidatVorname + ", k." + DB.kKandidatNachname + ", p." + DB.kParteiKuerzel + ", wk." + DB.kWahlkreisName + ", kvr.AbstandZumErsten, 'Verlierer' " +
					"FROM KnappsteVerliererRang kvr, " + db.partei() + " p, " + db.kandidat() + " k, " + db.wahlkreis() + " wk " +
					"WHERE Rang <= 10 " + 
					"AND kvr.ParteiID = p." + DB.kID + " " +
					"AND kvr.WahlkreisID = k." + DB.kKandidatDMWahlkreisID + " " +
					"AND k." + DB.kForeignKeyParteiID + " = p." + DB.kID + " " +
					"AND wk." + DB.kID + " = kvr.WahlkreisID " +
				"), " +
				"GewinnerUndVerliererOutput AS ( " +
					"SELECT * FROM KnappsteSiegerOutput " +
					"UNION ALL " +
					"SELECT * FROM KnappsteVerliererOutput " + 
				") " +
				"SELECT * FROM GewinnerUndVerliererOutput ORDER BY Typ"
		);
	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String html = "<h2>Knappste Gewinner</h2>";
		
		String[] headersGewinner = new String[] {"Partei", "Rang", "Kandidat", "Wahlkreis", "Abstand zum Zweiten"};
		List<List<String>> rowsGewinner = new ArrayList<List<String>>();

		while (resultSet.next()) {
			if ("Verlierer".equals(resultSet.getString("Typ")))
				break;
			List<String> row = new ArrayList<String>();
			row.add(resultSet.getString("Partei"));
			row.add(resultSet.getString("Rang"));
			row.add(resultSet.getString(DB.kKandidatVorname) + " " + resultSet.getString(DB.kKandidatNachname));
			row.add(resultSet.getString("Wahlkreis"));
			row.add(resultSet.getString("Differenz"));
			rowsGewinner.add(row);
		}
		html += new Table(headersGewinner, rowsGewinner).getHtml();

		
		html += "<h2>Knappste Verlierer</h2>";
		String[] headersVerlierer = new String[] {"Partei", "Rang", "Kandidat", "Wahlkreis", "Abstand zum Gewinner"};
		List<List<String>> rowsVerlierer = new ArrayList<List<String>>();

		do {
			List<String> row = new ArrayList<String>();
			row.add(resultSet.getString("Partei"));
			row.add(resultSet.getString("Rang"));
			row.add(resultSet.getString(DB.kKandidatVorname) + " " + resultSet.getString(DB.kKandidatNachname));
			row.add(resultSet.getString("Wahlkreis"));
			row.add(resultSet.getString("Differenz"));
			rowsVerlierer.add(row);
		} while (resultSet.next());
		
		html += new Table(headersVerlierer, rowsVerlierer).getHtml();
		
		return html;
	}

}

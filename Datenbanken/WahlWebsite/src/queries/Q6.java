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
					"FROM " + db.wahlergebnis1() + " we " +
					"WHERE we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"GROUP BY we.WahlkreisID " +
				"), " +
				"Erster(WahlkreisID, KandidatID, ParteiID, Anzahl) AS ( " +
					"SELECT we.WahlkreisID, we.KandidatID, k.ParteiID, we.Anzahl " +
					"FROM " + db.wahlergebnis1() + " we, MaxStimmen ms, " + db.kandidat() + " k " +
					"WHERE we.WahlkreisID = ms.WahlkreisID " +
					"AND we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"AND we.KandidatID = k.ID " +
					"AND we.Anzahl = ms.Anzahl " +
				"), " +
				"RestKandidaten(KandidatID) AS ( " +
					"SELECT KandidatID " +
					"FROM " + db.wahlergebnis1() + " " + 
					"WHERE we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"EXCEPT " +
					"SELECT KandidatID FROM Erster " +
				"), " +
				"MaxStimmenRest(WahlkreisID, Anzahl) AS ( " +
					"SELECT we.WahlkreisID, MAX(we.Anzahl) " +
					"FROM " + db.wahlergebnis1() + " we, RestKandidaten r " +
					"WHERE we.KandidatID = r.KandidatID " +
						"AND we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
					"GROUP BY we.WahlkreisID " +
				"), " +
				"Zweiter(WahlkreisID, KandidatID, ParteiID, Anzahl) AS ( " +
					"SELECT k.DMWahlkreisID, k.ID, k.ParteiID, we.Anzahl " +
					"FROM " + db.kandidat() + " k, " + db.wahlergebnis1() + " we, MaxStimmenRest ms " +
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
				"ParteienOhneSieg AS ( " + // TODO: benutzen um knappste Verlierer für Parteien ohne Erststimmen-Sieg zu bestimmen
					"SELECT ID FROM " + db.partei() + " " +
					"EXCEPT " +
					"SELECT GewinnerID FROM KnappsteSiegerRang " +
				") " +
				"SELECT knr.Rang, k.Vorname, k.Nachname, p.Kuerzel AS Partei, wk.Name AS Wahlkreis, knr.Differenz AS Vorsprung " + 
				"FROM KnappsteSiegerRang knr, " + db.partei() + " p, " + db.kandidat() + " k, " + db.wahlkreis() + " wk " +
				"WHERE Rang <= 10 " +
				"AND knr.GewinnerID = p.ID " +
				"AND knr.WahlkreisID = k.DMWahlkreisID " +
				"AND k.ParteiID = p.ID " +
				"AND wk.ID = knr.WahlkreisID"
		);
	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		String[] headers = new String[] {"Rang", "Gewinner", "Partei", "Wahlkreis", "Vorsprung"};
		List<List<String>> rows = new ArrayList<List<String>>();
		
		while (resultSet.next()) {
			List<String> row = new ArrayList<String>();
			row.add(resultSet.getString("Partei"));
			row.add(resultSet.getString("Rang"));
			row.add(resultSet.getString(DB.kKandidatVorname) + " " + resultSet.getString(DB.kKandidatNachname));
			row.add(resultSet.getString("Wahlkreis"));
			row.add(resultSet.getString("Vorsprung"));
			rows.add(row);
		}
		
		Table table = new Table(headers, rows);
		
		return table.getHtml();
	}

}

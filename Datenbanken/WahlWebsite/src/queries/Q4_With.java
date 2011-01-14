package queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import queries.GoogleChart.ChartType;

public class Q4_With extends Query {

	public Q4_With(String headline) {
		super(headline);
	}

	@Override
	protected ResultSet doQuery() throws SQLException {
		return db.executeSQL("WITH "
				+ db.maxZweitStimmenNachWahlkreis() + " AS (" + stmtMaxZweitStimmenNachWahlkreis() + "), "
				+ db.maxErststimmenNachWahlkreis() + " AS ("
					+ stmtMaxErststimmenNachWahlkreis(db.erstStimmenNachWahlkreis()) + "), "
				+ db.gewinnerZweitstimmen() + " AS (" + stmtGewinnerZweitStimmen(db.maxZweitStimmenNachWahlkreis()) + "), "
				+ db.gewinnerErststimmen() + " AS (" + stmtGewinnerErststimmen(db.maxErststimmenNachWahlkreis()) + "), "
				+ db.wahlkreisSieger() + " AS ("
					+ stmtWahlkreissieger(db.gewinnerErststimmen(), db.gewinnerZweitstimmen()) + ")"
				+ "SELECT * FROM " + db.wahlkreisSieger());
	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String[] tableHeaders = new String[] { "Wahlkreis", "Gewinner Erststimmen", "Gewinner Zweitstimmen" };
		List<List<String>> rows = new ArrayList<List<String>>();
		
		while (resultSet.next()) {
			List<String> row = new ArrayList<String>();
			row.add(String.valueOf(resultSet.getInt("WahlkreisID")));
			row.add(resultSet.getString("P1"));
			row.add(resultSet.getString("P2"));
			rows.add(row);
		}
		
		Table table = new Table(tableHeaders, rows);
		
		// SPECIAL - eingefärbte Deutschlandkarte
		Map<String, String> colorMapping = new HashMap<String, String>();
		colorMapping.put("CDU", "000000");
		colorMapping.put("SPD", "FF0000");
		colorMapping.put("CSU", "00FFFF");
		colorMapping.put("DIE LINKE", "FF00FF");
		
		Map<Integer, String> bundeslandMapping = new HashMap<Integer, String>();
		bundeslandMapping.put(1, "DE-BW");
		bundeslandMapping.put(2, "DE-BY");
		bundeslandMapping.put(3, "DE-BE");
		bundeslandMapping.put(4, "DE-BB");
		bundeslandMapping.put(5, "DE-HB");
		bundeslandMapping.put(6, "DE-HH");
		bundeslandMapping.put(7, "DE-HE");
		bundeslandMapping.put(8, "DE-MV");
		bundeslandMapping.put(9, "DE-NI");
		bundeslandMapping.put(10, "DE-NW");
		bundeslandMapping.put(11, "DE-RP");
		bundeslandMapping.put(12, "DE-SL");
		bundeslandMapping.put(13, "DE-SN");
		bundeslandMapping.put(14, "DE-ST");
		bundeslandMapping.put(15, "DE-SH");
		bundeslandMapping.put(16, "DE-TH");
		
		ResultSet rs = db.executeSQL("" +
			"WITH " + 
			"GewinnerErststimmen(BundeslandID, Partei, GewonneneWahlkreise) AS ( " + 
				"SELECT BundeslandID, P1, COUNT(*) FROM " + db.wahlkreisSieger() + " GROUP BY BundeslandID, P1 " + 
			"), " + 
			"GewinnerZweitStimmen(BundeslandID, Partei, GewonneneWahlkreise) AS ( " + 
				"SELECT BundeslandID, P2, COUNT(*) FROM " + db.wahlkreisSieger() + " GROUP BY BundeslandID, P2 " + 
			"), " + 
			"GewinnerGesamt(BundeslandID, Partei, GewonneneWahlkreise) AS ( " + 
				"SELECT g1.BundeslandID, g1.Partei, g1.GewonneneWahlkreise + g2.GewonneneWahlkreise " + 
				"FROM GewinnerErststimmen g1, GewinnerZweitStimmen g2 " + 
			") " + 
			"SELECT g.BundeslandID, g.Partei " + 
			"FROM GewinnerGesamt g WHERE NOT EXISTS ( " + 
				"SELECT * FROM GewinnerGesamt g0 " + 
				"WHERE g0.BundeslandID = g.BundeslandID " + 
			 	"AND g0.GewonneneWahlkreise > g.GewonneneWahlkreise " + 
			") "
		);
		
		List<String> labels = new ArrayList<String>(); 
		List<String> colors = new ArrayList<String>(); 
		
		colors.add("FFFFFF"); // first value is background color
		
		while (rs.next()) {
			labels.add(bundeslandMapping.get(rs.getInt("BundeslandID")));
			colors.add(colorMapping.get(rs.getString("Partei")));
		}
		
		GoogleChart chart = new GoogleChart(ChartType.MAP, 300, 500);
		chart.setLabels(labels);
		chart.setColors(colors);
		
		String html = "<h2>Deutschlandkarte</h2><p>Die meisten gewonnenen Wahlkreise in einem Bundesland:</p>";
		
		// Legende
		String[] legendHeaders = new String[] {"Partei", "Farbe"};
		List<List<String>> legendRows = new ArrayList<List<String>>();
		for (String partei : colorMapping.keySet()) {
			List<String> row = new ArrayList<String>();
			row.add(partei);
			row.add("<div style=\"background-color:#" + colorMapping.get(partei) + "\">&nbsp;</div>");
			legendRows.add(row);
		}
		
		html += new Table(legendHeaders, legendRows).getHtml();
		html += "<p>" + chart.getHtml() + "</p> <p><h2>Wahlkreissieger</h2>" + table.getHtml() + "</p>";
		
		return html;
	}

}

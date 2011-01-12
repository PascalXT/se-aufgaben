package queries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import queries.GoogleChart.ChartType;
import database.DB;


public class Q1_WITH extends Query {
	
	public Q1_WITH(String headline) {
		super(headline);
	}
	
	@Override
	protected ResultSet doQuery() throws SQLException {
		LineNumberReader lr = null;
		try {
			FileReader fr = new FileReader(new File("H:\\Q1.sql"));
			lr = new LineNumberReader(fr);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		String query = "";
		try {
			final int maxNLines = 10000;
			for (int i = 0; i < maxNLines; i++) {
				String nextLine = lr.readLine();
				if (nextLine == null) break;
				query += nextLine + "\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			

		System.out.println(query);
    return db.executeSQL(query);
	}
	
	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		
		String[] tableHeaders = new String[] { "Partei", "Anzahl Sitze" };
		List<List<String>> rows = new ArrayList<List<String>>();
		
		int sum = 0;
		
		List<Integer> data = new ArrayList<Integer>();
		List<String> labels = new ArrayList<String>();
		
		Map<String, String> colorMapping = new HashMap<String, String>();
		colorMapping.put("CDU", "000000");
		colorMapping.put("SPD", "FF0000");
		colorMapping.put("CSU", "00FFFF");
		colorMapping.put("FDP", "FFFF00");
		colorMapping.put("GRÜNE", "00FF00");
		colorMapping.put("DIE LINKE", "FF00FF");
		
		List<String> colors = new ArrayList<String>();
		
		while(resultSet.next()) {
			String partei = resultSet.getString(DB.kParteiKuerzel);
			int sitze = resultSet.getInt(DB.kAnzahlSitze);
			sum += sitze;
			
			List<String> row = new ArrayList<String>();
			row.add(partei);
			row.add(String.valueOf(sitze));
			rows.add(row);
			
			data.add(sitze * 100 / 598);
			labels.add(partei + " (" + sitze + ")");
			colors.add(colorMapping.get(partei));
		}
		
		
		
		GoogleChart chart = new GoogleChart(ChartType.PIE, 400, 240);
		chart.setData(data);
		chart.setLabels(labels);
		chart.setColors(colors);
		List<String> finalRow = new ArrayList<String>();
		finalRow.add("Summe");
		finalRow.add(String.valueOf(sum));
		rows.add(finalRow);
		
		Table table = new Table(tableHeaders, rows);
		return "<p>" +  chart.getHtml() + "</p>" + table.getHtml();
	}
	
}

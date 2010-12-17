package queries;

import java.util.List;

public class Table {
	
	private String tableRows = "";
	
	public Table(String[] headers, List<List<String>> rows) {
		
		for (String header : headers) {
			tableRows += "<th>" + header + "</th>";
		}
		tableRows = "<tr>" + tableRows + "</tr>";
		for (List<String> row : rows) {
			tableRows += "<tr>";
			for (String cell : row) {
				tableRows += "<td>" + cell + "</td>";
			}
			tableRows += "</tr>";
		}
		
	}
	
	public String getHtml() {
		return "<table>" + tableRows + "</table>";
	}
	
}

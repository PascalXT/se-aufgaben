package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.Database;

public abstract class Query {
	
	protected String headline;
	
	protected Database database;
	
	public Query(String headline) {
		this.headline = headline;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public String generateHtmlOutput() {
		try {
			ResultSet resultSet = doQuery();
			String body = generateBody(resultSet);
			return "<html><body><h1>" + headline + "</h1>" + body + "</body></html>";
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected abstract String generateBody(ResultSet resultSet) throws SQLException;
	
	protected abstract ResultSet doQuery() throws SQLException;
	
}

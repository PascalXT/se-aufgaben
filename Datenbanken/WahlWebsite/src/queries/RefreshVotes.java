package queries;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RefreshVotes	extends	Query {

	public RefreshVotes(String headline) {
		super(headline);
	}

	@Override
	protected ResultSet doQuery() throws SQLException {
		String erstStimmenNachWahlkreisTable = updateErststimmenNachWahlkreisTable();
		updateZweitstimmenNachWahlkreisTable(erstStimmenNachWahlkreisTable);
		return null;
	}

	@Override
	protected String generateBody(ResultSet resultSet) throws SQLException {
		return "Stimmen wurden aggregiert.";
	}

}

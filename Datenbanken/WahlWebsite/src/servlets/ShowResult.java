package servlets;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import queries.Q1;
import queries.Q1_WITH;
import queries.Q2;
import queries.Q2_WITH;
import queries.Q3;
import queries.Q3_WITH;
import queries.Q4;
import queries.Q4_With;
import queries.Q5;
import queries.Q5_WITH;
import queries.Q6;
import queries.Q7;
import queries.Q7_WITH;
import queries.Query;
import queries.RefreshVotes;
import database.DB;
import flags.FlagDefinition;
import flags.FlagErrorException;
import flags.Flags;

@WebServlet("/ShowResult")
public class ShowResult extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Query query;
	
    public ShowResult() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String configFile = gitIgnore.Config.getConfigFile();
		String[] args = {configFile};
		try {
			Flags.setFlags(FlagDefinition.kFlagDefinition, args);
		} catch (FlagErrorException e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		String queryParam = request.getParameter("query");
		if (queryParam == null) {
			response.getWriter().write("query parameter missing");
			return;
		}
		
		DB database = null;
		try {
			database = DB.getDatabaseByFlags();
		} catch (FlagErrorException e) {
			e.printStackTrace();
			System.exit(0);
		}

		if (request.getParameter("mqt") != null) {
			database.MQTmode = true;
		} else {
			database.MQTmode = false;
		}
		
		query = null;
		if (queryParam.equalsIgnoreCase("Q1")) {
			query = new Q1("Q1 - Sitzverteilung");			
		} else if (queryParam.equalsIgnoreCase("Q1.with")) {
			query = new Q1_WITH("Q1 - Sitzverteilung");
		} else if (queryParam.equalsIgnoreCase("Q2")) {			
			query = new Q2("Q2 - Abgeordnete");		
		} else if (queryParam.equalsIgnoreCase("Q2.with")) {			
			query = new Q2_WITH("Q2 - Abgeordnete");		
		} else if (queryParam.equalsIgnoreCase("Q3")) {		
			int randomWahlkreis = new Random().nextInt(299) + 1;
			if (request.getParameter("wk") != null) {
				randomWahlkreis = Integer.parseInt(request.getParameter("wk"));
			}
			query = new Q3("Q3 - Wahlkreisinfo", randomWahlkreis);			
		} else if (queryParam.equalsIgnoreCase("Q3.with")) {		
			int randomWahlkreis = new Random().nextInt(299) + 1;
			if (request.getParameter("wk") != null) {
				randomWahlkreis = Integer.parseInt(request.getParameter("wk"));
			}
			query = new Q3_WITH("Q3 - Wahlkreisinfo", randomWahlkreis);			
		} else if (queryParam.equalsIgnoreCase("Q4")) {	
			query = new Q4("Q4 - Wahlkreisergebnisse");
		} else if (queryParam.equalsIgnoreCase("Q4.with")) {	
			query = new Q4_With("Q4 - Wahlkreisergebnisse");
		} else if (queryParam.equalsIgnoreCase("Q5")) {
			query = new Q5("Q5 - Überhangsmandate");
		} else if (queryParam.equalsIgnoreCase("Q5.with")) {
			query = new Q5_WITH("Q5 - Überhangsmandate");
		} else if (queryParam.equalsIgnoreCase("Q6")) {
			query = new Q6("Q6 - Knappste Sieger");
		} else if (queryParam.equalsIgnoreCase("Q7")) {
			int randomWahlkreis = new Random().nextInt(5) + 213;
			if (request.getParameter("wk") != null) {
				randomWahlkreis = Integer.parseInt(request.getParameter("wk"));
			}
			query = new Q7("Q7 - Wahlkreisinfo (Einzelstimmen)", randomWahlkreis);
		} else if (queryParam.equalsIgnoreCase("Q7.with")) {
			int randomWahlkreis = new Random().nextInt(5) + 213;
			if (request.getParameter("wk") != null) {
				randomWahlkreis = Integer.parseInt(request.getParameter("wk"));
			}
			query = new Q7_WITH("Q7 - Wahlkreisinfo (Einzelstimmen)", randomWahlkreis);
		} else if (queryParam.equalsIgnoreCase("RefreshVotes")) {
			query = new RefreshVotes("Aggregiere Stimmen auf Wahlkreisebene.");
		}
		if (query != null) {
			query.setDatabase(database);
			String html = query.generateHtmlOutput();
			response.getWriter().write(html);
		} else {
			response.getWriter().write("Query was NULL.");
		}
	}

}

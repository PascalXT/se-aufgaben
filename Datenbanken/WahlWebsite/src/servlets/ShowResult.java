package servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import queries.Q1;
import queries.Q2;
import queries.Q5;
import queries.Query;
import database.Database;

@WebServlet("/ShowResult")
public class ShowResult extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Query query;
	
    public ShowResult() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String queryParam = request.getParameter("query");
		if (queryParam == null) {
			response.getWriter().write("query parameter missing");
			return;
		}
		
		Database database = new Database("Wahlsys", "Korbi", "stunk6", "KORBI", false);

		if (queryParam.equalsIgnoreCase("Q1")) {
			
			query = new Q1("Q1 - Sitzverteilung");
			
		} else if (queryParam.equalsIgnoreCase("Q2")) {
			
			query = new Q2("Q2 - Abgeordnete");
			
		} else if (queryParam.equalsIgnoreCase("Q5")) {
			
			query = new Q5("Q5 - Überhangsmandate");
		}
		
		query.setDatabase(database);
		String html = query.generateHtmlOutput();
		response.getWriter().write(html);
			

	}

}

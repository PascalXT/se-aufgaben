package servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import database.DB;
import flags.FlagDefinition;
import flags.FlagErrorException;
import flags.Flags;

@WebServlet("/ChooseWahllokal")
public class ChooseWahllokal extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ChooseWahllokal() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		
		
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

}

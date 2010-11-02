package main;

import csv.CsvParser;
import datenbank.Datenbank;

public class Main {
	public static void main(String[] args) throws Exception {
	  Datenbank db = new Datenbank();
	  CsvParser csv_parser = new CsvParser(db);
	  final String kDatenordner =
	    "H:\\MasterSE\\1.Semester\\Datenbanken\\se-aufgaben\\Datenbanken\\Daten\\";
	  final String kMessagePfad = "H:\\Desktop\\db2_progress_messages";
	  csv_parser.runImports(kDatenordner, kMessagePfad);
	}
//		Class.forName("com.ibm.db2.jcc.DB2Driver");
//		
//		Datenbank db = new Datenbank();
//		Connection connection = db.getConnection();
//
//    try {
//      Statement statement = connection.createStatement();
//      String sql_statement = "SELECT * FROM WAHLKREIS;";
//      ResultSet result_set = statement.executeQuery(sql_statement);
//      while (result_set.next()) {
//        System.out.println(result_set.getInt("WAHLKREISID") + " "
//            + result_set.getString("WAHLKREISNAME"));
//        }
//        result_set.close();
//        statement.close();
//      } catch (SQLException e) {
//        e.printStackTrace();
//    }
//
//    connection.close();
//  }
}

package main;

import datenbank.Datenbank;
import datenbank.TabellenDef;

public class Main {
	public static void main(String[] args) throws Exception {

	  Datenbank db = new Datenbank(args);
	  System.out.println(Datenbank.kSchemaName);
	  TabellenDef td = new TabellenDef(Datenbank.kSchemaName);
	  db.executeDB2(td.buildCreateTableStatement());
	  
//	  CsvParser csv_parser = new CsvParser(db, "H:\\stimmen.csv");
//
//	  final String kDatenordner =
//	    "H:\\MasterSE\\1.Semester\\Datenbanken\\se-aufgaben\\Datenbanken\\Daten\\";
//	  final String kMessagePfad = "H:\\Desktop\\db2_progress_messages";
//	  csv_parser.runImports(kDatenordner, kMessagePfad);
//	  csv_parser.parseVotes(kDatenordner + "Wahlergebnisse.csv");
	}
}

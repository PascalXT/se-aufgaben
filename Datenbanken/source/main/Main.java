package main;

import csv.CsvParser;
import datenbank.Datenbank;

public class Main {
	public static void main(String[] args) throws Exception {
	  Datenbank db = new Datenbank();
	  CsvParser csv_parser = new CsvParser(db, "H:\\stimmen.csv");
	  final String kDatenordner =
	    "H:\\MasterSE\\1.Semester\\Datenbanken\\se-aufgaben\\Datenbanken\\Daten\\";
	  final String kMessagePfad = "H:\\Desktop\\db2_progress_messages";
	  csv_parser.runImports(kDatenordner, kMessagePfad);
	  csv_parser.parseVotes(kDatenordner + "Wahlergebnisse.csv");
	}
}

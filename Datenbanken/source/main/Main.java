package main;

import csv.CsvParser;
import datenbank.Datenbank;
import datenbank.TabellenDef;

public class Main {
	public static void main(String[] args) throws Exception {
		
		if (args.length != 8)
			throw new Exception("parameter in eclipse hinzufügen!");
		
		String dbName = args[0];
		String dbUser = args[1];
		String dbPwd = args[2];
		String dbSchemaName = args[3];
		String dbCommandFile = args[4];
		
	  Datenbank db = new Datenbank(dbName, dbUser, dbPwd, dbSchemaName, dbCommandFile);
//	  System.out.println(Datenbank.kSchemaName);
//	  TabellenDef td = new TabellenDef(Datenbank.kSchemaName);
//	  db.executeDB2(td.buildCreateTableStatement());
	  
	  String parserStimmenFile = args[5];
	  String parserDataFolder = args[6];
	  String logFile = args[7];
	  
	  CsvParser csv_parser = new CsvParser(db, parserStimmenFile);

	  csv_parser.runImports(parserDataFolder, logFile);
	  //csv_parser.parseVotes(kDatenordner + "Wahlergebnisse.csv");
	}
}

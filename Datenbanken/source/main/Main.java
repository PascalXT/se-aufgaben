package main;

import csv.CsvParser;
import datenbank.Datenbank;
import datenbank.TabellenDef;
import flags.Flags;

public class Main {
  // Example arguments:
  // --dbName=WahlSys --dbUser=pascal_db2 --dbPwd=Datenbanken 
  // --dbSchemaName=Pascal --dbCommandFile=H:\db2_commands.txt 
  // --parserStimmenFile=C:\Stimmen.csv
  // --parserDataFolder=C:\se-aufgaben\Datenbanken\Daten\
  // --logFile=C:\db2_progress_messages.txt
  final static String kFlagDbName = "dbName";
  final static String kFlagDbUser = "dbUser";
  final static String kFlagDbPwd = "dbPwd";
  final static String kFlagDbSchemaName = "dbSchemaName";
  final static String kFlagDbCommandFile = "dbCommandFile";
  final static String kFlagParserStimmenFile = "parserStimmenFile";
  final static String kFlagParserDataFolder = "parserDataFolder";
  final static String kFlagLogFile = "logFile";
  final static String[][] kFlagDefinition = {
      {kFlagDbName, "WahlSys", "Name of the db2 database."},
      {kFlagDbUser, null, "Username for the db2 database."},
      {kFlagDbPwd, null, "Password of the user for the db2 database"},
      {kFlagDbSchemaName, null, "Name of the schema created in db2"},
      {kFlagDbCommandFile,
        null, 
        "Path of a temporary file used for db2 commands"},
      {kFlagParserStimmenFile, 
        null,
        "Path of the file used for storing the single votes created in the " +
          "parser. This file will be loaded by a db2 import."},
      {kFlagParserDataFolder,
        null,
        "Folder in which the csv source files are stored."},
      {kFlagLogFile,
        null,
        "Path of the file, where log messages of db2 commands are stored."}
  };
  
	public static void main(String[] args) throws Exception {		
		Flags flags = new Flags(kFlagDefinition, args);
		String dbName = flags.getFlagValue(kFlagDbName);
		String dbUser = flags.getFlagValue(kFlagDbUser);
		String dbPwd = flags.getFlagValue(kFlagDbPwd);
		String dbSchemaName = flags.getFlagValue(kFlagDbSchemaName);
		String dbCommandFile = flags.getFlagValue(kFlagDbCommandFile);
		
	  Datenbank db = new Datenbank(dbName, dbUser, dbPwd, dbSchemaName, dbCommandFile);
	  TabellenDef td = new TabellenDef(db.schemaName);
	  //db.executeDB2(td.buildCreateTableStatement());
	  
	  String parserStimmenFile = flags.getFlagValue(kFlagParserStimmenFile);
	  String parserDataFolder = flags.getFlagValue(kFlagParserDataFolder);
	  String logFile = flags.getFlagValue(kFlagLogFile);
	  
	  CsvParser csv_parser = new CsvParser(db, parserStimmenFile);

	  //csv_parser.runImports(parserDataFolder, logFile);
	  csv_parser.parseVotes(parserDataFolder + "Wahlergebnisse.csv");

	}
}

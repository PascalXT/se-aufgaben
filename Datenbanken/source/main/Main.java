package main;

import csv.CsvParser;
import datenbank.Datenbank;
import datenbank.TabellenDef;
import flags.*;

public class Main {  
	public static void main(String[] args) throws Exception {		
		Flags.setFlags(FlagDefinition.kFlagDefinition, args);
		final String dbName = Flags.getFlagValue(FlagDefinition.kFlagDbName);
		final String dbUser = Flags.getFlagValue(FlagDefinition.kFlagDbUser);
		final String dbPwd = Flags.getFlagValue(FlagDefinition.kFlagDbPwd);
		final String dbSchemaName =
		  Flags.getFlagValue(FlagDefinition.kFlagDbSchemaName);
		final String dbCommandFile =
		  Flags.getFlagValue(FlagDefinition.kFlagDbCommandFile);
		final String dbCommandFlags =
		  Flags.getFlagValue(FlagDefinition.kFlagDbCommandFlags);
		
	  Datenbank db =
	    new Datenbank(dbName, dbUser, dbPwd, dbSchemaName, dbCommandFile, 
	                  dbCommandFlags);
	  
	  if (Flags.isTrue(FlagDefinition.kFlagCreateTables)) {
  	  TabellenDef td = new TabellenDef(db.schemaName);
  	  db.executeDB2(td.buildCreateTableStatement());
	  }
	  
    final String parserErststimmenFile =
      Flags.getFlagValue(FlagDefinition.kFlagParserErststimmenFile);
    final String parserZweitstimmenFile =
      Flags.getFlagValue(FlagDefinition.kFlagParserZweitstimmenFile);
	  final String parserDataFolder =
	    Flags.getFlagValue(FlagDefinition.kFlagParserDataFolder);
	  final String logFile = Flags.getFlagValue(FlagDefinition.kFlagLogFile);
	  
	  CsvParser csvParser =
	    new CsvParser(db, parserErststimmenFile, parserZweitstimmenFile);

	  if (Flags.isTrue(FlagDefinition.kImportCsvFiles))
	    csvParser.runImports(parserDataFolder, logFile);
	  if (Flags.isTrue(FlagDefinition.kFlagCreateVotes))
	    csvParser.parseVotes(parserDataFolder + "Wahlergebnisse.csv", logFile);
	  if (Flags.isTrue(FlagDefinition.kFlagImportVotes))
	    csvParser.importVotes(logFile);

	}
}

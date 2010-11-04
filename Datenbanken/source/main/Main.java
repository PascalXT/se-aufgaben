package main;

import csv.CsvParser;
import datenbank.Datenbank;
import datenbank.TabellenDef;
import flags.Flags;

public class Main {
  // Example arguments:
  // --dbName=WahlSys --dbUser=pascal_db2 --dbPwd=Datenbanken 
  // --dbSchemaName=Pascal --dbCommandFile=H:\db2_commands.txt 
  // --parserErststimmenFile=C:\Erststimmen.csv
  // --parserZweitstimmenFile=C:\Zweitstimmen.csv
  // --parserDataFolder=C:\se-aufgaben\Datenbanken\Daten\
  // --logFile=C:\db2_progress_messages.txt
  final static String kFlagDbName = "dbName";
  final static String kFlagDbUser = "dbUser";
  final static String kFlagDbPwd = "dbPwd";
  final static String kFlagDbSchemaName = "dbSchemaName";
  final static String kFlagDbCommandFile = "dbCommandFile";
  final static String kFlagParserErststimmenFile = "parserErststimmenFile";
  final static String kFlagParserZweitstimmenFile = "parserZweitstimmenFile";
  final static String kFlagParserDataFolder = "parserDataFolder";
  final static String kFlagLogFile = "logFile";
  final static String kFlagCreateTables = "createTables";
  final static String kImportCsvFiles = "importCsvFiles";
  final static String kCreateVotes = "createVotes";
  final static String kFlagDbCommandFlags = "dbCommandFlags";
  final static String[][] kFlagDefinition = {
      {kFlagDbName, "WahlSys", "Name of the db2 database."},
      {kFlagDbUser, null, "Username for the db2 database."},
      {kFlagDbPwd, null, "Password of the user for the db2 database"},
      {kFlagDbSchemaName, null, "Name of the schema created in db2"},
      {kFlagDbCommandFile,
        null, 
        "Path of a temporary file used for db2 commands"},
        {kFlagParserErststimmenFile, 
          null,
          "Path of the file used for storing the single votes created in the " +
            "parser. This file will be loaded by a db2 import."},
      {kFlagParserZweitstimmenFile, 
        null,
        "Path of the file used for storing the single votes created in the " +
        "parser. This file will be loaded by a db2 import."},
      {kFlagParserDataFolder,
        null,
        "Folder in which the csv source files are stored."},
      {kFlagLogFile,
        null,
        "Path of the file, where log messages of db2 commands are stored."},
      {kFlagCreateTables,
        Flags.kFalse,
        "If true, the database tables will be created in the beginning."
      },
      {kImportCsvFiles,
        Flags.kTrue,
        "If true, the csv files will be imported in the beginning."
      },
      {kCreateVotes,
        Flags.kFalse,
        "If true, the csv files containing the votes will be generated."
      },
      {kFlagDbCommandFlags,
        "-w db2 -tvf",
        "The flags that are added to db2cmd.exe in executeDB2."
      }
  };
  public static Flags flags;
  
	public static void main(String[] args) throws Exception {		
		flags = new Flags(kFlagDefinition, args);
		final String dbName = flags.getFlagValue(kFlagDbName);
		final String dbUser = flags.getFlagValue(kFlagDbUser);
		final String dbPwd = flags.getFlagValue(kFlagDbPwd);
		final String dbSchemaName = flags.getFlagValue(kFlagDbSchemaName);
		final String dbCommandFile = flags.getFlagValue(kFlagDbCommandFile);
		final String dbCommandFlags = flags.getFlagValue(kFlagDbCommandFlags);
		
	  Datenbank db =
	    new Datenbank(dbName, dbUser, dbPwd, dbSchemaName, dbCommandFile, 
	                  dbCommandFlags);
	  
	  if (flags.isTrue(kFlagCreateTables)) {
  	  TabellenDef td = new TabellenDef(db.schemaName);
  	  db.executeDB2(td.buildCreateTableStatement());
	  }
	  
    final String parserErststimmenFile =
      flags.getFlagValue(kFlagParserErststimmenFile);
    final String parserZweitstimmenFile =
      flags.getFlagValue(kFlagParserErststimmenFile);
	  final String parserDataFolder = flags.getFlagValue(kFlagParserDataFolder);
	  final String logFile = flags.getFlagValue(kFlagLogFile);
	  
	  CsvParser csvParser =
	    new CsvParser(db, parserErststimmenFile, parserZweitstimmenFile);

	  if (flags.isTrue(kImportCsvFiles))
	    csvParser.runImports(parserDataFolder, logFile);
	  if (flags.isTrue(kCreateVotes))
	    csvParser.parseVotes(parserDataFolder + "Wahlergebnisse.csv", logFile);
	}
}

package flags;

public class FlagDefinition {
  // Example arguments:
  // --dbName=WahlSys --dbUser=pascal_db2 --dbPwd=Datenbanken 
  // --dbSchemaName=Pascal --dbCommandFile=H:\db2_commands.txt 
  // --parserErststimmenFile=C:\Erststimmen.csv
  // --parserZweitstimmenFile=C:\Zweitstimmen.csv
  // --parserDataFolder=C:\se-aufgaben\Datenbanken\Daten\
  // --logFile=C:\db2_progress_messages.txt
  final public static String kFlagDbName = "dbName";
  final public static String kFlagDbUser = "dbUser";
  final public static String kFlagDbPwd = "dbPwd";
  final public static String kFlagDbSchemaName = "dbSchemaName";
  final public static String kFlagDbCommandFile = "dbCommandFile";
  final public static String kFlagParserErststimmenFile = "parserErststimmenFile";
  final public static String kFlagParserZweitstimmenFile = "parserZweitstimmenFile";
  final public static String kFlagParserDataFolder = "parserDataFolder";
  final public static String kFlagLogFile = "logFile";
  final public static String kFlagCreateTables = "createTables";
  final public static String kImportCsvFiles = "importCsvFiles";
  final public static String kFlagCreateVotes = "createVotes";
  final public static String kFlagDbCommandFlags = "dbCommandFlags";
  final public static String kFlagImportVotes = "importVotes";
  final public static String[][] kFlagDefinition = {
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
        Flags.kTrue,
        "If true, the database tables will be created in the beginning."
      },
      {kImportCsvFiles,
        Flags.kFalse,
        "If true, the csv files will be imported in the beginning."
      },
      {kFlagCreateVotes,
        Flags.kTrue,
        "If true, the csv files containing the votes will be generated."
      },
      {kFlagImportVotes,
        Flags.kFalse,
        "If true, the csv files containing the votes will be generated."
      },
      {kFlagDbCommandFlags,
        "-w db2 -tvf",
        "The flags that are added to db2cmd.exe in executeDB2."
      }
  };
}

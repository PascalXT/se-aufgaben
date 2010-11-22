package flags;

public class FlagDefinition {
  final public static String kFlagDbName = "dbName";
  final public static String kFlagDbUser = "dbUser";
  final public static String kFlagDbPwd = "dbPwd";
  final public static String kFlagDbSchemaName = "dbSchemaName";
  final public static String kFlagDbCommandFile = "dbCommandFile";
  final public static String kFlagParserStimmenFile = "parserStimmenFile";
  final public static String kFlagParserErststimmenAggregiertFile = "parserErststimmenAggregiertFile";
  final public static String kFlagParserZweitstimmenAggregiertFile = "parserZweitstimmenAggregiertFile";
  final public static String kFlagParserDataFolder = "parserDataFolder";
  final public static String kFlagLogFile = "logFile";
  final public static String kFlagCreateTables = "createTables";
  final public static String kImportCsvFiles = "importCsvFiles";
  final public static String kFlagCreateVotes = "createVotes";
  final public static String kFlagDbCommandFlags = "dbCommandFlags";
  final public static String kFlagImportVotes = "importVotes";
  final public static String kFlagImportAggregatedVotes = "importAggregatedVotes";
  final public static String kFlagOnlyVotesForBundesland = "onlyVotesForBundesland";
  final public static String kFlagComputeSitzverteilungBundestag = "computeSitzverteilungBundestag";
  final public static String kFlagMakeTemporaryTablesPermanent = "makeTemporaryTablesPermanent";
  final public static String[][] kFlagDefinition = {
      { kFlagDbName, "WahlSys", "Name of the db2 database." },
      { kFlagDbUser, null, "Username for the db2 database." },
      { kFlagDbPwd, null, "Password of the user for the db2 database" },
      { kFlagDbSchemaName, null, "Name of the schema created in db2" },
      { kFlagDbCommandFile, null, "Path of a temporary file used for db2 commands" },
      {
          kFlagParserStimmenFile,
          null,
          "Path of the file used for storing the single votes created in the parser. This file will be loaded by a db2"
              + " import." },
      { kFlagParserErststimmenAggregiertFile, null, "Path of the file used for storing the aggregated votes" },
      { kFlagParserZweitstimmenAggregiertFile, null, "Path of the file used for storing the aggregated votes" },
      { kFlagParserDataFolder, null, "Folder in which the csv source files are stored." },
      { kFlagLogFile, null, "Path of the file, where log messages of db2 commands are stored." },
      { kFlagCreateTables, Flags.kFalse, "If true, the database tables will be created in the beginning." },
      { kImportCsvFiles, Flags.kFalse, "If true, the csv files will be imported in the beginning." },
      { kFlagCreateVotes, Flags.kFalse, "If true, the csv files containing the votes will be generated." },
      { kFlagImportVotes, Flags.kFalse, "If true, the csv files containing the votes will be generated." },
      { kFlagImportAggregatedVotes, Flags.kFalse, "If true, the aggregated votes / wahlkreis will be imported" },
      { kFlagDbCommandFlags, "-w db2 -tvf", "The flags that are added to db2cmd.exe in executeDB2." },
      { kFlagOnlyVotesForBundesland, "", "If not empty, only votes for this Bundesland will be created." },
      { kFlagComputeSitzverteilungBundestag, Flags.kFalse,
          "If true, the Sitzverteilung for the Bundestag will be computed." },
      { kFlagMakeTemporaryTablesPermanent, Flags.kFalse,
          "If true, permanent tables will be used instead of temporary tables." } };

}

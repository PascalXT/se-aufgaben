package main;

import csv.CsvParser;
import datenbank.Datenbank;
import datenbank.TabellenDef;
import evaluation.Evaluation;
import flags.*;

public class Main {
  public static void main(String[] args) throws Exception {
//    int[] votes = {11828277, 9990488, 6316080, 5155933, 4643272, 2830238};
//    int sitze[] = Evaluation.getSitze(votes, 622);
//    for (int i = 0; i < sitze.length; i++) {
//      System.out.println("Party " + i + " got " + sitze[i] + " Sitze.");
//    }
//    System.exit(0);
    
    Flags.setFlags(FlagDefinition.kFlagDefinition, args);
    final String dbName = Flags.getFlagValue(FlagDefinition.kFlagDbName);
    final String dbUser = Flags.getFlagValue(FlagDefinition.kFlagDbUser);
    final String dbPwd = Flags.getFlagValue(FlagDefinition.kFlagDbPwd);
    final String dbSchemaName = Flags.getFlagValue(FlagDefinition.kFlagDbSchemaName);
    final String dbCommandFile = Flags.getFlagValue(FlagDefinition.kFlagDbCommandFile);
    final String dbCommandFlags = Flags.getFlagValue(FlagDefinition.kFlagDbCommandFlags);
    final String logFile = Flags.getFlagValue(FlagDefinition.kFlagLogFile);

    Datenbank db = new Datenbank(dbName, dbUser, dbPwd, dbSchemaName, dbCommandFile, dbCommandFlags, logFile);

    if (Flags.isTrue(FlagDefinition.kFlagCreateTables)) {
      TabellenDef td = new TabellenDef(db.schemaName);
      db.executeDB2(td.buildCreateTableStatement());
    }

    final String parserStimmenFile = Flags.getFlagValue(FlagDefinition.kFlagParserStimmenFile);
    final String parserErststimmenAggregiertFile = Flags
        .getFlagValue(FlagDefinition.kFlagParserErststimmenAggregiertFile);
    final String parserZweitstimmenAggregiertFile = Flags
        .getFlagValue(FlagDefinition.kFlagParserZweitstimmenAggregiertFile);
    final String parserDataFolder = Flags.getFlagValue(FlagDefinition.kFlagParserDataFolder);

    CsvParser csvParser = new CsvParser(db, parserStimmenFile, parserErststimmenAggregiertFile,
        parserZweitstimmenAggregiertFile);

    String bundesland = Flags.getFlagValue(FlagDefinition.kFlagOnlyVotesForBundesland);
    if (bundesland.isEmpty())
      bundesland = null;

    if (Flags.isTrue(FlagDefinition.kImportCsvFiles))
      csvParser.runImports(parserDataFolder, logFile);
    if (Flags.isTrue(FlagDefinition.kFlagCreateVotes))
      csvParser.parseVotes(parserDataFolder + "Wahlergebnisse.csv", bundesland, logFile);
    if (Flags.isTrue(FlagDefinition.kFlagImportVotes))
      csvParser.importVotes();
    if (Flags.isTrue(FlagDefinition.kFlagImportAggregatedVotes))
      csvParser.importAggregatedVotes();
  }

}

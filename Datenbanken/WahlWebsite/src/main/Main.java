package main;

import csv.CsvParser;
import database.DB;
import database.TableDef;
import evaluation.Evaluation;
import flags.*;

public class Main {
  public static void main(String[] args) throws Exception {  
    Flags.setFlags(FlagDefinition.kFlagDefinition, args);

    final String logFile = Flags.getFlagValue(FlagDefinition.kFlagLogFile);

    DB db = DB.getDatabaseByFlags(); 

    if (Flags.isTrue(FlagDefinition.kFlagCreateTables)) {
      TableDef td = new TableDef(db.schemaName);
      db.executeDB2(td.buildCreateTableStatement());
    }

    final String parserStimmenFile = Flags.getFlagValue(FlagDefinition.kFlagParserStimmenFile);
    final String parserErststimmenAggregiertFile = Flags
        .getFlagValue(FlagDefinition.kFlagParserErststimmenAggregiertFile);
    final String parserZweitstimmenAggregiertFile = Flags
        .getFlagValue(FlagDefinition.kFlagParserZweitstimmenAggregiertFile);
    final String parserWahlberechtigteFile = Flags.getFlagValue(FlagDefinition.kFlagParserWahlberechtigteFile);
    final String parserDataFolder = Flags.getFlagValue(FlagDefinition.kFlagParserDataFolder);

    CsvParser csvParser = new CsvParser(db, parserStimmenFile, parserErststimmenAggregiertFile,
        parserZweitstimmenAggregiertFile, parserWahlberechtigteFile);

    String bundesland = Flags.getFlagValue(FlagDefinition.kFlagOnlyVotesForBundesland);
    if (bundesland.isEmpty())
      bundesland = null;

    if (Flags.isTrue(FlagDefinition.kImportCsvFiles))
      csvParser.runImports(parserDataFolder, logFile);
    if (Flags.isTrue(FlagDefinition.kFlagCreateVotes))
      csvParser.parseKergCsv(parserDataFolder + "kerg.csv");
    if (Flags.isTrue(FlagDefinition.kFlagImportVotes))
      csvParser.importVotes();
    if (Flags.isTrue(FlagDefinition.kFlagImportAggregatedVotes))
      csvParser.importAggregatedVotes();
    if (Flags.isTrue(FlagDefinition.kFlagComputeSitzverteilungBundestag)) {
      Evaluation evaluation = new Evaluation(db);
      evaluation.computeSitzverteilungBundestag();
    }
    
    db.disconnect();
  }

}

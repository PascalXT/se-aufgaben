package csv;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.*;

import datenbank.Datenbank;

public class CsvParser {
  final int kAktuellesJahr = 2009;
  final int kMaxBewohnerProBezirk = 2500;
  
  private Datenbank datenbank;
  
  public CsvParser(Datenbank datenbank) {
    this.datenbank = datenbank;
  }
  
  private int getKandidat(int wahlkreis_ID, int partei_ID) throws SQLException {
    final int kKeineParteiID = 99;
    if (partei_ID == kKeineParteiID) {
      final String sql = "SELECT " + Datenbank.kKandidatID + " FROM " +
      Datenbank.kKandidat + " WHERE " + Datenbank.kWahlkreisID + "=" +
      wahlkreis_ID + " AND " + Datenbank.kParteiID + "=NULL";
    ResultSet result_set = datenbank.executeSQL(sql);
    return result_set.getInt(Datenbank.kKandidatID);
    } else {
      final String sql = "SELECT " + Datenbank.kKandidatID + " FROM " +
        Datenbank.kKandidat + " WHERE " + Datenbank.kWahlkreisID + "=" +
        wahlkreis_ID + " AND " + Datenbank.kParteiID + "=" + partei_ID;
      ResultSet result_set = datenbank.executeSQL(sql);
      return result_set.getInt(Datenbank.kKandidatID);
    }
  }
  
  private void einzelneErststimme(int wahlkreis_ID,
                                  int bezirk_ID,
                                  int kandidat_ID) {
    System.out.println(wahlkreis_ID + ";" + bezirk_ID + ";" + kandidat_ID);
  }
  
  private void insertErststimmen(int wahlkreis_ID,
                                 int partei_ID,
                                 int anzahl_erststimmen,
                                 int anzahl_wahlbezirke) {
    for (int i = 0; i < anzahl_wahlbezirke; i++) {
      for (int j = 0; j < anzahl_erststimmen; j++) {
        try {
          einzelneErststimme(wahlkreis_ID,
                             i,
                             getKandidat(wahlkreis_ID, partei_ID));
        } catch (SQLException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }
    }
  }
  
  public void parseVotes(String tabelle, String csv_datei) throws IOException {
    final int kSpalteWahlkreisID = 1;
    final int kSpalteParteiID = 2;
    final int kSpalteErststimmen = 3;
    final int kSpalteZweitstimmen = 4;
    final int kSpalteJahr = 5;
    
    FileReader file_reader = new FileReader(csv_datei);
    LineNumberReader line_number_reader = new LineNumberReader(file_reader);
    String next_line;
    while ((next_line = line_number_reader.readLine()) != null) {
      String[] tokens = next_line.split(";");
      assert(tokens.length == 5);
      
      if (Integer.parseInt(tokens[kSpalteJahr]) == kAktuellesJahr) {
        final int anzahl_erststimmen =
          Integer.parseInt(tokens[kSpalteErststimmen]);
        final int anzahl_zweitstimmen =
          Integer.parseInt(tokens[kSpalteErststimmen]);
        final int anzahl_wahlbezirke = 
          Math.max(anzahl_erststimmen, anzahl_zweitstimmen) /
          kMaxBewohnerProBezirk;
        insertErststimmen(Integer.parseInt(tokens[kSpalteWahlkreisID]),
                          Integer.parseInt(tokens[kSpalteParteiID]),
                          anzahl_erststimmen,
                          anzahl_wahlbezirke);
      }
    }
  }
  
  public void runImports(String datenordner, String message_pfad) {
    final int kDatei = 0;
    final int kSpaltenNummern = 1;
    final int kSpaltenNamen = 2;
    final String[][] kImportTripel = {
        {"Wahlkreise.csv",
             "(1, 2, 3)",
             Datenbank.kWahlkreis + " (" + Datenbank.kWahlkreisID + ", " +
                 Datenbank.kWahlkreisName + ", " + Datenbank.kBundeslandID +
                 ")"},
        {"Bundeslaender.csv",
             "(1, 2)",
             Datenbank.kBundesland + " (" + Datenbank.kBundeslandID + ", " +
             Datenbank.kBundeslandName + ")"},
        {"Parteien.csv",
              "(3, 1, 2)",
              Datenbank.kPartei + " (" + Datenbank.kParteiID + ", " +
              Datenbank.kParteiKuerzel + ", " + Datenbank.kParteiName + ")"},
        {"Kandidaten.cvs",
          "(1, 2, 3, 4, 5, 6)",
          Datenbank.kKandidat + " (" + Datenbank.kNachname + ", " +
          Datenbank.kVorname + ", " + Datenbank.kParteiID + ", " +
          Datenbank.kBundeslandID + ", " + Datenbank.kListenplatz + ", " + ")"},
    };
       
    for (int i = 0; i < kImportTripel.length; i++) {
      System.out.println("Import: " + kImportTripel[i][kDatei]);
      final String datei_pfad = datenordner + kImportTripel[i][kDatei];
      String sql = "IMPORT FROM \"" + datei_pfad + 
        "\" OF DEL MODIFIED BY COLDEL; METHOD P " +
        kImportTripel[i][kSpaltenNummern] + " MESSAGES \"" + message_pfad +
        "\" INSERT INTO " + kImportTripel[i][kSpaltenNamen];
      System.out.println(sql);
      datenbank.executeDB2(sql);
    }
  }
}

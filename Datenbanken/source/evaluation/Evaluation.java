package evaluation;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import datenbank.Datenbank;

;

public class Evaluation {
  private Datenbank database;
  final private String kSitzeTable = "TempSitze";

  public Evaluation(Datenbank database) {
    this.database = database;
  }

  public static int[] getSitze(int[] stimmen, int maxSitze) {
    return hoechstZahlVerfahren(stimmen, maxSitze);
  }

  // Returns a number of {0, ..., maxNumber}.
  private static int getRandomNumber(int maxNumber) {
    return 0;
  }

  private static int[] hoechstZahlVerfahren(int[] votes, int maxSitze) {
    double[] divisoren = new double[votes.length];
    for (int i = 0; i < divisoren.length; i++) {
      divisoren[i] = 0.5;
    }

    int[] sitze = new int[votes.length];
    for (int i = 0; i < sitze.length; i++) {
      sitze[i] = 0;
    }

    int remainingSitze = maxSitze;
    while (remainingSitze > 0) {
      // Parties that have the highest quotient in this iteration.
      List<Integer> sitzCandidates = new ArrayList<Integer>();
      double maxQuotient = -1;
      for (int i = 0; i < votes.length; i++) {
        double quotient = votes[i] / divisoren[i];
        if (quotient >= maxQuotient) {
          if (quotient > maxQuotient) {
            maxQuotient = quotient;
            sitzCandidates.clear();
          }
          sitzCandidates.add(i);
        }
      }
      while (sitzCandidates.size() > remainingSitze) {
        sitzCandidates.remove(getRandomNumber(sitzCandidates.size()));
      }
      remainingSitze -= sitzCandidates.size();
      for (int i : sitzCandidates) {
        sitze[i]++;
        divisoren[i]++;
      }
    }
    return sitze;
  }

  /**
   * @Pre for all i: sum[j=0..n](stimmenPerBundesland[i][n]) = totalStimmen[i]
   *      parteiID.length = totalStimmen.length bundeslandID.length =
   * @param parteiID
   *          Array of IDs of the partys referenced in totalStimmen and
   *          stimmenPerBundesland
   * @param bundeslandID
   *          Array of IDs of the bundesländer referenced in
   *          stimmenPerBundesland
   * @param totalStimmen
   *          Array of number of Stimmen for each party in the Bundestag
   * @param stimmenPerBundesland
   *          Array of Stimmen for each party and each Bundesland.
   * @throws SQLException
   */
  public void createSitzeTable(int[] parteiID, int[] bundeslandID, int[] totalStimmen, int[][] stimmenPerBundesland,
      int maxSitze) throws SQLException {
    database.createOrReplaceTemporaryTable(kSitzeTable, "ParteiID BIGING, bundeslandID BIGINT, AnzahlStimmen BIGINT");
    int[] sitzePerParty = getSitze(totalStimmen, maxSitze);
    for (int i = 0; i < parteiID.length; i++) {
      int[] sitzePerPartyAndBundesland = getSitze(stimmenPerBundesland[i], sitzePerParty[i]);
      for (int j = 0; j < bundeslandID.length; j++) {
        database.executeUpdate("INSERT INTO " + kSitzeTable + " VALUES(" + parteiID[i] + ", " + bundeslandID[j] + ", "
            + sitzePerPartyAndBundesland[j] + ")");
      }
    }
  }

  // public void create

  public void computeSitzverteilungBundestag() throws SQLException {
    // Aggregate election results to Bundesland level.
    database.createOrReplaceTemporaryTable(database.zweitStimmenNachBundesland(), Datenbank.kForeignKeyParteiID
        + " BIGINT, " + Datenbank.kForeignKeyBundeslandID + " BIGINT, " + Datenbank.kAnzahlStimmen + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachBundesland() + " SELECT w2."
        + Datenbank.kWahlergebnis2ParteiID + ", wk." + Datenbank.kWahlkreisBundeslandID + ", sum(w2."
        + Datenbank.kWahlergebnis2Anzahl + ") as " + Datenbank.kAnzahlStimmen + "" + " FROM "
        + database.wahlergebnis2() + " w2" + ", " + database.wahlkreis + " wk" + " WHERE w2."
        + Datenbank.kWahlergebnis2WahlkreisID + " = wk." + Datenbank.kWahlkreisID + " GROUP BY " + "wk."
        + Datenbank.kWahlkreisBundeslandID + ", w2." + Datenbank.kWahlergebnis2ParteiID);

    database.createOrReplaceTemporaryTable(database.zweitStimmenNachPartei(), Datenbank.kForeignKeyParteiID + " BIGINT, "
        + Datenbank.kAnzahlStimmen + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachPartei() + " SELECT " + Datenbank.kForeignKeyParteiID
        + ", SUM(" + Datenbank.kAnzahlStimmen + ") FROM " + database.zweitStimmenNachBundesland() + " GROUP BY "
        + Datenbank.kForeignKeyParteiID);

    database.printTable(database.zweitStimmenNachBundesland());
    database.printTable(database.zweitStimmenNachPartei());
    database.printResultSet(database.executeSQL("SELECT " + Datenbank.kForeignKeyParteiID + ", SUM("
        + Datenbank.kAnzahlStimmen + ") FROM " + database.zweitStimmenNachBundesland() + " GROUP BY "
        + Datenbank.kForeignKeyParteiID));
  }
  // WITH maxErgebnis(WahlkreisId, maxStimmen) as (
  // SELECT k.dmwahlkreisid, max(w.anzahl)
  // FROM PASCAL.wahlergebnis1 w, Pascal.kandidat k
  // WHERE w.kandidatid = k.id
  // GROUP BY k.dmwahlkreisid),
  // Gewinner(Wahlkreis, Kandidat) as (
  // SELECT e.WahlkreisID, w.KandidatID
  // FROM maxErgebnis e, wahlergebnis1 w
  // WHERE e.wahlkreisID = w.wahlkreisid AND e.maxStimmen = w.Anzahl
  // ORDER BY e.wahlkreisID)
  // Select g.Kandidat, p.kuerzel, g.Wahlkreis
  // FROM Gewinner g, Kandidat k, Partei P
  // WHERE g.Kandidat = k.ID AND k.DMParteiID = p.id AND p.KUErzel = 'CSU'

}

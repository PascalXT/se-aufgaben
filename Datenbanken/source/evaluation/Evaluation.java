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
    database.createTemporaryTable(kSitzeTable, "ParteiID BIGING, bundeslandID BIGINT, AnzahlStimmen BIGINT");
    int[] sitzePerParty = getSitze(totalStimmen, maxSitze);
    for (int i = 0; i < parteiID.length; i++) {
      int[] sitzePerPartyAndBundesland = getSitze(stimmenPerBundesland[i], sitzePerParty[i]);
      for (int j = 0; j < bundeslandID.length; j++) {
        database.executeUpdate("INSERT INTO " + kSitzeTable + " VALUES(" + parteiID[i] + ", " + bundeslandID[j] + ", "
            + sitzePerPartyAndBundesland[j] + ")");
      }
    }
  }
  
  //public void create
  
  public void doEverything(int[] parteienInBundestag) {
    
  }

  // Temporary tables:
  // http://books.google.com/books?id=O-ueX_E_b9EC&lpg=PA276&ots=9qsV44q8wF&dq=db2%20create%20global%20temporary%20table&pg=PA276#v=onepage&q=db2%20create%20global%20temporary%20table&f=false

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

  // Auf Bundeslandebene Aggregierte Zweitstimmenergebnisse
  // SELECT wk.bundeslandid, w2.parteiid, sum(w2.anzahl) as Stimmen
  // FROM Wahlergebnis2 w2, Wahlkreis wk
  // WHERE w2.wahlkreisid = wk.id
  // GROUP BY wk.bundeslandid, w2.parteiid
  // ORDER BY parteiid
  //  
  // //DECLARE GLOBAL TEMPORARY TABLE ZweitstimmenBundesland (BundeslandID int,
  // parteiID bigint, AnzahlStimmen bigint);
  //
  //
  // CREATE TABLE Pascal.ZweitstimmenBundesland(BundeslandID, parteiID,
  // AnzahlStimmen) AS (
  // SELECT wk.bundeslandid, w2.parteiid, sum(w2.anzahl) as Stimmen
  // FROM Wahlergebnis2 w2, Wahlkreis wk
  // WHERE w2.wahlkreisid = wk.id
  // GROUP BY wk.bundeslandid, w2.parteiid
  // ) DATA INITIALLY DEFERRED REFRESH DEFERRED;
  // SET INTEGRITY FOR Pascal.ZweitstimmenBundesland IMMEDIATE CHECKED;

}

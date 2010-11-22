package evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    
    // +++++++++++++++++ DIREKTMANDATE +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.direktmandate(), 
    		Datenbank.kKandidatID + " BIGINT, " + Datenbank.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.direktmandate() 
    		+ " WITH maxErgebnis(wahlkreisID, maxStimmen) as (SELECT"
    		+ " k." + Datenbank.kKandidatDMWahlkreisID + ", MAX(v." + Datenbank.kWahlergebnis1Anzahl + ")"
    		+ " FROM " + database.wahlergebnis1() + " v, " + database.kandidat + " k"
    		+ " WHERE v." + Datenbank.kWahlergebnis1KandidatID + " = k." + Datenbank.kKandidatID
    		+ " GROUP BY k." + Datenbank.kKandidatDMWahlkreisID + ")"
    		+ " SELECT k." + Datenbank.kKandidatID + ", k." + Datenbank.kKandidatParteiID
    		+ " FROM maxErgebnis e, " + database.wahlergebnis1() + " v, " + database.kandidat + " k"
    		+ " WHERE e.wahlkreisID = v." + Datenbank.kWahlergebnis1WahlkreisID
    		+ " AND e.maxStimmen = v." + Datenbank.kWahlergebnis1Anzahl
    		+ " AND k." + Datenbank.kKandidatID + " = v." + Datenbank.kWahlergebnis1KandidatID);
    database.printResultSet(database.executeSQL("SELECT COUNT(*) FROM " + database.direktmandate()));
    
    // +++++++++++++++++ 5 PROZENT PARTEIEN +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.fuenfProzentParteien(), Datenbank.kParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.fuenfProzentParteien()
    		+ " SELECT p." + Datenbank.kParteiID
    		+ " FROM " + database.partei + " p, " + database.wahlergebnis2() + " v"
    		+ " WHERE v." + Datenbank.kWahlergebnis2ParteiID + " = p." + Datenbank.kParteiID
    		+ " GROUP BY p." + Datenbank.kParteiID
    		+ " HAVING CAST(SUM(v." + Datenbank.kWahlergebnis2Anzahl + ") AS FLOAT)"
    		+ " / (SELECT SUM(" + Datenbank.kAnzahlStimmen + ") FROM " + database.zweitStimmenNachBundesland() + ")"
    		+ " >= 0.05"
    );
    database.printResultSet(database.executeSQL("SELECT p." + Datenbank.kParteiKuerzel 
    		+ " FROM " + database.partei + " p, " + database.fuenfProzentParteien() + " fpp"
    		+ " WHERE p." + Datenbank.kParteiID + " = fpp." + Datenbank.kParteiID
    ));
    
    // +++++++++++++++++ PARTEIEN MIT MINDESTENS 3 DIREKTMANDATEN +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.dreiDirektMandatParteien(), Datenbank.kParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.dreiDirektMandatParteien()
    		+ " SELECT dm." + Datenbank.kKandidatParteiID
    		+ " FROM " + database.direktmandate() + " dm "
    		+ " GROUP BY dm." + Datenbank.kKandidatParteiID
    		+ " HAVING COUNT(*) >= 3"
    );
    database.printResultSet(database.executeSQL("SELECT p." + Datenbank.kParteiKuerzel 
    		+ " FROM " + database.partei + " p, " + database.dreiDirektMandatParteien() + " ddmp"
    		+ " WHERE p." + Datenbank.kParteiID + " = ddmp." + Datenbank.kParteiID
    ));
    
    // +++++++++++++++++ PARTEIEN IM BUNDESTAG +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.parteienImBundestag(), Datenbank.kParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.parteienImBundestag()
    		+ " SELECT * FROM " + database.fuenfProzentParteien()
    		+ " UNION "
    		+ " SELECT * FROM " + database.dreiDirektMandatParteien()
    );
    database.printResultSet(database.executeSQL("SELECT p." + Datenbank.kParteiKuerzel 
    		+ " FROM " + database.partei + " p, " + database.dreiDirektMandatParteien() + " ddmp"
    		+ " WHERE p." + Datenbank.kParteiID + " = ddmp." + Datenbank.kParteiID
    ));
    
    // +++++++++++++++++ ANZAHL PROPORZSITZE +++++++++++++++++ //
    ResultSet anzahlProporzSitzeResultSet = database.executeSQL("WITH AlleinigeDirektmandate AS ("
    		+ " SELECT dm." + Datenbank.kKandidatID
    		+ " FROM " + database.direktmandate() + " dm"
    		+ " EXCEPT "
    		+ " SELECT dm." + Datenbank.kKandidatID
    		+ " FROM " + database.direktmandate() + " dm, " + database.parteienImBundestag() + " pib"
    		+ " WHERE dm." + Datenbank.kKandidatParteiID + " = pib." + Datenbank.kParteiID + ")"
    		+ " SELECT 598 - COUNT(*) AS AnzahlProporzSitze FROM AlleinigeDirektmandate"
    );
    anzahlProporzSitzeResultSet.next();
    System.out.println("AnzahlProporzSitze: " + anzahlProporzSitzeResultSet.getInt("AnzahlProporzSitze"));
    
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

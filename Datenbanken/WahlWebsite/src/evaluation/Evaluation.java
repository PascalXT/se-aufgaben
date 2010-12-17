package evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DB;

;

public class Evaluation {
  private DB database;

  public Evaluation(DB database) {
    this.database = database;
  }

  public static int[] getSitze(Integer[] stimmen, int maxSitze) {
    return hoechstZahlVerfahren(stimmen, maxSitze);
  }

  // Returns a number of {0, ..., maxNumber}.
  private static int getRandomNumber(int maxNumber) {
    return 0;
  }

  private static int[] hoechstZahlVerfahren(Integer[] votes, int maxSitze) {
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

  public void computeSitzverteilungBundestag() throws SQLException {
    // Aggregate election results to Bundesland level.
    database.createOrReplaceTemporaryTable(database.zweitStimmenNachBundesland(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachBundesland() + " SELECT w2."
        + DB.kForeignKeyParteiID + ", wk." + DB.kForeignKeyBundeslandID + ", sum(w2."
        + DB.kWahlergebnis2Anzahl + ") as " + DB.kAnzahlStimmen + "" + " FROM "
        + database.wahlergebnis2() + " w2" + ", " + database.wahlkreis() + " wk" + " WHERE w2."
        + DB.kForeignKeyWahlkreisID + " = wk." + DB.kID + " GROUP BY " + "wk."
        + DB.kForeignKeyBundeslandID + ", w2." + DB.kForeignKeyParteiID);

    database.createOrReplaceTemporaryTable(database.zweitStimmenNachPartei(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachPartei() + " SELECT "
        + DB.kForeignKeyParteiID + ", SUM(" + DB.kAnzahlStimmen + ") FROM "
        + database.zweitStimmenNachBundesland() + " GROUP BY " + DB.kForeignKeyParteiID);

    database.printTable(database.zweitStimmenNachBundesland());
    database.printTable(database.zweitStimmenNachPartei());
    database.printResultSet(database.executeSQL(""
        + "SELECT " + DB.kForeignKeyParteiID + ", SUM(" + DB.kAnzahlStimmen + ") "
        + "FROM " + database.zweitStimmenNachBundesland() + " "
        + "GROUP BY " + DB.kForeignKeyParteiID));

    // +++++++++++++++++ DIREKTMANDATE +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.direktmandate(), DB.kForeignKeyKandidatID + " BIGINT, "
        + DB.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate(""
        + "INSERT INTO " + database.direktmandate() + " "
        + "WITH maxErgebnis(wahlkreisID, maxStimmen) as ("
          + "SELECT k." + DB.kKandidatDMWahlkreisID + ", MAX(v." + DB.kWahlergebnis1Anzahl + ") "
          + "FROM " + database.wahlergebnis1() + " v, " + database.kandidat() + " k "
          + "WHERE v." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " "
          + "GROUP BY k." + DB.kKandidatDMWahlkreisID + ") "
        + "SELECT k." + DB.kID + " as " + DB.kForeignKeyKandidatID + ", "
               + "k." + DB.kForeignKeyParteiID + " "
        + "FROM maxErgebnis e, " + database.wahlergebnis1() + " v, " + database.kandidat() + " k "
        + "WHERE e.wahlkreisID = v." + DB.kForeignKeyWahlkreisID + " "
          + "AND e.maxStimmen = v." + DB.kWahlergebnis1Anzahl + " "
          + "AND k." + DB.kID + " = v." + DB.kForeignKeyKandidatID);
    
    database.printResultSet(database.executeSQL("SELECT COUNT(*) FROM " + database.direktmandate()));

    // +++++++++++++++++ 5 PROZENT PARTEIEN +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.fuenfProzentParteien(), DB.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.fuenfProzentParteien() + " SELECT p." + DB.kID + " as "
        + DB.kForeignKeyParteiID + " FROM " + database.partei() + " p, " + database.wahlergebnis2() + " v"
        + " WHERE v." + DB.kForeignKeyParteiID + " = p." + DB.kID + " GROUP BY p." + DB.kID
        + " HAVING CAST(SUM(v." + DB.kWahlergebnis2Anzahl + ") AS FLOAT)" + " / (SELECT SUM("
        + DB.kAnzahlStimmen + ") FROM " + database.zweitStimmenNachBundesland() + ")" + " >= 0.05");
    database.printResultSet(database.executeSQL("SELECT p." + DB.kParteiKuerzel + " FROM " + database.partei()
        + " p, " + database.fuenfProzentParteien() + " fpp" + " WHERE p." + DB.kID + " = fpp."
        + DB.kForeignKeyParteiID));

    // +++++++++++++++++ PARTEIEN MIT MINDESTENS 3 DIREKTMANDATEN
    // +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.dreiDirektMandatParteien(), DB.kForeignKeyParteiID
        + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.dreiDirektMandatParteien() + " SELECT dm."
        + DB.kForeignKeyParteiID + " FROM " + database.direktmandate() + " dm " + " GROUP BY dm."
        + DB.kForeignKeyParteiID + " HAVING COUNT(*) >= 3");
    database.printResultSet(database.executeSQL("SELECT p." + DB.kParteiKuerzel + " FROM " + database.partei()
        + " p, " + database.dreiDirektMandatParteien() + " ddmp" + " WHERE p." + DB.kID + " = ddmp."
        + DB.kForeignKeyParteiID));

    // +++++++++++++++++ PARTEIEN IM BUNDESTAG +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.parteienImBundestag(), DB.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.parteienImBundestag() + " SELECT * FROM "
        + database.fuenfProzentParteien() + " UNION " + " SELECT * FROM " + database.dreiDirektMandatParteien());
    database.printResultSet(database.executeSQL("SELECT p." + DB.kParteiKuerzel + " FROM " + database.partei()
        + " p, " + database.dreiDirektMandatParteien() + " ddmp" + " WHERE p." + DB.kID + " = ddmp."
        + DB.kForeignKeyParteiID));

    // +++++++++++++++++ ANZAHL PROPORZSITZE +++++++++++++++++ //
    ResultSet anzahlProporzSitzeResultSet = database.executeSQL("WITH AlleinigeDirektmandate AS (" + " SELECT dm."
        + DB.kForeignKeyKandidatID + " FROM " + database.direktmandate() + " dm" + " EXCEPT " + " SELECT dm."
        + DB.kForeignKeyKandidatID + " FROM " + database.direktmandate() + " dm, "
        + database.parteienImBundestag() + " pib" + " WHERE dm." + DB.kForeignKeyParteiID + " = pib."
        + DB.kForeignKeyParteiID + ")"
        + " SELECT 598 - COUNT(*) AS AnzahlProporzSitze FROM AlleinigeDirektmandate");
    anzahlProporzSitzeResultSet.next();
    int anzahlProporzSitze = anzahlProporzSitzeResultSet.getInt("AnzahlProporzSitze");
    System.out.println("AnzahlProporzSitze: " + anzahlProporzSitze);

    // +++++++++++++++++ Sitze nach Partei +++++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.sitzeNachPartei(), DB.kForeignKeyParteiID + " BIGINT, "
        + DB.kAnzahlSitze + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.sitzeNachPartei() + " "
        + "WITH Divisoren (wert) as (SELECT ROW_NUMBER() OVER (order by w." + DB.kID + ") - 0.5 FROM "
        + database.wahlkreis() + " w " + "UNION SELECT ROW_NUMBER() OVER (order by w." + DB.kID
        + ") + (SELECT COUNT(*) FROM " + database.wahlkreis() + ") - 0.5 FROM " + database.wahlkreis() + " w), "
        + "Zugriffsreihenfolge (" + DB.kForeignKeyParteiID + ", " + DB.kAnzahlStimmen
        + ", DivWert, Rang) as " + "(SELECT p." + DB.kForeignKeyParteiID + ", z." + DB.kAnzahlStimmen
        + ", (z." + DB.kAnzahlStimmen + " / d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY (z."
        + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + database.parteienImBundestag() + " p, "
        + database.zweitStimmenNachPartei() + " z, Divisoren d " + "WHERE p." + DB.kForeignKeyParteiID + " = z."
        + DB.kForeignKeyParteiID + " ORDER BY DivWert desc) " + "SELECT " + DB.kForeignKeyParteiID
        + ", COUNT(Rang) as " + DB.kAnzahlSitze + " FROM Zugriffsreihenfolge " + " WHERE Rang <= 598 "
        + " GROUP BY ParteiID");

    // +++++++++++++++++ Sitze nach Landeslisten +++++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.sitzeNachLandeslisten(), DB.kForeignKeyParteiID
        + " BIGINT, " + DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlSitze + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.sitzeNachLandeslisten() + " " + "WITH Divisoren (wert) as ( "
        + "SELECT ROW_NUMBER() OVER (order by w.ID) - 0.5 FROM " + database.wahlkreis() + " w "
        + "UNION SELECT ROW_NUMBER() OVER (order by w.ID) + (SELECT COUNT(*) FROM " + database.wahlkreis()
        + ") - 0.5 FROM " + database.wahlkreis() + " w " + "), " + " " + "Zugriffsreihenfolge ("
        + DB.kForeignKeyParteiID + ", " + DB.kForeignKeyBundeslandID
        + ", AnzahlStimmen, DivWert, Rang) as " + "(SELECT p." + DB.kForeignKeyParteiID + ", z."
        + DB.kForeignKeyBundeslandID + ", z." + DB.kAnzahlStimmen + ", (z." + DB.kAnzahlStimmen
        + " / d.wert) as DivWert, ROW_NUMBER() OVER (PARTITION BY p." + DB.kForeignKeyParteiID + " ORDER BY (z."
        + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + database.parteienImBundestag() + " p, "
        + database.zweitStimmenNachBundesland() + " z, Divisoren d " + "WHERE p." + DB.kForeignKeyParteiID
        + " = z." + DB.kForeignKeyParteiID + " ORDER BY " + DB.kForeignKeyParteiID + ", DivWert desc) "
        + " " + "SELECT z." + DB.kForeignKeyParteiID + ", " + DB.kForeignKeyBundeslandID
        + ", COUNT(Rang) as " + DB.kAnzahlSitze + " " + "FROM Zugriffsreihenfolge z, "
        + database.sitzeNachPartei() + " s " + "WHERE z." + DB.kForeignKeyParteiID + " = s."
        + DB.kForeignKeyParteiID + " AND z.Rang <= s." + DB.kAnzahlSitze + " " + "GROUP BY z."
        + DB.kForeignKeyParteiID + ", z." + DB.kForeignKeyBundeslandID + ", s."
        + DB.kForeignKeyParteiID);

    database.printTable(database.sitzeNachLandeslisten());
  }

}

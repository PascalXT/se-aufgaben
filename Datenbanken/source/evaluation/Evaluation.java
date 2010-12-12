package evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import datenbank.Datenbank;

;

public class Evaluation {
  private Datenbank database;

  public Evaluation(Datenbank database) {
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
    database.createOrReplaceTemporaryTable(database.zweitStimmenNachBundesland(), Datenbank.kForeignKeyParteiID
        + " BIGINT, " + Datenbank.kForeignKeyBundeslandID + " BIGINT, " + Datenbank.kAnzahlStimmen + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachBundesland() + " SELECT w2."
        + Datenbank.kForeignKeyParteiID + ", wk." + Datenbank.kForeignKeyBundeslandID + ", sum(w2."
        + Datenbank.kWahlergebnis2Anzahl + ") as " + Datenbank.kAnzahlStimmen + "" + " FROM "
        + database.wahlergebnis2() + " w2" + ", " + database.wahlkreis() + " wk" + " WHERE w2."
        + Datenbank.kForeignKeyWahlkreisID + " = wk." + Datenbank.kID + " GROUP BY " + "wk."
        + Datenbank.kForeignKeyBundeslandID + ", w2." + Datenbank.kForeignKeyParteiID);

    database.createOrReplaceTemporaryTable(database.zweitStimmenNachPartei(), Datenbank.kForeignKeyParteiID
        + " BIGINT, " + Datenbank.kAnzahlStimmen + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.zweitStimmenNachPartei() + " SELECT "
        + Datenbank.kForeignKeyParteiID + ", SUM(" + Datenbank.kAnzahlStimmen + ") FROM "
        + database.zweitStimmenNachBundesland() + " GROUP BY " + Datenbank.kForeignKeyParteiID);

    database.printTable(database.zweitStimmenNachBundesland());
    database.printTable(database.zweitStimmenNachPartei());
    database.printResultSet(database.executeSQL("SELECT " + Datenbank.kForeignKeyParteiID + ", SUM("
        + Datenbank.kAnzahlStimmen + ") FROM " + database.zweitStimmenNachBundesland() + " GROUP BY "
        + Datenbank.kForeignKeyParteiID));

    // +++++++++++++++++ DIREKTMANDATE +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.direktmandate(), Datenbank.kForeignKeyKandidatID + " BIGINT, "
        + Datenbank.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.direktmandate()
        + " WITH maxErgebnis(wahlkreisID, maxStimmen) as (SELECT" + " k." + Datenbank.kKandidatDMWahlkreisID
        + ", MAX(v." + Datenbank.kWahlergebnis1Anzahl + ")" + " FROM " + database.wahlergebnis1() + " v, "
        + database.kandidat() + " k" + " WHERE v." + Datenbank.kForeignKeyKandidatID + " = k." + Datenbank.kID
        + " GROUP BY k." + Datenbank.kKandidatDMWahlkreisID + ")" + " SELECT k." + Datenbank.kID + " as "
        + Datenbank.kForeignKeyKandidatID + ", k." + Datenbank.kForeignKeyParteiID + " FROM maxErgebnis e, "
        + database.wahlergebnis1() + " v, " + database.kandidat() + " k" + " WHERE e.wahlkreisID = v."
        + Datenbank.kForeignKeyWahlkreisID + " AND e.maxStimmen = v." + Datenbank.kWahlergebnis1Anzahl + " AND k."
        + Datenbank.kID + " = v." + Datenbank.kForeignKeyKandidatID);
    database.printResultSet(database.executeSQL("SELECT COUNT(*) FROM " + database.direktmandate()));

    // +++++++++++++++++ 5 PROZENT PARTEIEN +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.fuenfProzentParteien(), Datenbank.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.fuenfProzentParteien() + " SELECT p." + Datenbank.kID + " as "
        + Datenbank.kForeignKeyParteiID + " FROM " + database.partei() + " p, " + database.wahlergebnis2() + " v"
        + " WHERE v." + Datenbank.kForeignKeyParteiID + " = p." + Datenbank.kID + " GROUP BY p." + Datenbank.kID
        + " HAVING CAST(SUM(v." + Datenbank.kWahlergebnis2Anzahl + ") AS FLOAT)" + " / (SELECT SUM("
        + Datenbank.kAnzahlStimmen + ") FROM " + database.zweitStimmenNachBundesland() + ")" + " >= 0.05");
    database.printResultSet(database.executeSQL("SELECT p." + Datenbank.kParteiKuerzel + " FROM " + database.partei()
        + " p, " + database.fuenfProzentParteien() + " fpp" + " WHERE p." + Datenbank.kID + " = fpp."
        + Datenbank.kForeignKeyParteiID));

    // +++++++++++++++++ PARTEIEN MIT MINDESTENS 3 DIREKTMANDATEN
    // +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.dreiDirektMandatParteien(), Datenbank.kForeignKeyParteiID
        + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.dreiDirektMandatParteien() + " SELECT dm."
        + Datenbank.kForeignKeyParteiID + " FROM " + database.direktmandate() + " dm " + " GROUP BY dm."
        + Datenbank.kForeignKeyParteiID + " HAVING COUNT(*) >= 3");
    database.printResultSet(database.executeSQL("SELECT p." + Datenbank.kParteiKuerzel + " FROM " + database.partei()
        + " p, " + database.dreiDirektMandatParteien() + " ddmp" + " WHERE p." + Datenbank.kID + " = ddmp."
        + Datenbank.kForeignKeyParteiID));

    // +++++++++++++++++ PARTEIEN IM BUNDESTAG +++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.parteienImBundestag(), Datenbank.kForeignKeyParteiID + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.parteienImBundestag() + " SELECT * FROM "
        + database.fuenfProzentParteien() + " UNION " + " SELECT * FROM " + database.dreiDirektMandatParteien());
    database.printResultSet(database.executeSQL("SELECT p." + Datenbank.kParteiKuerzel + " FROM " + database.partei()
        + " p, " + database.dreiDirektMandatParteien() + " ddmp" + " WHERE p." + Datenbank.kID + " = ddmp."
        + Datenbank.kForeignKeyParteiID));

    // +++++++++++++++++ ANZAHL PROPORZSITZE +++++++++++++++++ //
    ResultSet anzahlProporzSitzeResultSet = database.executeSQL("WITH AlleinigeDirektmandate AS (" + " SELECT dm."
        + Datenbank.kForeignKeyKandidatID + " FROM " + database.direktmandate() + " dm" + " EXCEPT " + " SELECT dm."
        + Datenbank.kForeignKeyKandidatID + " FROM " + database.direktmandate() + " dm, "
        + database.parteienImBundestag() + " pib" + " WHERE dm." + Datenbank.kForeignKeyParteiID + " = pib."
        + Datenbank.kForeignKeyParteiID + ")"
        + " SELECT 598 - COUNT(*) AS AnzahlProporzSitze FROM AlleinigeDirektmandate");
    anzahlProporzSitzeResultSet.next();
    int anzahlProporzSitze = anzahlProporzSitzeResultSet.getInt("AnzahlProporzSitze");
    System.out.println("AnzahlProporzSitze: " + anzahlProporzSitze);

    // +++++++++++++++++ Sitze nach Partei +++++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.sitzeNachPartei(), Datenbank.kForeignKeyParteiID + " BIGINT, "
        + Datenbank.kAnzahlSitze + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.sitzeNachPartei() + " "
        + "WITH Divisoren (wert) as (SELECT ROW_NUMBER() OVER (order by w." + Datenbank.kID + ") - 0.5 FROM "
        + database.wahlkreis() + " w " + "UNION SELECT ROW_NUMBER() OVER (order by w." + Datenbank.kID
        + ") + (SELECT COUNT(*) FROM " + database.wahlkreis() + ") - 0.5 FROM " + database.wahlkreis() + " w), "
        + "Zugriffsreihenfolge (" + Datenbank.kForeignKeyParteiID + ", " + Datenbank.kAnzahlStimmen
        + ", DivWert, Rang) as " + "(SELECT p." + Datenbank.kForeignKeyParteiID + ", z." + Datenbank.kAnzahlStimmen
        + ", (z." + Datenbank.kAnzahlStimmen + " / d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY (z."
        + Datenbank.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + database.parteienImBundestag() + " p, "
        + database.zweitStimmenNachPartei() + " z, Divisoren d " + "WHERE p." + Datenbank.kForeignKeyParteiID + " = z."
        + Datenbank.kForeignKeyParteiID + " ORDER BY DivWert desc) " + "SELECT " + Datenbank.kForeignKeyParteiID
        + ", COUNT(Rang) as " + Datenbank.kAnzahlSitze + " FROM Zugriffsreihenfolge " + " WHERE Rang <= 598 "
        + " GROUP BY ParteiID");

    // +++++++++++++++++ Sitze nach Landeslisten +++++++++++++++++++ //
    database.createOrReplaceTemporaryTable(database.sitzeNachLandeslisten(), Datenbank.kForeignKeyParteiID
        + " BIGINT, " + Datenbank.kForeignKeyBundeslandID + " BIGINT, " + Datenbank.kAnzahlSitze + " BIGINT");
    database.executeUpdate("INSERT INTO " + database.sitzeNachLandeslisten() + " " + "WITH Divisoren (wert) as ( "
        + "SELECT ROW_NUMBER() OVER (order by w.ID) - 0.5 FROM " + database.wahlkreis() + " w "
        + "UNION SELECT ROW_NUMBER() OVER (order by w.ID) + (SELECT COUNT(*) FROM " + database.wahlkreis()
        + ") - 0.5 FROM " + database.wahlkreis() + " w " + "), " + " " + "Zugriffsreihenfolge ("
        + Datenbank.kForeignKeyParteiID + ", " + Datenbank.kForeignKeyBundeslandID
        + ", AnzahlStimmen, DivWert, Rang) as " + "(SELECT p." + Datenbank.kForeignKeyParteiID + ", z."
        + Datenbank.kForeignKeyBundeslandID + ", z." + Datenbank.kAnzahlStimmen + ", (z." + Datenbank.kAnzahlStimmen
        + " / d.wert) as DivWert, ROW_NUMBER() OVER (PARTITION BY p." + Datenbank.kForeignKeyParteiID + " ORDER BY (z."
        + Datenbank.kAnzahlStimmen + " / d.wert) DESC) as Rang " + "FROM " + database.parteienImBundestag() + " p, "
        + database.zweitStimmenNachBundesland() + " z, Divisoren d " + "WHERE p." + Datenbank.kForeignKeyParteiID
        + " = z." + Datenbank.kForeignKeyParteiID + " ORDER BY " + Datenbank.kForeignKeyParteiID + ", DivWert desc) "
        + " " + "SELECT z." + Datenbank.kForeignKeyParteiID + ", " + Datenbank.kForeignKeyBundeslandID
        + ", COUNT(Rang) as " + Datenbank.kAnzahlSitze + " " + "FROM Zugriffsreihenfolge z, "
        + database.sitzeNachPartei() + " s " + "WHERE z." + Datenbank.kForeignKeyParteiID + " = s."
        + Datenbank.kForeignKeyParteiID + " AND z.Rang <= s." + Datenbank.kAnzahlSitze + " " + "GROUP BY z."
        + Datenbank.kForeignKeyParteiID + ", z." + Datenbank.kForeignKeyBundeslandID + ", s."
        + Datenbank.kForeignKeyParteiID);

    database.printTable(database.sitzeNachLandeslisten());
  }

}

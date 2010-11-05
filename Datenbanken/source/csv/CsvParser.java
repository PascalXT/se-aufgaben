package csv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.SQLException;

import datenbank.Datenbank;

public class CsvParser {

	final int kAktuellesJahr = 2009;
	final int kMaxBewohnerProBezirk = 2500;
	String stimmenDatei = "H:\\stimmen.csv";

	private Datenbank datenbank;

	public CsvParser(Datenbank datenbank) {
		this.datenbank = datenbank;
	}

	public CsvParser(Datenbank datenbank, String stimmenDatei) {
		this.datenbank = datenbank;
		this.stimmenDatei = stimmenDatei;
	}

	private int getKandidat(int wahlkreisID, int parteiID) throws SQLException {
		final int kKeineParteiID = 99;
		String parteiIDString;
		if (parteiID == kKeineParteiID) {
		  parteiIDString = " is NULL";
		} else {
		  parteiIDString = "=" + parteiID;
		}
		final String sql = 
      "SELECT " + Datenbank.kKandidatID + " " +
      "FROM " + datenbank.kandidat + " " + 
      "WHERE " + Datenbank.kKandidatDMWahlkreisID + "=" + wahlkreisID + " " +
      "AND " + Datenbank.kKandidatDMParteiID + parteiIDString;
		ResultSet resultSet = datenbank.executeSQL(sql);
    resultSet.next();
    final int result = resultSet.getInt(Datenbank.kKandidatID);
    resultSet.close();
    return result;
	}

	private void einzelneErststimme(int wahlkreisID, int bezirkID,
	    int kandidatID, FileWriter fileWriter) {
		String csv_zeile = wahlkreisID + ";" + bezirkID + ";" + kandidatID;
	  try {
      fileWriter.write(csv_zeile + "\n");
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
	}

	private void insertErststimmen(int wahlkreisID, int parteiID,
	    int anzahlErststimmen, int anzahlWahlbezirke, FileWriter fileWriter) {
	  if (anzahlErststimmen == 0) return;
    try {
  	  final int kandidatID = getKandidat(wahlkreisID, parteiID);
  		for (int i = 0; i < anzahlWahlbezirke; i++) {
        int anzahlErststimmenBezirk = kMaxBewohnerProBezirk;
        if (i == anzahlWahlbezirke) {
          anzahlErststimmenBezirk = anzahlErststimmen - kMaxBewohnerProBezirk *
                                    (anzahlWahlbezirke - 1);
        }
        System.out.println(wahlkreisID + ";" + i + ";" + kandidatID + ":" +
            anzahlErststimmenBezirk);
  			for (int j = 0; j < anzahlErststimmenBezirk; j++) {
  			  einzelneErststimme(wahlkreisID, i, kandidatID, fileWriter);
  			}
  		}
    } catch (SQLException e) {
      e.printStackTrace();
      System.exit(1);
    }
	}

	public void parseVotes(String csv_datei) throws IOException {
		final int kSpalteWahlkreisID = 0;
		final int kSpalteParteiID = 1;
		final int kSpalteErststimmen = 2;
		final int kSpalteZweitstimmen = 3;
		final int kSpalteJahr = 4;

		FileReader file_reader = new FileReader(csv_datei);
    FileWriter fileWriter = new FileWriter(stimmenDatei);
		LineNumberReader line_number_reader = new LineNumberReader(file_reader);
		String next_line = line_number_reader.readLine();  // Skip Header line.
		while ((next_line = line_number_reader.readLine()) != null) {
			String[] tokens = next_line.split(";");
			assert (tokens.length == 5);

			if (Integer.parseInt(tokens[kSpalteJahr]) == kAktuellesJahr) {
				final int anzahl_erststimmen = Integer
				    .parseInt("0" + tokens[kSpalteErststimmen]);
				final int anzahl_zweitstimmen = Integer
				    .parseInt("0" + tokens[kSpalteZweitstimmen]);
				final int anzahl_wahlbezirke = Math.max(anzahl_erststimmen,
				    anzahl_zweitstimmen)
				    / kMaxBewohnerProBezirk;
				insertErststimmen(Integer.parseInt(tokens[kSpalteWahlkreisID]),
				    Integer.parseInt(tokens[kSpalteParteiID]),
				    anzahl_erststimmen,
				    anzahl_wahlbezirke,
				    fileWriter);
				fileWriter.flush();
			}
		}
		fileWriter.close();
	}

	public void runImports(String datenordner, String message_pfad) {
		final int kDatei = 0;
		final int kSpaltenNummern = 1;
		final int kSpaltenNamen = 2;
		final String[][] kImportTripel = {
        {
          "Bundeslaender.csv",
          "(1, 2)",
          datenbank.bundesland + " (" + Datenbank.kBundeslandID + ", "
              + Datenbank.kBundeslandName + ")" },
		    {
	        "Wahlkreise.csv",
	        "(1, 2, 3)",
	        datenbank.wahlkreis + " (" + Datenbank.kWahlkreisID + ", " +
	            Datenbank.kWahlkreisName + ", " +
	            Datenbank.kWahlkreisBundeslandID + ")" },
		    {
	        "Parteien.csv",
	        "(3, 1, 2)",
	        datenbank.partei + " (" + Datenbank.kParteiID + ", "
	            + Datenbank.kParteiKuerzel + ", " + Datenbank.kParteiName + 
	            ")"},
		    {
	        "Kandidaten.csv",
	        "(1, 2, 3, 4, 5, 3, 6)",
	        datenbank.kandidat + " (" + Datenbank.kKandidatNachname + ", " +
	            Datenbank.kKandidatVorname + ", " +
	            Datenbank.kKandidatParteiID + ", " +
	            Datenbank.kKandidatBundeslandID + ", " +
	            Datenbank.kKandidatListenplatz + ", " +
	            Datenbank.kKandidatDMParteiID + ", " +
	            Datenbank.kKandidatDMWahlkreisID +
	            ")" }, };

		for (int i = 0; i < Math.min(4, kImportTripel.length); i++) {
			System.out.println("Import: " + kImportTripel[i][kDatei]);
			final String datei_pfad = datenordner + kImportTripel[i][kDatei];
			String sql = "IMPORT FROM \"" + datei_pfad
			    + "\" OF DEL MODIFIED BY COLDEL; METHOD P "
			    + kImportTripel[i][kSpaltenNummern] + " SKIPCOUNT 1 MESSAGES \""
			    + message_pfad + "\" INSERT INTO " + kImportTripel[i][kSpaltenNamen];
			System.out.println(sql);
			datenbank.executeDB2(sql);
		}
	}

}

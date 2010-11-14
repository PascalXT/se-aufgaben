package csv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.SQLException;

import datenbank.Datenbank;

public class CsvParser {

	final int kMaxBewohnerProBezirk = 2500;
	final int kInvalidID = -1;
	String stimmenFile;
	String erststimmenAggregiertFile;
	String zweitstimmenAggregiertFile;

	private Datenbank datenbank;

	public CsvParser(Datenbank datenbank) {
		this.datenbank = datenbank;
	}

	public CsvParser(Datenbank datenbank, String stimmenFile, String erstStimmenAggregiertFile, String zweitStimmenAggregiertFile) {
		this.datenbank = datenbank;
		this.stimmenFile = stimmenFile;
		this.erststimmenAggregiertFile = erstStimmenAggregiertFile;
    this.zweitstimmenAggregiertFile = zweitStimmenAggregiertFile;
	}

	private int getKandidat(int wahlkreisID, int parteiID) throws SQLException {
		final int kKeineParteiID = 99;
		String parteiIDString;
		if (parteiID == kKeineParteiID) {
			parteiIDString = " is NULL";
		} else {
			parteiIDString = "=" + parteiID;
		}
		final String sql = "SELECT " + Datenbank.kKandidatID + " " + "FROM " + datenbank.kandidat + " " + "WHERE " + Datenbank.kKandidatDMWahlkreisID + "=" + wahlkreisID + " " + "AND " + Datenbank.kKandidatDMParteiID + parteiIDString;
		ResultSet resultSet = datenbank.executeSQL(sql);
		resultSet.next();
		final int result = resultSet.getInt(Datenbank.kKandidatID);
		resultSet.close();
		return result;
	}
	
	private String getBundesland(int wahlkreisID) throws SQLException {
		String sql = "SELECT " + Datenbank.kBundeslandName + " FROM " + datenbank.bundesland + " WHERE " + Datenbank.kBundeslandID + "=" + "(SELECT " + Datenbank.kWahlkreisBundeslandID + " FROM " + datenbank.wahlkreis + " WHERE " + Datenbank.kWahlkreisID + "=" + wahlkreisID + ")";
		ResultSet resultSet = datenbank.executeSQL(sql);
		resultSet.next();
		final String result = resultSet.getString(Datenbank.kBundeslandName);
		resultSet.close();
		return result;
	}

	/**
	 * Creates the single votes based on the overall election results.
	 * @param csvFile A file containing the election results.
	 * @param bundesland If not null, only votes for this Bundesland will be
	 *        created.
	 * @param messagePath Location where the db2 messages will be stored.
	 * @throws IOException
	 * @throws SQLException
	 */
	public void parseVotes(String csvFile, String bundesland, String messagePath) throws IOException, SQLException {
		final int kSpalteWahlkreisID = 0;
		final int kSpalteParteiID = 1;
		final int kSpalteErststimmen = 2;
		final int kSpalteZweitstimmen = 3;
		final int kSpalteJahr = 4;

    FileWriter fileWriterStimmen = new FileWriter(stimmenFile);
    FileWriter fileWriterErststimmenAggregiert = new FileWriter(erststimmenAggregiertFile);
		FileWriter fileWriterZweitstimmenAggregiert = new FileWriter(zweitstimmenAggregiertFile);
		
		// These two values are necessary for inventing the right Wahlbezirke.
		int currentWahlbezirk = -1;
		int currentWahlkreis = -1;
		
		FileReader fileReader = new FileReader(csvFile);
    LineNumberReader line_number_reader = new LineNumberReader(fileReader);
    String next_line = line_number_reader.readLine();  // Skip Header line.
		while ((next_line = line_number_reader.readLine()) != null) {
			String[] tokens = next_line.split(";");
			assert (tokens.length == 5);
			
			final int jahr = Integer.parseInt(tokens[kSpalteJahr]);
			if (jahr == 2009) { // TODO: support 2005 as well
			
				final int wahlkreisID = Integer.parseInt(tokens[kSpalteWahlkreisID]);
				if (wahlkreisID != currentWahlkreis) {
				  currentWahlbezirk = 1;
				}

				if (bundesland == null || bundesland.equals(getBundesland(wahlkreisID))) {
					final int parteiId = Integer.parseInt(tokens[kSpalteParteiID]);
					final int anzahlErststimmen = Integer.parseInt("0" + tokens[kSpalteErststimmen]);
					final int anzahlZweitstimmen = Integer.parseInt("0" + tokens[kSpalteZweitstimmen]);
					final int anzahlWahlbezirke = Math.max(anzahlErststimmen, anzahlZweitstimmen) / kMaxBewohnerProBezirk;
					
					writeErstAndZweitstimmen(jahr, wahlkreisID, parteiId,
					    anzahlErststimmen, anzahlZweitstimmen, currentWahlbezirk,
					    anzahlWahlbezirke, fileWriterStimmen);
					
					
					writeErststimmenAggregiert(jahr, wahlkreisID, parteiId, anzahlErststimmen, fileWriterErststimmenAggregiert);
					writeZweitstimmenAggregiert(jahr, wahlkreisID, parteiId, anzahlZweitstimmen, fileWriterZweitstimmenAggregiert);
          fileWriterStimmen.flush();
					fileWriterErststimmenAggregiert.flush();
					fileWriterZweitstimmenAggregiert.flush();
				}
			}

		}
		fileWriterStimmen.close();
		System.out.println("Stimmen files have been created.");
	}
	
	private void doppelStimme(int jahr, int wahlkreisID, int bezirkID, String votedForErstimmeID, String votedForZweitstimmeID, FileWriter fileWriter) {
	    String csvLine = jahr + ";" + wahlkreisID + ";" + bezirkID + ";" + votedForErstimmeID + ";" + votedForZweitstimmeID;
	    try {
	      fileWriter.write(csvLine + "\n");
	    } catch (IOException e) {
	      e.printStackTrace();
	      System.exit(1);
	    }
	  }
	
  private void writeErstAndZweitstimmen(int jahr, int wahlkreisID, int parteiID,
      int anzahlErststimmen, int anzahlZweitstimmen, int lastWahlbezirk,
      int anzahlWahlbezirke, FileWriter fileWriter) {
    int kandidatID = kInvalidID;
    try {
      if (anzahlErststimmen > 0)
        kandidatID = getKandidat(wahlkreisID, parteiID);
    } catch (SQLException e) {
      System.out.println("No candidate could be found.");
      e.printStackTrace();
      System.exit(1);
    }
    
    int remainingErststimmen = anzahlErststimmen;  // # processed Erststimmen.
    int remainingZweitstimmen = anzahlZweitstimmen;  // # processed Zweitstimmen.
    for (int i = 0; i < anzahlWahlbezirke; i++) {
      int anzahlErststimmenBezirk = Math.min(kMaxBewohnerProBezirk, remainingErststimmen);
      remainingErststimmen -= anzahlErststimmenBezirk;
      int anzahlZweitstimmenBezirk = Math.min(kMaxBewohnerProBezirk, remainingZweitstimmen);
      remainingZweitstimmen -= anzahlZweitstimmenBezirk;
      
      final int wahlbezirk = i + lastWahlbezirk;
      System.out.println(wahlkreisID + ";" + wahlbezirk + ";" + kandidatID + ":"
          + anzahlErststimmenBezirk + ";" + parteiID + ":" + anzahlZweitstimmenBezirk);
      final int anzahlStimmenBezirk = Math.max(anzahlErststimmenBezirk, anzahlZweitstimmenBezirk);
      for (int j = 0; j < anzahlStimmenBezirk; j++) {
        final String stimmeKandidatID = (j < anzahlErststimmenBezirk) ? Integer.toString(kandidatID) : "";
        final String stimmeParteiID = (j < anzahlZweitstimmenBezirk) ? Integer.toString(parteiID) : "";
        doppelStimme(jahr, wahlkreisID, wahlbezirk, stimmeKandidatID, stimmeParteiID, fileWriter);
      }
    }
    assert(remainingErststimmen == 0);
    assert(remainingZweitstimmen == 0);
  }
	
	private void writeErststimmenAggregiert(int jahr, int wahlkreisID, int parteiID, int anzahlErststimmen, FileWriter fileWriter) {
		if (anzahlErststimmen == 0)
			return;
		try {
			final int kandidatID = getKandidat(wahlkreisID, parteiID);
			String csvLine = jahr + ";" + wahlkreisID + ";" + anzahlErststimmen + ";" + kandidatID;
			fileWriter.write(csvLine + "\n");
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
  }
	
	private void writeZweitstimmenAggregiert(int jahr, int wahlkreisID, int parteiID, int anzahlZweitstimmen, FileWriter fileWriter) {
		if (anzahlZweitstimmen == 0)
			return;
		try {
			String csvLine = jahr + ";" + wahlkreisID + ";" + anzahlZweitstimmen + ";" + parteiID;
			fileWriter.write(csvLine + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
  }

	public void importVotes(String messagePfad) {
		
		try {
			datenbank.truncate(datenbank.stimme);
			datenbank.truncate(datenbank.wahlergebnis1);
			datenbank.truncate(datenbank.wahlergebnis2);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		final String importStimmenStmt = "LOAD FROM \"" + stimmenFile + "\" OF DEL MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 4, 5) SAVECOUNT 10000 " + "MESSAGES \"" + messagePfad + "\" " 
		+ "INSERT INTO " + datenbank.stimme 
		+ " (" + Datenbank.kStimmeJahr + ", " + Datenbank.kStimmeWahlkreisID + ", " + Datenbank.kStimmeWahlbezirkID + ", " + Datenbank.kStimmeKandidatID + "," + Datenbank.kStimmeParteiID + ")";

		System.out.println(importStimmenStmt);
		datenbank.executeDB2(importStimmenStmt);
		System.out.println("Stimmen have been imported to the database");
	
		final String importErststimmenAggregiertStmt = "LOAD FROM \"" + erststimmenAggregiertFile + "\" OF DEL MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 4) SAVECOUNT 10000 " + "MESSAGES \"" + messagePfad + "\" " 
		+ "INSERT INTO " + datenbank.wahlergebnis1 
		+ " (" + Datenbank.kWahlergebnis1Jahr + ", " + Datenbank.kWahlergebnis1WahlkreisID + ", " + Datenbank.kWahlergebnis1Anzahl + ", " + Datenbank.kWahlergebnis1KandidatID + ")";

		System.out.println(importErststimmenAggregiertStmt);
		datenbank.executeDB2(importErststimmenAggregiertStmt);
		System.out.println("ErststimmenAggregiert have been imported to the database");

		final String importZweitstimmenAggregiertStmt = "LOAD FROM \"" + zweitstimmenAggregiertFile + "\" OF DEL MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 4) SAVECOUNT 10000 " + "MESSAGES \"" + messagePfad + "\" " 
		+ "INSERT INTO " + datenbank.wahlergebnis2 
		+ " (" + Datenbank.kWahlergebnis2Jahr + ", " + Datenbank.kWahlergebnis2WahlkreisID + ", " + Datenbank.kWahlergebnis2Anzahl + ", " + Datenbank.kWahlergebnis2ParteiID + ")";

		System.out.println(importZweitstimmenAggregiertStmt);
		datenbank.executeDB2(importZweitstimmenAggregiertStmt);
		System.out.println("ZweitstimmenAggregiert have been imported to the database");
	}

	public void runImports(String dataFolder, String messagePfad) {
		final int kFile = 0;
		final int kColumnNumber = 1;
		final int kTableName = 2;
		final int kColumnName = 3;
		final String[][] kImportTripel = { 
				{ "Bundeslaender.csv", "(1, 2)", datenbank.bundesland, " (" + Datenbank.kBundeslandID + ", " + Datenbank.kBundeslandName + ")" }, 
				{ "Wahlkreise.csv", "(1, 2, 3)", datenbank.wahlkreis, " (" + Datenbank.kWahlkreisID + ", " + Datenbank.kWahlkreisName + ", " + Datenbank.kWahlkreisBundeslandID + ")" }, 
				{ "Parteien.csv", "(3, 1, 2)", datenbank.partei, " (" + Datenbank.kParteiID + ", " + Datenbank.kParteiKuerzel + ", " + Datenbank.kParteiName + ")" }, 
				{ "Kandidaten.csv", "(1, 2, 3, 4, 5, 3, 6)", datenbank.kandidat, " (" + Datenbank.kKandidatNachname + ", " + Datenbank.kKandidatVorname + ", " + Datenbank.kKandidatParteiID + ", " + Datenbank.kKandidatBundeslandID + ", " + Datenbank.kKandidatListenplatz + ", " + Datenbank.kKandidatDMParteiID + ", " + Datenbank.kKandidatDMWahlkreisID + ")" }, 
		};

		for (int i = 0; i < Math.min(4, kImportTripel.length); i++) {
						
			System.out.println("Import: " + kImportTripel[i][kFile]);
			final String datei_pfad = dataFolder + kImportTripel[i][kFile];
			String sql = "IMPORT FROM \"" + datei_pfad + "\" OF DEL MODIFIED BY COLDEL; METHOD P " 
			+ kImportTripel[i][kColumnNumber] + " SKIPCOUNT 1 MESSAGES \"" + messagePfad 
			+ "\" INSERT INTO " + kImportTripel[i][kTableName] + kImportTripel[i][kColumnName];
			System.out.println(sql);
			datenbank.executeDB2(sql);
		}
		System.out.println("Import has been completed.");
	}

	public void importAggregatedVotes(String csvFile, String logFile) {
		try {
			datenbank.truncate(datenbank.stimme);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		final String importErststimmenStmt = "LOAD FROM \"" + csvFile + "\" OF DEL SKIPCOUNT 1 MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 5) SAVECOUNT 10000 " + "MESSAGES \"" + logFile + "\" " 
		+ "INSERT INTO " + datenbank.wahlergebnis1 
		+ " (" + Datenbank.kWahlergebnis1WahlkreisID + ", " + Datenbank.kWahlergebnis1KandidatID + ", " + Datenbank.kStimmeWahlbezirkID + ", " + Datenbank.kStimmeKandidatID + ")";

		System.out.println(importErststimmenStmt);
		datenbank.executeDB2(importErststimmenStmt);
  }
	
  
}

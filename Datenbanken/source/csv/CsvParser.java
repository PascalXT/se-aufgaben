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
	String erstStimmenDatei;
	String zweitStimmenDatei;
	String erstStimmenAggregiertDatei;
	String zweitStimmenAggregiertDatei;

	private Datenbank datenbank;

	public CsvParser(Datenbank datenbank) {
		this.datenbank = datenbank;
	}

	public CsvParser(Datenbank datenbank, String erstStimmenDatei, String zweitStimmenDatei, String erstStimmenAggregiertDatei, String zweitStimmenAggregiertDatei) {
		this.datenbank = datenbank;
		this.erstStimmenDatei = erstStimmenDatei;
		this.zweitStimmenDatei = zweitStimmenDatei;
		this.erstStimmenAggregiertDatei = erstStimmenAggregiertDatei;
		this.zweitStimmenAggregiertDatei = zweitStimmenAggregiertDatei;
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

	private void einzelneStimme(int jahr, int wahlkreisID, int bezirkID, int votedForID, FileWriter fileWriter) {
		String csvLine = jahr + ";" + wahlkreisID + ";" + bezirkID + ";" + votedForID;
		try {
			fileWriter.write(csvLine + "\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void parseVotes(String csvDatei, String bundesland, String messagePfad) throws IOException, SQLException {
		final int kSpalteWahlkreisID = 0;
		final int kSpalteParteiID = 1;
		final int kSpalteErststimmen = 2;
		final int kSpalteZweitstimmen = 3;
		final int kSpalteJahr = 4;

		FileReader fileReader = new FileReader(csvDatei);
		FileWriter fileWriterErststimmen = new FileWriter(erstStimmenDatei);
		FileWriter fileWriterZweitstimmen = new FileWriter(zweitStimmenDatei);
		FileWriter fileWriterErststimmenAggregiert = new FileWriter(erstStimmenAggregiertDatei);
		FileWriter fileWriterZweitstimmenAggregiert = new FileWriter(zweitStimmenAggregiertDatei);
		LineNumberReader line_number_reader = new LineNumberReader(fileReader);
		String next_line = line_number_reader.readLine(); // Skip Header line.
		while ((next_line = line_number_reader.readLine()) != null) {
			String[] tokens = next_line.split(";");
			assert (tokens.length == 5);
			
			final int jahr = Integer.parseInt(tokens[kSpalteJahr]);
			if (jahr == 2009) { // TODO: support 2005 as well
			
				final int wahlkreisID = Integer.parseInt(tokens[kSpalteWahlkreisID]);
				String wahlkreisBundesland = getBundesland(wahlkreisID);

				if (bundesland == null || bundesland.equals(wahlkreisBundesland)) {
					final int parteiId = Integer.parseInt(tokens[kSpalteParteiID]);
					final int anzahlErststimmen = Integer.parseInt("0" + tokens[kSpalteErststimmen]);
					final int anzahlZweitstimmen = Integer.parseInt("0" + tokens[kSpalteZweitstimmen]);
					final int anzahlWahlbezirke = Math.max(anzahlErststimmen, anzahlZweitstimmen) / kMaxBewohnerProBezirk;
					writeErststimmen(jahr, wahlkreisID, parteiId, anzahlErststimmen, anzahlWahlbezirke, fileWriterErststimmen);
					writeZweitstimmen(jahr, wahlkreisID, parteiId, anzahlZweitstimmen, anzahlWahlbezirke, fileWriterZweitstimmen);
					writeErststimmenAggregiert(jahr, wahlkreisID, parteiId, anzahlErststimmen, fileWriterErststimmenAggregiert);
					writeZweitstimmenAggregiert(jahr, wahlkreisID, parteiId, anzahlZweitstimmen, fileWriterZweitstimmenAggregiert);
					fileWriterErststimmen.flush();
					fileWriterZweitstimmen.flush();
					fileWriterErststimmenAggregiert.flush();
					fileWriterZweitstimmenAggregiert.flush();
				}
			}

		}
		fileWriterErststimmen.close();
		fileWriterZweitstimmen.close();
		System.out.println("Erststimmen and Zweitstimmen files have been created.");
	}
	
	private void writeErststimmen(int jahr, int wahlkreisID, int parteiID, int anzahlErststimmen, int anzahlWahlbezirke, FileWriter fileWriter) {
		if (anzahlErststimmen == 0)
			return;
		try {
			final int kandidatID = getKandidat(wahlkreisID, parteiID);
			for (int i = 0; i < anzahlWahlbezirke; i++) {
				int anzahlErststimmenBezirk = kMaxBewohnerProBezirk;
				if (i == anzahlWahlbezirke) {
					anzahlErststimmenBezirk = anzahlErststimmen - kMaxBewohnerProBezirk * (anzahlWahlbezirke - 1);
				}
				System.out.println(wahlkreisID + ";" + i + ";" + kandidatID + ":" + anzahlErststimmenBezirk);
				for (int j = 0; j < anzahlErststimmenBezirk; j++) {
					einzelneStimme(jahr, wahlkreisID, i, kandidatID, fileWriter);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void writeZweitstimmen(int jahr, int wahlkreisID, int parteiID, int anzahlZweitstimmen, int anzahlWahlbezirke, FileWriter fileWriter) {
		if (anzahlZweitstimmen == 0)
			return;
		for (int i = 0; i < anzahlWahlbezirke; i++) {
			int anzahlZweitstimmenBezirk = kMaxBewohnerProBezirk;
			if (i == anzahlWahlbezirke) {
				anzahlZweitstimmenBezirk = anzahlZweitstimmen - kMaxBewohnerProBezirk * (anzahlWahlbezirke - 1);
			}
			System.out.println(wahlkreisID + ";" + i + ";" + parteiID + ":" + anzahlZweitstimmenBezirk);
			for (int j = 0; j < anzahlZweitstimmenBezirk; j++) {
				einzelneStimme(jahr, wahlkreisID, i, parteiID, fileWriter);
			}
		}
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
			datenbank.truncate(datenbank.erststimme);
			datenbank.truncate(datenbank.zweitstimme);
			datenbank.truncate(datenbank.wahlergebnis1);
			datenbank.truncate(datenbank.wahlergebnis2);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		final String importErststimmenStmt = "LOAD FROM \"" + erstStimmenDatei + "\" OF DEL MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 4) SAVECOUNT 10000 " + "MESSAGES \"" + messagePfad + "\" " 
		+ "INSERT INTO " + datenbank.erststimme 
		+ " (" + Datenbank.kErststimmeJahr + ", " + Datenbank.kErststimmeWahlkreisID + ", " + Datenbank.kErststimmeWahlbezirkID + ", " + Datenbank.kErststimmeKandidatID + ")";

		System.out.println(importErststimmenStmt);
		datenbank.executeDB2(importErststimmenStmt);
		System.out.println("Erststimmen have been imported to the database");

		final String importZweitstimmenStmt = "LOAD FROM \"" + zweitStimmenDatei + "\" OF DEL MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 4) SAVECOUNT 10000 " + "MESSAGES \"" + messagePfad + "\" " 
		+ "INSERT INTO " + datenbank.zweitstimme 
		+ " (" + Datenbank.kZweitstimmeJahr + ", " + Datenbank.kZweitstimmeWahlkreisID + ", " + Datenbank.kZweitstimmeWahlbezirkID + ", " + Datenbank.kZweitstimmeParteiID + ")";

		System.out.println(importZweitstimmenStmt);
		datenbank.executeDB2(importZweitstimmenStmt);
		System.out.println("Zweitstimmen have been imported to the database");
		
		final String importErststimmenAggregiertStmt = "LOAD FROM \"" + erstStimmenAggregiertDatei + "\" OF DEL MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 4) SAVECOUNT 10000 " + "MESSAGES \"" + messagePfad + "\" " 
		+ "INSERT INTO " + datenbank.wahlergebnis1 
		+ " (" + Datenbank.kWahlergebnis1Jahr + ", " + Datenbank.kWahlergebnis1WahlkreisID + ", " + Datenbank.kWahlergebnis1Anzahl + ", " + Datenbank.kWahlergebnis1KandidatID + ")";

		System.out.println(importErststimmenAggregiertStmt);
		datenbank.executeDB2(importErststimmenAggregiertStmt);
		System.out.println("ErststimmenAggregiert have been imported to the database");

		final String importZweitstimmenAggregiertStmt = "LOAD FROM \"" + zweitStimmenAggregiertDatei + "\" OF DEL MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 4) SAVECOUNT 10000 " + "MESSAGES \"" + messagePfad + "\" " 
		+ "INSERT INTO " + datenbank.wahlergebnis2 
		+ " (" + Datenbank.kWahlergebnis2Jahr + ", " + Datenbank.kWahlergebnis2WahlkreisID + ", " + Datenbank.kWahlergebnis2Anzahl + ", " + Datenbank.kWahlergebnis2ParteiID + ")";

		System.out.println(importZweitstimmenAggregiertStmt);
		datenbank.executeDB2(importZweitstimmenAggregiertStmt);
		System.out.println("ZweitstimmenAggregiert have been imported to the database");
	}

	public void runImports(String datenordner, String messagePfad) {
		final int kDatei = 0;
		final int kSpaltenNummern = 1;
		final int kTabellenName = 2;
		final int kSpaltenNamen = 3;
		final String[][] kImportTripel = { 
				{ "Bundeslaender.csv", "(1, 2)", datenbank.bundesland, " (" + Datenbank.kBundeslandID + ", " + Datenbank.kBundeslandName + ")" }, 
				{ "Wahlkreise.csv", "(1, 2, 3)", datenbank.wahlkreis, " (" + Datenbank.kWahlkreisID + ", " + Datenbank.kWahlkreisName + ", " + Datenbank.kWahlkreisBundeslandID + ")" }, 
				{ "Parteien.csv", "(3, 1, 2)", datenbank.partei, " (" + Datenbank.kParteiID + ", " + Datenbank.kParteiKuerzel + ", " + Datenbank.kParteiName + ")" }, 
				{ "Kandidaten.csv", "(1, 2, 3, 4, 5, 3, 6)", datenbank.kandidat, " (" + Datenbank.kKandidatNachname + ", " + Datenbank.kKandidatVorname + ", " + Datenbank.kKandidatParteiID + ", " + Datenbank.kKandidatBundeslandID + ", " + Datenbank.kKandidatListenplatz + ", " + Datenbank.kKandidatDMParteiID + ", " + Datenbank.kKandidatDMWahlkreisID + ")" }, 
		};

		for (int i = 0; i < Math.min(4, kImportTripel.length); i++) {
						
			System.out.println("Import: " + kImportTripel[i][kDatei]);
			final String datei_pfad = datenordner + kImportTripel[i][kDatei];
			String sql = "IMPORT FROM \"" + datei_pfad + "\" OF DEL MODIFIED BY COLDEL; METHOD P " 
			+ kImportTripel[i][kSpaltenNummern] + " SKIPCOUNT 1 MESSAGES \"" + messagePfad 
			+ "\" INSERT INTO " + kImportTripel[i][kTabellenName] + kImportTripel[i][kSpaltenNamen];
			System.out.println(sql);
			datenbank.executeDB2(sql);
		}
		System.out.println("Import has been completed.");
	}

	public void importAggregatedVotes(String csvFile, String logFile) {
		try {
			datenbank.truncate(datenbank.erststimme);
			datenbank.truncate(datenbank.zweitstimme);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		final String importErststimmenStmt = "LOAD FROM \"" + csvFile + "\" OF DEL SKIPCOUNT 1 MODIFIED BY COLDEL; " 
		+ "METHOD P (1, 2, 3, 5) SAVECOUNT 10000 " + "MESSAGES \"" + logFile + "\" " 
		+ "INSERT INTO " + datenbank.wahlergebnis1 
		+ " (" + Datenbank.kWahlergebnis1WahlkreisID + ", " + Datenbank.kWahlergebnis1KandidatID + ", " + Datenbank.kErststimmeWahlbezirkID + ", " + Datenbank.kErststimmeKandidatID + ")";

		System.out.println(importErststimmenStmt);
		datenbank.executeDB2(importErststimmenStmt);
  }
	
  
}

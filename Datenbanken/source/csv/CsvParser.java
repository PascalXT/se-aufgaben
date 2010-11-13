package csv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import datenbank.Datenbank;

public class CsvParser {

	final int kMaxBewohnerProBezirk = 2500;
	String erstStimmenDatei;
	String zweitStimmenDatei;

	private Datenbank datenbank;

	public CsvParser(Datenbank datenbank) {
		this.datenbank = datenbank;
	}

	public CsvParser(Datenbank datenbank, String erstStimmenDatei, String zweitStimmenDatei) {
		this.datenbank = datenbank;
		this.erstStimmenDatei = erstStimmenDatei;
		this.zweitStimmenDatei = zweitStimmenDatei;
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
	
	
	
	public void parseVotes(String csvDatei, String bundesland, String messagePfad) throws IOException, SQLException {
		final int kSpalteWahlkreisID = 0;
		final int kSpalteParteiID = 1;
		final int kSpalteErststimmen = 2;
		final int kSpalteZweitstimmen = 3;
		final int kSpalteJahr = 4;

		FileReader fileReader = new FileReader(csvDatei);
		FileWriter fileWriterErststimmen = new FileWriter(erstStimmenDatei);
		FileWriter fileWriterZweitstimmen = new FileWriter(zweitStimmenDatei);
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
					fileWriterErststimmen.flush();
					fileWriterZweitstimmen.flush();
				}
			}

		}
		fileWriterErststimmen.close();
		fileWriterZweitstimmen.close();
		System.out.println("Erststimmen and Zweitstimmen files have been created.");
	}

	public void importVotes(String messagePfad) {
		
		try {
			datenbank.truncate(datenbank.erststimme);
			datenbank.truncate(datenbank.zweitstimme);
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
	
	
	/**
   * speichert alle Erst- und Zweitstimmen aggregiert auf Wahlkreisebene in die Tables Wahlergebnis1 und Wahlergebnis2
   * löscht danach die Tables der Erst- und Zweitstimmen
   */
  public void convertToWahlergebnis() {
  	
  	// truncate 
  	try {
	    datenbank.truncate(datenbank.wahlergebnis1);
	    datenbank.truncate(datenbank.wahlergebnis2);
  	} catch (SQLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
    }
  	
  	// get all wahlkreise, kandidaten and parteien from database
  	List<Integer> wahlkreise = new ArrayList<Integer>();
  	List<Integer> kandidaten = new ArrayList<Integer>();
  	List<Integer> parteien = new ArrayList<Integer>();
  	try {
	    ResultSet rs_wahlkreise = datenbank.executeSQL("SELECT " + Datenbank.kWahlkreisID + " FROM " + datenbank.wahlkreis);
	    while (rs_wahlkreise.next()) 
	    	wahlkreise.add(rs_wahlkreise.getInt(Datenbank.kWahlkreisID));
	    rs_wahlkreise.close();
	    
	    ResultSet rs_kandidaten = datenbank.executeSQL("SELECT " + Datenbank.kKandidatID + " FROM " + datenbank.kandidat);
	    while (rs_kandidaten.next()) 
	    	kandidaten.add(rs_kandidaten.getInt(Datenbank.kKandidatID));
	    rs_kandidaten.close();
	    
	    ResultSet rs_parteien = datenbank.executeSQL("SELECT " + Datenbank.kParteiID + " FROM " + datenbank.partei);
	    while (rs_parteien.next()) 
	    	parteien.add(rs_parteien.getInt(Datenbank.kParteiID));
	    rs_parteien.close();
  	
  	// erststimmen: make an entry for each wahlkreis and kandidat
  	for(int wahlkreis : wahlkreise)
  		for(int kandidat : kandidaten) {
  			String sql = "SELECT * FROM " + datenbank.erststimme + " WHERE " 
  			+ Datenbank.kErststimmeWahlkreisID + "=" + wahlkreis + " AND " 
  			+ Datenbank.kErststimmeKandidatID + "=" + kandidat;  
  			// if there's an error 668, try this: " SET INTEGRITY FOR KORBI."ERSTSTIMME" IMMEDIATE CHECKED ; "
  			ResultSet rs_votes = datenbank.executeSQL(sql);
  			int votes = getSize(rs_votes);
  			int jahr = 2009; // TODO: support JAHR
  			if (votes > 0) {
  				sql = "INSERT INTO " + datenbank.wahlergebnis1 
  				+ " (" + Datenbank.kWahlergebnis1Anzahl + ", " + Datenbank.kWahlergebnis1Jahr + ", " + Datenbank.kWahlergebnis1WahlkreisID + ", " + Datenbank.kWahlergebnis1KandidatID + ") "
  				+ " VALUES (" + votes + ", " + jahr + ", " + wahlkreis + ", " + kandidat + ")";
  			}
  		}
  	
  	// zweitstimmen: make an entry for each wahlkreis and partei
  	for(int wahlkreis : wahlkreise)
  		for(int partei : parteien) {
  			String sql = "SELECT * FROM " + datenbank.zweitstimme + " WHERE " 
  			+ Datenbank.kZweitstimmeWahlkreisID + "=" + wahlkreis + " AND " 
  			+ Datenbank.kZweitstimmeParteiID + "=" + partei;  
  			ResultSet rs_votes = datenbank.executeSQL(sql);
  			int votes = getSize(rs_votes);
  			int jahr = 2009; // TODO: support JAHR
  			if (votes > 0) {
  				sql = "INSERT INTO " + datenbank.wahlergebnis2 
  				+ " (" + Datenbank.kWahlergebnis2Anzahl + ", " + Datenbank.kWahlergebnis2Jahr + ", " + Datenbank.kWahlergebnis2WahlkreisID + ", " + Datenbank.kWahlergebnis2ParteiID + ") "
  				+ " VALUES (" + votes + ", " + jahr + ", " + wahlkreis + ", " + partei + ")";
  			}
  		}
  			
  	} catch (SQLException e) {
	    e.printStackTrace();
	    System.exit(1);
    }
  }
	
  private int getSize(ResultSet rs) throws SQLException {
  	rs.last();
  	return rs.getRow();
  }
  
}

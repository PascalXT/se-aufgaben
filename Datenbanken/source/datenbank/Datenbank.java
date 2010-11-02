package datenbank;

import java.sql.*;

public class Datenbank {
	
  private String datenbank_name = "jdbc:db2:FIRSTDAY";
  private String user = "pascal_db2";
  private String pwd = "Datenbanken";
  private static String kSchemaName = "Pascal";
  
  private Connection connection;
  
  // Tabellen
  public final static String kWahlkreis = tabellenName("Wahlkreis");
  public final static String kWahlkreisID = "WahlkreisID";
  public final static String kWahlkreisName = "WahlkreisName";
  
  public final static String kBundesland = tabellenName("Bundesland");
  public final static String kBundeslandID = "BundeslandID";
  public final static String kBundeslandName = "BundeslandName";
  
  public final static String kPartei = tabellenName("Partei");
  public final static String kParteiID = "ParteiID";
  public final static String kParteiKuerzel = "ParteiKuerzel";
  public final static String kParteiName = "ParteiName";
  
  public final static String kKandidat = tabellenName("Kandidat");
  public final static String kKandidatID = "KandidatID";
  public final static String kNachname = "Nachname";
  public final static String kVorname = "Vorname";
  public final static String kListenplatz = "Listenplatz";
  
  public static String tabellenName(String kurzname) {
    return kSchemaName + "." + kurzname;
  }

  public Datenbank(String[] args) {
    try {
    	if (args.length != 4)
    		throw new Exception("parameter in eclipse hinzufügen!");
    	
    	this.datenbank_name = "jdbc:db2:" + args[0];
    	this.user = args[1];
    	this.pwd = args[2];
    	Datenbank.kSchemaName = args[3];
    	
      Class.forName("com.ibm.db2.jcc.DB2Driver");
      connection = DriverManager.getConnection(datenbank_name, user, pwd);
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (Exception e) {
    	e.printStackTrace();
    }
  }

  public ResultSet executeSQL(String sql_statement) throws SQLException {
    Statement statement = connection.createStatement();
    ResultSet result_set = statement.executeQuery(sql_statement);
    statement.close();
    return result_set;
  }
  
  public void executeUpdate(String sql_statement) throws SQLException {
    Statement statement = connection.createStatement();
    statement.executeUpdate(sql_statement);
    statement.close();
  }
  


  public Connection getConnection() {
    return connection;
  }

}
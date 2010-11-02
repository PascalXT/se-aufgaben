package datenbank;

import java.sql.*;

public class Datenbank {
  private String datenbank_name = "jdbc:db2:FIRSTDAY";
  private String user = "pascal_db2";
  private String pwd = "Datenbanken";
  private String db2_treiber_name = "com.ibm.db2.jcc.DB2Driver";
  private Connection connection;
  public static String kSchemaName = "Pascal";
  
  // Tabellen
  final public static String kWahlkreis = tabellenName("Wahlkreis");
  final public static String kWahlkreisID = "WahlkreisID";
  final public static String kWahlkreisName = "WahlkreisName";
  
  final public static String kBundesland = tabellenName("Bundesland");
  final public static String kBundeslandID = "BundeslandID";
  final public static String kBundeslandName = "BundeslandName";
  
  final public static String kPartei = tabellenName("Partei");
  final public static String kParteiID = "ParteiID";
  final public static String kParteiKuerzel = "ParteiKuerzel";
  final public static String kParteiName = "ParteiName";
  
  final public static String kKandidat = tabellenName("Kandidat");
  final public static String kKandidatID = "KandidatID";
  final public static String kNachname = "Nachname";
  final public static String kVorname = "Vorname";
  final public static String kListenplatz = "Listenplatz";
  
  public static String tabellenName(String kurzname) {
    return kSchemaName + "." + kurzname;
  }

  public Datenbank() {
    try {
      Class.forName(db2_treiber_name);
      connection = DriverManager.getConnection(datenbank_name, user, pwd);
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
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
  
  public void executeDB2()

  public Connection getConnection() {
    return connection;
  }

}
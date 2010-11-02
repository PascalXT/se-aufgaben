package datenbank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class Datenbank {
  private String datenbank_kurzname = "FIRSTDAY";
  private String datenbank_name = "jdbc:db2:" + datenbank_kurzname;
  private String user = "pascal_db2";
  private String db2_command_file = "H:\\db2_commands.txt";
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
  
  public void executeDB2(String db2_statement) {
    File file = new File(db2_command_file);
    if(file.exists()){
        file.delete();
    }
    FileWriter file_writer;
    try {
      file_writer = new FileWriter(file);
      file_writer.write("CONNECT TO " + datenbank_kurzname + ";\n" +
                        db2_statement + ";\nCONNECT RESET;");
      file_writer.flush();
      System.out.println("cmd: " + db2_statement);
      String cmd = "db2cmd.exe -c -w db2 -tvf " + db2_command_file;
      Process p = Runtime.getRuntime().exec(cmd);
      p.waitFor();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public Connection getConnection() {
    return connection;
  }

}
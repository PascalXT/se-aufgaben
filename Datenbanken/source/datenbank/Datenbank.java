package datenbank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class Datenbank {

  private String datenbank_name = "jdbc:db2:FIRSTDAY";
  private String datenbank_kurzname = "FIRSTDAY";
  private String user = "pascal_db2";
  private String db2_command_file = "H:\\db2_commands.txt";
  private String pwd = "Datenbanken";
  public String schemaName;
  
  private Connection connection;
  private Statement statement = null;
  
  // Tabellen
  public String wahlkreis;
  public final static String kWahlkreisID = "ID";
  public final static String kWahlkreisName = "Name";
  public final static String kWahlkreisBundeslandID = "BundeslandID";
  
  public String bundesland;
  public final static String kBundeslandID = "ID";
  public final static String kBundeslandName = "Name";
  
  public String partei;
  public final static String kParteiID = "ID";
  public final static String kParteiKuerzel = "Kuerzel";
  public final static String kParteiName = "Name";
  
  public String kandidat;
  public final static String kKandidatID = "ID";
  public final static String kKandidatParteiID = "ParteiID";
  public final static String kKandidatNachname = "Nachname";
  public final static String kKandidatVorname = "Vorname";
  public final static String kKandidatListenplatz = "Listenplatz";
  public final static String kKandidatBundeslandID = "BundeslandID";
  public final static String kKandidatDMParteiID = "DMParteiID";
  public final static String kKandidatDMWahlkreisID = "DMWahlkreisID";
  
  public String tabellenName(String kurzname) {
    return schemaName + "." + kurzname;
  }

  public Datenbank(String name, String user, String pwd, String schemaName, String commandFile) {
    this.datenbank_kurzname = name;
    this.datenbank_name = "jdbc:db2:" + name;
    this.user = user;
    this.pwd = pwd;
    this.db2_command_file = commandFile;
    this.schemaName = schemaName;
    this.bundesland = tabellenName("Bundesland");
    this.wahlkreis = tabellenName("Wahlkreis");
    this.kandidat = tabellenName("Kandidat");
    this.partei = tabellenName("Partei");
    try {
      Class.forName("com.ibm.db2.jcc.DB2Driver");
      connection = DriverManager.getConnection(datenbank_name, user, pwd);
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public ResultSet executeSQL(String sql_statement) throws SQLException {
    System.out.println(sql_statement);
    if (statement != null) statement.close();
    statement = connection.createStatement();
    ResultSet result_set;
    try {
      result_set = statement.executeQuery(sql_statement + "\n");
    } catch (SQLException e) {
      if (e.getErrorCode() == -551) {
        System.out.println("User " + user + " does not have the necessary " +
        		"priveleges to perform this action. You can change the priveleges" +
        		" using this command: " + 
        		"GRANT  CREATETAB,BINDADD,CONNECT,CREATE_NOT_FENCED_ROUTINE," +
        		"IMPLICIT_SCHEMA,LOAD,CREATE_EXTERNAL_ROUTINE,QUIESCE_CONNECT " +
        		"ON DATABASE  TO USER " + user + ";");
        System.exit(1);
      }
      throw new SQLException(e);
    }
    return result_set;
  }
  
  public void executeUpdate(String sql_statement) throws SQLException {
    System.out.println(sql_statement);
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
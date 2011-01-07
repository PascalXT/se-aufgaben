package benchmark;

public class Karte {
	
  private String query;
  
  public Karte(String query) {
    this.query = query;
  }
  
  public String getQuery() {
  	return query;
  }
  
  public String getUrl() {
    return "http://localhost:8080/WahlWebsite/ShowResult?query=" + query;
  }
}

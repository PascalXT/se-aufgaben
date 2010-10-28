
public class Wahlbewerber {
	
	String vorname;
	String nachname;
	Integer jahrgang;
	String partei;
	Integer wahlkreis;
	String bundesland; // nur belegt bei listenplatz
	Integer listenplatz;

	public Wahlbewerber(String nachname2, String vorname2, String partei2,
			Integer jahrgang, Integer wahlkreis2, String land, Integer listenplatz2) {

		this.nachname = nachname2;
		this.vorname = vorname2;
		this.jahrgang = jahrgang;
		this.partei = partei2;
		this.wahlkreis = wahlkreis2;
		this.bundesland = land;
		this.listenplatz = listenplatz2;
	}
	
	
	/**
	 * Zeile in CSV-Stil ausgeben
	 * @param sep Zellen-Seperator (Semikolon, Komma...)
	 * @return
	 */
	public String toCsv(Character sep, Character stringChar) {
		
		StringBuilder sb= new StringBuilder( stringChar+vorname+stringChar+sep+stringChar+nachname+stringChar+sep+jahrgang+sep);
		if (partei != null)
			sb.append(stringChar+partei+stringChar+sep);
		else
			sb.append(sep.toString());
		
		if (wahlkreis != null)
			sb.append( wahlkreis.toString()+sep);
		else
			sb.append(sep+"");
		
		if (bundesland != null && listenplatz != null)
			sb.append( stringChar+bundesland+stringChar + sep + listenplatz.toString()+sep);
		else
			sb.append(sep.toString()+sep.toString()+"");
		return sb.toString();
	}
	
	/**
	 * Tabellenüberschriften werden hier generiert
	 * @param sep Zellen-Seperator (Semikolon, Komma...)
	 * @return
	 */
	public static String tableHead(Character sep, Character stringChar){
		return stringChar+"Vorname"+stringChar+sep+stringChar+"Nachname"+stringChar+sep+stringChar+"Jahrgang"+stringChar+sep+stringChar+"Partei"+stringChar+sep+stringChar+"Wahlkreis"+stringChar+sep+stringChar+"Bundesland"+stringChar+sep+stringChar+"Listenplatz"+stringChar+sep;
	}
	
}

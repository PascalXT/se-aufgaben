import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// http://www.bundeswahlleiter.de/de/bundestagswahlen/BTW_BUND_09/wahlbewerber/alphabetisch/a.html
// http://www.bundeswahlleiter.de/de/bundestagswahlen/BTW_BUND_09/wahlbewerber/alphabetisch/x.html

public class WahlbewerberParser {

	ArrayList<Wahlbewerber> bewerber;

	public WahlbewerberParser() {
		bewerber = new ArrayList<Wahlbewerber>();
	}

	/**
	 * Öffnet die Webadresse und liefert komplettes HTML als String
	 * @param Webadresse URL
	 * @return
	 * @throws IOException
	 */
	private String openURL(String Webadresse) throws IOException {
		URL url = new URL(Webadresse);
		URLConnection conn = url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn
				.getInputStream(), "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String inputLine;
		while ((inputLine = br.readLine()) != null) {
			sb.append(inputLine);
//			if (!inputLine.endsWith("\\n"))
//				sb.append("\\n");
		}
		br.close();

		return sb.toString();
	}

	/**
	 * HTML-Datei parsen
	 * @param html
	 * @return Gefundene Wahlbewerber
	 */
	private Collection<Wahlbewerber> parseHtml(String html) {
		// <tr>
		// <td class="lb">Zager, Dieter</td>
		// <td class="cen">1938</td>
		// <td class="lb">RRP</td>
		// <td class="cen">
		// <a href="../wahlkreis/l03/bewerber_direkt_28.html">028</a>
		// </td>
		// <td class="lb">
		// <a href="../land/liste_3.html#GRUPPE19LP14"
		// tooltip="linkalert-tip">Niedersachsen (Platz 14 )</a>
		// </td>
		// </tr>

		html = html.replaceAll("&nbsp;", " ");

		ArrayList<Wahlbewerber> liste = new ArrayList<Wahlbewerber>();

		String pattern = "<tr>[\\n ]*<td.*?>(.*?)</td>[\\n ]*<td.*?>(\\d*)</td>[\\n ]*?<td.*?>(.*?)</td>[\\n ]*?<td.*?>[\\n ]*?(.*?)[\\n ]*?</td>[\\n ]*?<td.*?>\\s*?(.*?)\\s*?</td>";
		// [\\n ]*</tr>
		Pattern p = Pattern.compile(pattern);

		Matcher m = p.matcher(html);

		while (m.find()) {
			String nachname = m.group(1).split(",")[0].trim();
			String vorname = m.group(1).split(",")[1].trim();
			int jahrgang;
			jahrgang = Integer.parseInt(m.group(2).trim());

			
			String partei = m.group(3).trim();
			// Parteilose fangen mit K: an
			if (partei.startsWith("K:"))
				partei = null;

			String wahlkreishtml = m.group(4).trim();
			String landeslistehtml = m.group(5).trim();

			// Direktkandidat?
			Integer wahlkreis = null;
			if (wahlkreishtml.length() > 5) {
				Matcher wkreismatch = Pattern.compile("<a.*>.*?(\\d*).*?</a>")
						.matcher(wahlkreishtml);
				wkreismatch.find();
				wahlkreis = Integer.parseInt(wkreismatch.group(1));
			}

			// Listenkandidat?
			String land = null;
			Integer listenplatz = null;

			if (landeslistehtml.length() > 3) {
				Matcher listenplatzmatcher = Pattern.compile(
						"<a.*>(.+?).\\(Platz.(.+?)\\)</a>").matcher(
						landeslistehtml);
				if (listenplatzmatcher.find()) {
					land = listenplatzmatcher.group(1).trim();
					listenplatz = Integer.parseInt(listenplatzmatcher.group(2)
							.trim());
				}
			}

			System.out.println("Nachname: " + nachname + "Vorname: " + vorname
					+ "Partei: " + partei + "Jahrgang: " + jahrgang);
			if (wahlkreis != null)
				System.out.println("   wahlkreis" + wahlkreis);
			if (land != null)
				System.out
						.println("   land: " + land + "platz: " + listenplatz);

			liste.add(new Wahlbewerber(nachname, vorname, partei, jahrgang,
					wahlkreis, land, listenplatz));

			// System.out.println(m.group(1)+" "+m.group(2)+ " "+m.group(3)+
			// " "+m.group(4)+" "+m.group(5)+"\\n");
		}

		return liste;

	}

	/**
	 * Startet den Hauptprozess
	 */
	public void startParsing() {
		char seite = 'a';
		while (seite != 'y') {
			try {
				Collection<Wahlbewerber> vonseite = this
						.parseHtml(this
								.openURL("http://www.bundeswahlleiter.de/de/bundestagswahlen/BTW_BUND_09/wahlbewerber/alphabetisch/"
										+ seite + ".html"));
				System.out.println(vonseite.size()
						+ " Wahlbewerber von der Seite mit Anfangsbuchstaben "
						+ seite);
				this.bewerber.addAll(vonseite);
			} catch (IOException e) {
				e.printStackTrace();
			}
			seite = (char) ((byte) seite + 1);
		}
	}

	/**
	 * Eingelesene Wahlbewerber in CSV-Datei schreiben
	 * 
	 * @param csvFile
	 * @throws IOException
	 */
	public void writeToCsv(File csvFile) throws IOException {

		PrintWriter pw = new PrintWriter(csvFile,"UTF8");
		pw.println(Wahlbewerber.tableHead(new Character(';'), new Character('"')));
		for (Wahlbewerber wb : this.bewerber) {
			pw.println(wb.toCsv(new Character(';'), new Character('"')));
		}
		pw.close();
	}

	public static void main(String[] args) {
		
		System.setProperty("proxyPort","8080");
		System.setProperty("proxyHost","proxy.informatik.tu-muenchen.de");


		System.out.println("STart!");
		WahlbewerberParser parser = new WahlbewerberParser();

		parser.startParsing();
		System.out.println(parser.bewerber.size() + " Bewerber insgesamt");

		File csvFile = new File("wahlbewerber.csv");
		try {
			parser.writeToCsv(csvFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
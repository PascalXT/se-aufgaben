package queries;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class GoogleChart {
	
	
	public enum ChartType {
		PIE("p");
		
		private String code;
		
		private ChartType(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
	}
	
	private String url = "http://chart.apis.google.com/chart";

	public GoogleChart(ChartType type, int width, int height, List<Integer> data, List<String> labels) {
		assert(data.size() == labels.size());
		assert(width < 1000 && height < 1000 && width * height < 3000000);
		url += "?cht=" + type.getCode();
		url += "&chs=" + width + "x" + height;
		String dataParam = "&chd=t:";
		String labelParam = "&chl=";
		for (int i = 0; i < data.size(); i++) {
			dataParam += data.get(i) + ",";
			try {
				labelParam += URLEncoder.encode(labels.get(i), "UTF-8") + "|";
			} catch (UnsupportedEncodingException e) { }
		}
		url += dataParam.substring(0, dataParam.length() - 1);
		url += labelParam.substring(0, labelParam.length() - 1);
		System.out.println("ChartApi URL: " + url);
	}
	
	public String getHtml() {
		return "<img src=\"" + url + "\" alt=\"\"/>";
	}
}

package queries;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class GoogleChart {

	public enum ChartType {
		PIE("p"),
		MAP("map");
		
		private String code;
		
		private ChartType(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
	}
	
	private String url = "http://chart.apis.google.com/chart";
	
	private ChartType type;
	
	private List<Integer> data = null;
	
	private List<String> labels = null;
	
	private List<String> colors = null;
	
	public List<Integer> getData() {
		return data;
	}

	public void setData(List<Integer> data) {
		this.data = data;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public List<String> getColors() {
		return colors;
	}

	public void setColors(List<String> colors) {
		this.colors = colors;
	}

	public GoogleChart(ChartType type, int width, int height) {
		assert(width < 1000 && height < 1000 && width * height < 3000000);
		url += "?cht=" + type.getCode();
		url += "&chs=" + width + "x" + height;
		this.type = type;
	}
	
	public String getHtml() {
		
		if (labels != null) {
			if(type == ChartType.PIE)
				url += "&chl=";
			else if(type == ChartType.MAP)
				url += "&chld=";
			for (int i = 0; i < labels.size(); i++) {
				try {
					url += URLEncoder.encode(labels.get(i), "UTF-8") + "|";
				} catch (UnsupportedEncodingException e) { }
			}
			url = url.substring(0, url.length() - 1);
		}
		
		if (data != null) {
			url += "&chd=t:";
			for (int i = 0; i < data.size(); i++) {
				url += data.get(i) + ",";
			}
			url = url.substring(0, url.length() - 1);
		}
		
		if (colors != null) {
			url += "&chco=";
			for (int i = 0; i < colors.size(); i++) {
				url += colors.get(i) + "|";
			}
			url = url.substring(0, url.length() - 1);
		}
		
		System.out.println("ChartApi URL: " + url);
		return "<img src=\"" + url + "\" alt=\"\"/>";
	}
}

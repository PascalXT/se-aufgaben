package benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BenchmarkResult {
	
	private Map<String, List<Integer>> data;
	
	public BenchmarkResult() {
		data = new HashMap<String, List<Integer>>();
	}
	
	public void addData(String key, Integer value) {
		List<Integer> values = null;
  	if (data.containsKey(key) == false) {
  		values = new ArrayList<Integer>();
  		data.put(key, values);
  	} else {
  		values = data.get(key);
  	}
  	values.add(value);
	}

	
	public List<String> getQueries() {
		List<String> queries = new ArrayList<String>(data.keySet());
		Collections.sort(queries);
		return queries;
	}

	public int getAverageResponseTimeForQuery(String query) {
		List<Integer> values = data.get(query);
		int sum = 0;
		for (Integer value : values)
			sum += value;
		return sum / values.size();
	}
	
}

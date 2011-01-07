package benchmark;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import benchmark.Kartendeck.KartendeckType;

public class Benchmark {
	
	private int numSimulatedBrowsers;
	
	private int sleepTime;
	
	private KartendeckType deckType;
	
	private BenchmarkResult benchmarkResult;
	
	public Benchmark(int numSimulatedBrowsers, int sleepTime, KartendeckType deckType) {
		this.numSimulatedBrowsers = numSimulatedBrowsers;
		this.sleepTime = sleepTime;
		this.deckType = deckType;
		benchmarkResult = new BenchmarkResult();
	}
	
	public ThreadPoolExecutor run() {
		BlockingQueue<Runnable> browsers = new LinkedBlockingQueue<Runnable>();
  	ThreadPoolExecutor texc = new ThreadPoolExecutor(numSimulatedBrowsers, numSimulatedBrowsers, 1000, TimeUnit.MICROSECONDS, browsers);
    
    for (int b = 0; b < numSimulatedBrowsers; b++) 
    	texc.execute(new SimulatedBrowser(this, sleepTime, deckType));
    
    // all browsers are added, request shutdown so awaitTermination() gets called
    // when all tasks are complete
    texc.shutdown();
    
       
    return texc;
	}

  public synchronized void addResponseTime(String query, int responseTime) {
  	benchmarkResult.addData(query, responseTime);
  }
  
  public BenchmarkResult getBenchmarkResult() {
  	return benchmarkResult;
  }
  
}

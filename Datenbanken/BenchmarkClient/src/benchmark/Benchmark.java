package benchmark;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Benchmark {
  public static void runBenchmark(int numSimulatedBrowsers, int iterations, int sleepTime) {
    
  	BlockingQueue<Runnable> browsers = new LinkedBlockingQueue<Runnable>();
    
  	ThreadPoolExecutor texc = new ThreadPoolExecutor(1, numSimulatedBrowsers, 1000, TimeUnit.MICROSECONDS, browsers);
    
  	for (int i = 0; i < iterations; i++)
	    for (int b = 0; b < numSimulatedBrowsers; b++) 
	    	texc.execute(new SimulatedBrowser(sleepTime));
    
    System.out.println("Thread pool has been created.");
  }
}

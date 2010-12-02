package benchmark;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Benchmark {
  public static void runBenchmark(int numSimulatedBrowsers) {
    BlockingQueue<Runnable> browsers = new LinkedBlockingQueue<Runnable>();
    ThreadPoolExecutor t1 = new ThreadPoolExecutor(1, numSimulatedBrowsers, 1000, TimeUnit.MICROSECONDS, browsers);
    for (int i = 0; i < numSimulatedBrowsers; i++) t1.execute(new SimulatedBrowser());
    
    System.out.println("Thread pool has been created.");
  }
}

package main;

import benchmark.Benchmark;

public class Main {

	public static void main(String[] args) {
		
		final int numberOfSimulatedBrowsers = 2;
		final int iterations = 1;
		final int sleepTime = 2000;
		
		Benchmark.runBenchmark(numberOfSimulatedBrowsers, iterations, sleepTime);
	}

}

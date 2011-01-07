package benchmark;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import benchmark.Kartendeck.KartendeckType;

public class SimulatedBrowser implements Runnable { 
	
	private Benchmark benchmark;
	
	private int sleepTime;
	
  Kartendeck<Karte> deck;
  
  public SimulatedBrowser(Benchmark benchmark, int sleepTime, KartendeckType deckType) {
  	this.benchmark = benchmark;
  	this.sleepTime = sleepTime;
    this.deck = Kartendeck.createKartendeckByType(deckType);
  }

  public void sleep() {
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
    }
  }
  
  private int visitPage(String page) {
  	
    System.out.println(Thread.currentThread().getName() + " loads " + page);
    int responseTime = -1;
    try {
    	URL url = new URL(page);
    	final long startLoadingTime = System.currentTimeMillis();
    	new Scanner(url.openStream()).useDelimiter("\\Z").next();
    	responseTime = (int) (System.currentTimeMillis() - startLoadingTime);
    	System.out.println(Thread.currentThread().getName() + " loading complete in " + (responseTime / 1000.0) + " seconds.");

    } catch (MalformedURLException e) {
      e.printStackTrace();
      return -1;
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return responseTime;
  }

  @Override
  public void run() {
    final long benchmarkStartTime = System.currentTimeMillis();
    while (!deck.isEmpty()) {
      final Karte nextKarte = deck.removeRandomKarte();
      final int responseTime = visitPage(nextKarte.getUrl());
      new Thread() {
      	@Override
      	public void run() {
      		benchmark.addResponseTime(nextKarte.getQuery(), responseTime);
      	};
      }.start();
      sleep();
    }
    final long benchmarkStopTime = System.currentTimeMillis();
    System.out.println("client " + Thread.currentThread().getName() + " finished. total time: " + (benchmarkStopTime - benchmarkStartTime));
  }
}

package benchmark;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
      Thread.sleep((int) (sleepTime * ((new Random()).nextDouble() + 0.5)));
    } catch (InterruptedException e) {
    }
  }
  
  int computationTime = -1;
  private int visitPage(String page) {
  	
    System.out.println(Thread.currentThread().getName() + " loads " + page);
    int responseTime = -1;
    try {
    	URL url = new URL(page);
    	final long startLoadingTime = System.currentTimeMillis();
    	String content = (new Scanner(url.openStream()).useDelimiter("\\Z").next());
    	
    	Pattern MY_PATTERN = Pattern.compile("Die Berechnung hat (\\d+) Millisekunden gedauert.");
    	Matcher m = MY_PATTERN.matcher(content);
    	m.find();
    	computationTime = Integer.parseInt(m.group(1));
    	System.out.println(m.group(1));
    	
    	responseTime = (int) (System.currentTimeMillis() - startLoadingTime);
    	System.out.println(Thread.currentThread().getName() + " loading complete in " + (responseTime / 1000.0) + " seconds. Computation time: " + (computationTime / 1000.0) + " seconds.");

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
      		benchmark.addResponseTime(nextKarte.getQuery(), responseTime, computationTime);
      		computationTime = -1;
      	};
      }.start();
      sleep();
    }
    final long benchmarkStopTime = System.currentTimeMillis();
    System.out.println("client " + Thread.currentThread().getName() + " finished. total time: " + (benchmarkStopTime - benchmarkStartTime));
  }
}

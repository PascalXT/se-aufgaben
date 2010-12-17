package benchmark;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class SimulatedBrowser implements Runnable { 
	
	private int sleepTime;
	
  Kartendeck<String> deck;
  
  public SimulatedBrowser(int sleepTime) {
  	this.sleepTime = sleepTime;
    setKartendeck1();
  }
  
  public void setKartendeck1() {
    deck = new Kartendeck<String>();
    deck.addKarte("http://localhost:8080/WahlWebsite/ShowResult?query=Q1", 1);
    deck.addKarte("http://localhost:8080/WahlWebsite/ShowResult?query=Q2", 1);
    deck.addKarte("http://localhost:8080/WahlWebsite/ShowResult?query=Q5", 1);
  }
  
  public void sleep() {
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
    }
  }
  
  public void visitPage(String page) {
  	
    System.out.println(Thread.currentThread().getName() + " loads " + page);
    try {
    	URL url = new URL(page);
    	final long startLoadingTime = System.currentTimeMillis();
    	new Scanner(url.openStream()).useDelimiter("\\Z").next();
    	final long finishLoadingTime = System.currentTimeMillis();
    	System.out.println(Thread.currentThread().getName() + " loading complete in " + ((finishLoadingTime - startLoadingTime) / 1000.0) + " seconds.");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }

  @Override
  public void run() {
    final long benchmarkStartTime = System.currentTimeMillis();
    while (!deck.isEmpty()) {
      final String nextKarte = deck.removeRandomKarte();
      visitPage(nextKarte);
      sleep();
    }
    final long benchmarkStopTime = System.currentTimeMillis();
    System.out.println(Thread.currentThread().getName() + " finished. BenchmarkTime: " + (benchmarkStopTime - benchmarkStartTime));
  }
}

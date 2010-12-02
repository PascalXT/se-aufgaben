package benchmark;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;

import sun.net.www.http.HttpClient;

public class SimulatedBrowser implements Runnable{
  Kartendeck<String> deck;
  
  public SimulatedBrowser() {
    setKartendeck1();
  }
  
  public void setKartendeck1() {
    deck = new Kartendeck<String>();
    deck.addKarte("http://www.google.com", 5);
    deck.addKarte("http://www.amazon.com", 10);
    deck.addKarte("http://www.ebay.com/", 3);
  }
  
  public void sleep7Sec() {
    try {
      wait(7000);
    } catch (InterruptedException e) {
    }
  }
  
  public void visitPage(String page) {
    System.out.println("Visiting page: " + page);
    try {
      HttpClient client = new HttpClient(new URL(page), "", 0);
      LineNumberReader lnr = new LineNumberReader(new InputStreamReader(client.serverInput));
      System.out.println("Want to read a line.");
      System.out.println("First line: " + lnr.readLine());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }

  @Override
  public void run() {
    // TODO: this function does not work properly, yet.
    System.out.println("TODO: this function does not work properly, yet.");
    System.out.println("I ... I am alive!");
    final long benchmarkStartTime = System.currentTimeMillis();
    while (!deck.isEmpty()) {
      final String nextKarte = deck.removeRandomKarte();
      visitPage(nextKarte);
      sleep7Sec();
    }
    final long benchmarkStopTime = System.currentTimeMillis();
    System.out.println("BenchmarkTime: " + (benchmarkStopTime - benchmarkStartTime));
  }
}

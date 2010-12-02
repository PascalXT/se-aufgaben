package benchmark;

import java.util.HashMap;
import java.util.Map;

public class Kartendeck<T> {
  Map<T, Integer> deck;

  public Kartendeck() {
    deck = new HashMap<T, Integer>();
  }

  public int numKarten() {
    int result = 0;
    for (int c : deck.values()) {
      result += c;
    }
    return result;
  }

  public void addKarte(T k, int count) {
    if (deck.containsKey(k)) {
      final int oldCount = deck.get(k);
      final int newCount = oldCount + count;
      deck.put(k, newCount);
    } else {
      deck.put(k, count);
    }
  }

  public boolean isEmpty() {
    return deck.isEmpty();
  }
  
  public T removeRandomKarte() {
    final int numKarten = numKarten();
    int restKarten = (int) (numKarten * Math.random());
    for (T k : deck.keySet()) {
      final int countK = deck.get(k);
      restKarten -= countK;
      if (restKarten < 0) {
        if (countK > 1) {
          deck.put(k, countK - 1);
        } else {
          deck.remove(k);
        }
        return k;
      }
    }
    // Only happens, if deck is empty.
    assert(isEmpty());
    return null;
  }
}

package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;

public class TestInfo extends InfoLine {
   static ArrayList<Integer> spalten=new ArrayList<>();
   public TestInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

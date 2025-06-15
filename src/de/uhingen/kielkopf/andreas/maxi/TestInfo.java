package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;

public class TestInfo extends InfoLine {
   /** gemeinsame Liste der Spaltenbreite */
   static ArrayList<Integer> spalten=new ArrayList<>();
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public TestInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;

public class ConfigInfo extends InfoLine {
   static ArrayList<Integer> spalten=new ArrayList<>();
   public ConfigInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

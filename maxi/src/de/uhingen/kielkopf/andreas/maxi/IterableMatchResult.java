package de.uhingen.kielkopf.andreas.maxi;

import java.util.Iterator;
import java.util.regex.MatchResult;

/**
 * Iterator über Matchresult Ein Match ohne Klammern liefert einen GesamtString Ein Match mit Klammern liefert für jede
 * Klammer einen String, aber keinen Gesamtstring
 * 
 * @author andreas kielkopf
 */
public class IterableMatchResult implements Iterable<String> {
   final MatchResult matchResult;
   final int         start;
   public IterableMatchResult(MatchResult mr) {
      matchResult=mr;
      start=(matchResult.groupCount()==0) ? 0 : 1;
   }
   @Override
   public Iterator<String> iterator() {
      return new Iterator<String>() {
         private int index=start;
         @Override
         public String next() {
            return matchResult.group(index++);
         }
         @Override
         public boolean hasNext() {
            return index<=matchResult.groupCount();
         }
      };
   }
   public String replace(String replacement) {
      if (matchResult.groupCount()==0)
         return replacement.replaceAll("$0", matchResult.group(0));
      String erg=replacement;
      char   nr ='1';
      for (String s:this) {
         String q="§"+nr++;
         erg=erg.replaceAll(q, s);
      }
      return erg;
   }
}

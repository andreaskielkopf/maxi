package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Iterator über Matchresult.
 * 
 * <pre>
 * Ein Match ohne Klammern liefert einen GesamtString 
 * Ein Match mit Klammern liefert für jede Klammer einen String, aber keinen Gesamtstring
 * </pre>
 * 
 * @author Andreas Kielkopf ©2022
 * @version GNU General Public License v3.0
 * @param matchResult
 *           übernommener MatchResult
 * @param start
 *           startpunkt (0 oder 1)
 */
record IterableMatchResult(MatchResult matchResult, int start) implements Iterable<String> {
   /**
    * Übernimmt den MatchResult, und macht ihn iterable
    * 
    * @param mr
    */
   public IterableMatchResult(MatchResult mr) {
      this(mr, (mr.groupCount() == 0) ? 0 : 1);
   }
   /**
    * private implementation von Iterable<String>
    */
   @Override
   public Iterator<String> iterator() {
      return new Iterator<String>() {
         private int index=start;
         @Override
         public boolean hasNext() {
            return index <= matchResult.groupCount();
         }
         @Override
         public String next() {
            return matchResult.group(index++);
         }
      };
   }
   /**
    * Ersetzt alle "§"-Zeichen durch den vorgegebenen Text
    * 
    * @param replacement
    * @return changed text
    */
   public String replaceP(String replacement) {
      if (matchResult.groupCount() == 0)
         return replacement.replaceAll("$0", matchResult.group(0));
      String erg=replacement;
      char nr='1';
      for (final String s:this)
         erg=erg.replaceAll("§" + nr++, s);
      return erg;
   }
   /**
    * Stream mit allen Matches
    * 
    * @return matches
    */
   public Stream<String> stream() {
      final ArrayList<String> erg=new ArrayList<>();
      for (final String s:this)
         erg.add(s);
      return erg.stream();
   }
}

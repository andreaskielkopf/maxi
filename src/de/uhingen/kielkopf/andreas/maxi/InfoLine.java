package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public class InfoLine {
   final static int                                 SHALEN    =64;
   final static int                                 SHASHORT  =4;
   final static String                              UTF_SUM   ="\u2211";
   final static String                              GREEN     ="\u001b[0;32m";
   final static String                              RED       ="\u001b[1;31m";
   final static String                              WHITE     ="\u001b[0;97m";
   final static String                              RESET     ="\u001b[0m";
   final static String                              ANY_ESC   ="\\x1b\\[[0-9;]+m";
   final static String                              TAGD      ="(?:[1-3 ]?[0-9][.] )?";
   final static String                              MONAT     ="[A-Z][a-z][a-z] ";
   final static String                              TAGE      ="(?:[1-3 ]?[0-9] )?";
   final static String                              REST      ="(?:[ 0-9][0-9]{3}[ 0-9]|[0-9:]{5})";
   final static String                              DATE      ="(" + TAGD + MONAT + TAGE + REST + ").*";
   final static String                              SIZE4     ="^([ 0-9KMG,]{4})";
   final static String                              SIZE5     ="^([ 0-9KMG,]{4,5})";
   final static String                              SHA       ="([0-9a-fA-F]{" + SHALEN + "})";
   final static String                              NA        ="--";
   final static List<String>                        MISSING   =Arrays.asList("<missing>", "");
   final static List<String>                        MISSING_V =Arrays.asList("<vmlinuz missing>", "");
   final static List<String>                        MISSING_I =Arrays.asList("<initrd missing>", "");
   final static List<String>                        MISSING_IA=Arrays.asList(NA, "");
   final static List<String>                        MISSING_F =Arrays.asList("fallback", "<no>");
   final static List<String>                        MISSING_FA=Arrays.asList(NA, "");
   final static List<String>                        EOL_TEST  =Arrays.asList("linux51", "51");
   static Map<Predicate<String>, ArrayList<String>> basis     =new LinkedHashMap<>();
   final Iterable<String>                           info;
   @SuppressWarnings("boxing")
   public InfoLine(Iterable<String> iterableInfo, ArrayList<Integer> spalten) {
      info=iterableInfo;
      int index=0;
      for (final String text:iterableInfo) {
         if (index >= spalten.size())
            spalten.add(0);
         if ((text != null) && (text.length() > spalten.get(index)))
            spalten.set(index, text.length());
         index++;
      }
   }
   static void clear() {
      Query.MHWD_LI.clear();
   }
   /**
    * @param lls
    * @param pr
    * @param success
    * @param error
    * @return
    */
   static String deepSearch(List<List<String>> lls, Predicate<String> pr, String success, String error) {
      return lls.stream().flatMap(List::stream).filter(pr).map(s -> success.replaceFirst("§", s)).findAny()
               .orElse(error);
   }
   /** @return a list of kernels to search for */
   static Stream<Entry<Predicate<String>, ArrayList<String>>> getBasisStream() {
      if (basis.isEmpty()) {
         basis=new LinkedHashMap<>();
         final List<List<String>> kernels=Flag.LIST_ALL.get()
                  ? Query.MHWD_L.getLists(Pattern.compile("[*].*(linux(.*))"))
                  : Query.MHWD_LI.getLists(Pattern.compile("[*].*(linux(.*))"));
         final String             r      ="abcdef";
         for (final List<String> k:kernels) {
            final ArrayList<String> info  =k.stream().collect(Collectors.toCollection(ArrayList<String>::new));
            final String            kNr   =info.remove(1);
            // * linux44 * linux515-rt
            String                  search="linuxabcdef$"
                     // initramfs-4.4-x86_64.img initramfs-5.15-rt-x86_64-fallback.img
                     + "|^initr.m.s-a[.]bcdef[-.][^r].*img"
                     // extramodules-4.4-MANJARO extramodules-5.15-rt-MANJAR
                     + "|^.xtr.mo.ul.s-a[.]bcdef-MANJARO$"
                     // cat kver -> 4.4.294-1-MANJARO x64 5.15.5-rt22-1-MANJARO x64
                     + "|^a[.]bc[.][-0-9]*def[-0-9]*MANJARO(?:[ 0-9]+[KM])?"
                     // vmlinuz-4.4-x86_64 vmlinuz-5.15-rt-x86_64
                     + "|^vmlinuz-a[.]bcdef-x86_64$"
                     // initramfs-5.15-x86_64-fallback.img
                     + "|^a[.]bcdef-x86_64-"
            //
            ;
            for (int i=0; i < r.length(); i++)
               search=search.replaceAll("" + r.charAt(i), (i < kNr.length() ? "" + kNr.charAt(i) : ""));
            // search becomes a pattern to identify this one kernel
            basis.put(Pattern.compile(search).asPredicate(), info);
         }
      }
      return basis.entrySet().stream();
   }
   static List<String> insert(List<String> source, String exclude) {
      return Arrays.asList(UTF_SUM, source.stream().filter(s -> !s.isEmpty()).filter(s -> !s.contains(exclude))
               .map(InfoLine::shortSHA).findFirst().orElse("<?>"));
   }
   // public String getInfo() {
   // Iterator<String> it=info.iterator();
   // return (it.hasNext()) ? it.next() : ".";
   // }
   static List<String> select(Stream<List<String>> sl, Predicate<String> pr, List<String> missing) {
      return sl.filter(text -> text.stream().anyMatch(pr)).map(list -> {
         Collections.reverse(list);
         return list;
      }).findAny().orElse(missing);
   }
   static String shortSHA(String sha) {
      return (sha.length() != SHALEN) ? "<sha?>"
               : sha.substring(0, SHASHORT) + "~" + sha.substring(SHALEN - SHASHORT - 1, SHALEN - 1);
   }
   /** @return one Line of the Info */
   public String getLine(Iterator<Integer> len) {
      final StringBuilder sb     =new StringBuilder();
      boolean             noSpace=false;
      for (final String text0:info) {
         final String  text     =(text0 != null) ? text0 : "";
         final boolean separator=((text.equals("=")) || text.equals("|") || text.equals(UTF_SUM));
         final boolean title    =((text.endsWith(":")) || text.startsWith(":"));
         if (Flag.COLOR.get())
            if ((sb.length() == 0) || separator || title)
               sb.append(GREEN);// hervorgehobene Spalte
            else
               if (text.startsWith("<"))
                  sb.append(RED);// Fehler
               else
                  sb.append(WHITE);
         @SuppressWarnings("boxing")
         int width=len.next();
         if ((!noSpace && !separator && (width != 0)))
            sb.append(" ");
         sb.append(text);
         for (; width > text.length(); width--)
            sb.append(" ");
         noSpace=(separator || title);
      }
      while (' ' == sb.charAt(sb.length() - 1))
         sb.deleteCharAt(sb.length() - 1);
      if (Flag.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
}

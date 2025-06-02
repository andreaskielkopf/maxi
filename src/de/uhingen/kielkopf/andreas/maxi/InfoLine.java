package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
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
   static final int                                 SHALEN    =64;
   static final int                                 SHASHORT  =4;
   static final int                                 UUIDLEN   =36;
   static final int                                 UUIDSHORT =4;
   static final String                              UTF_SUM   ="\u2211";                                 // ∑
   static final String                              GREEN     ="\u001b[0;32m";
   static final String                              RED       ="\u001b[1;31m";
   static final String                              WHITE     ="\u001b[0;97m";
   static final String                              RESET     ="\u001b[0m";
   static final String                              ANY_ESC   ="\\x1b\\[[0-9;]+m";
   static final String                              TAGD      ="(?:[1-3 ]?[0-9][.] )?";
   static final String                              MONAT     ="[A-Z][a-z][a-z] ";
   static final String                              TAGE      ="(?:[1-3 ]?[0-9] )?";
   static final String                              REST      ="(?:[ 0-9][0-9]{3}[ 0-9]|[0-9:]{5})";
   static final String                              DATE      ="(" + TAGD + MONAT + TAGE + REST + ").*";
   static final String                              SIZE4     ="^([ 0-9KMGT,]{4})";
   static final String                              SIZE5     ="^([ 0-9KMGT,]{4,5})";
   static final String                              SIZE7     ="([0-9KMGTiB,]{2,7})";
   static final String                              SHA256    ="([0-9a-fA-F]{" + SHALEN + "})";
   static final String                              NA        ="-n/a-";
   static final String                              UUID      ="([-0-9a-f]{36})";
   static final String                              UUIDMIX   ="([-0-9a-f]{36}|[-0-9A-F]{9} {27}| {36})";
   static final List<String>                        MISSING   =Arrays.asList("<missing>", "");
   static final List<String>                        MISSING_V =Arrays.asList("<vmlinuz missing>", "");
   static final List<String>                        MISSING_I =Arrays.asList("<initrd missing>", "");
   static final List<String>                        MISSING_IA=Arrays.asList(NA, "");
   static final List<String>                        MISSING_F =Arrays.asList("fallback", "<no>");
   static final List<String>                        MISSING_FA=Arrays.asList(NA, "");
   static final List<String>                        EOL_TEST  =Arrays.asList("linux51", "51");
   static Map<Predicate<String>, ArrayList<String>> basis     =new LinkedHashMap<>();
   final Iterable<String>                           info;
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
   static void clearQ() {
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
         final List<List<String>> kernels=Maxi.LIST_ALL.get()
                  ? Query.MHWD_L.getLists(Pattern.compile("[*].*(linux(.*))"))
                  : Query.MHWD_LI.getLists(Pattern.compile("[*].*(linux(.*))"));
         final String r="abCdef";
         for (final List<String> k:kernels) {
            final ArrayList<String> info=k.stream().collect(Collectors.toCollection(ArrayList<String>::new));
            final String kNr=info.remove(1);
            // * linux44 * linux515-rt
            String search="linuxabCdef$"
                     // initramfs-4.4-x86_64.img initramfs-5.15-rt-x86_64-fallback.img
                     + "|^initr.m.s-a[.]bCdef[-.][^r].*img"
                     // extramodules-4.4-MANJARO extramodules-5.15-rt-MANJAR
                     + "|^.xtr.mo.ul.s-a[.]bCdef-MANJARO$"
                     // cat kver -> 4.4.294-1-MANJARO x64 5.15.5-rt22-1-MANJARO x64
                     + "|^a[.]bC[.][-0-9rct]*def[-0-9]*MANJARO(?:[ 0-9]+[KM])?"
                     // vmlinuz-4.4-x86_64 vmlinuz-5.15-rt-x86_64
                     + "|^vmlinuz-a[.]bCdef-x86_64$"
                     // initramfs-5.15-x86_64-fallback.img
                     + "|^a[.]bCdef-x86_64-"
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
      return sl.filter(text -> text.stream().anyMatch(pr)).map(fList -> {
         List<String> rList=new ArrayList<String>(fList);
         Collections.reverse(rList);
         return rList;
      }).findAny().orElse(missing);
   }
   static String shortSHA(String sha) {
      return (sha.length() != SHALEN) ? "<sha?>"
               : sha.substring(0, SHASHORT) + "~" + sha.substring(SHALEN - SHASHORT - 1, SHALEN - 1);
   }
   /** Verkürzte UUID für die Ausgabe */
   static String shortUUID(String uuid) {
      return (uuid.length() != UUIDLEN) ? "<uuid?>"
               : uuid.substring(0, UUIDSHORT) + "." + uuid.substring(UUIDLEN - UUIDSHORT - 1, UUIDLEN - 1);
   }
   /**
    * Erzeugt eine Infoline mit Hervorhebungen in Farbe
    * 
    * = | ∑ und der erste block in der Zeile werden grün Texte zwischen \< und \> werden rot Der Rest wird weiß
    * 
    * @return one Line of the Info
    */
   static public String getLine(Iterable<String> blocks, Iterator<Integer> len) {
      final StringBuilder sb=new StringBuilder();
      boolean noSpace=false;
      for (final String block:blocks) {
         final String text=(block != null) ? block : "";
         final boolean separator=((text.equals("=")) || text.equals("|") || text.equals(UTF_SUM));
         final boolean title=((text.endsWith(":")) || text.startsWith(":"));
         if (Maxi.COLOR.get())
            if ((sb.length() == 0) || separator || title)
               sb.append(GREEN);// hervorgehobene Spalte
            else
               if (text.startsWith("<"))
                  sb.append(RED);// Fehler
               else
                  sb.append(WHITE);
         int width=len.next();
         if ((!noSpace && !separator && (width != 0)))
            sb.append(" "/* +": " */);
         sb.append(text);
         for (; width > text.length(); width--)
            sb.append(" ");
         noSpace=(separator || title);
      }
      while (' ' == sb.charAt(sb.length() - 1))
         sb.deleteCharAt(sb.length() - 1);
      if (Maxi.COLOR.get())
         sb.append(RESET);
      return sb.toString();// + "<";
   }
}

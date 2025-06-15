package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Basisclasse für die Ausgabe von strukturierten Informationen in Tabellenform
 * 
 * @author Andreas Kielkopf ©2022
 * 
 *         under GNU General Public License v3.0
 */
public class InfoLine {
   /** {@value} Originale Länge von SHA */
   protected static final int                       SHALEN    =64;
   /** {@value} x2 Verkürzter SHA */
   protected static final int                       SHASHORT  =4;
   /** {@value} Originale Länge von UUID */
   protected static final int                       UUIDLEN   =36;
   /** {@value} x2 Verkürzte UUID */
   protected static final int                       UUIDSHORT =4;
   /** {@value} Summen-Zeichen ∑ */
   protected static final String                    UTF_SUM   ="\u2211";
   /** {@value} ESC-Sequenz */
   protected static final String                    GREEN     ="\u001b[0;32m";
   /** {@value} ESC-Sequenz */
   protected static final String                    RED       ="\u001b[1;31m";
   /** {@value} ESC-Sequenz */
   protected static final String                    WHITE     ="\u001b[0;97m";
   /** {@value} ESC-Sequenz für (Farb-Reset) */
   protected static final String                    RESET     ="\u001b[0m";
   /** {@value} erkenne alle ESC-Sequenzen um sie entfernen zu können */
   protected static final String                    ANY_ESC   ="\\x1b\\[[0-9;]+m";
   /** {@value} erkenne Tag */
   protected static final String                    TAGD      ="(?:[1-3 ]?[0-9][.] )?";
   /** {@value} erkenne Monat */
   protected static final String                    MONAT     ="[A-Z][a-z][a-z] ";
   /** {@value} erkenne Tage */
   protected static final String                    TAGE      ="(?:[1-3 ]?[0-9] )?";
   /** {@value} erkenne Rest des Datums */
   protected static final String                    REST      ="(?:[ 0-9][0-9]{3}[ 0-9]|[0-9:]{5})";
   /** {@value} erkenne Datum */
   protected static final String                    DATE      ="(" + TAGD + MONAT + TAGE + REST + ").*";
   /** {@value} 4 Zeichen mit einer Zahlenangabe */
   protected static final String                    SIZE4     ="^([ 0-9KMGT,]{4})";
   /** {@value} 4-5 Zeichen mit einer Zahlenangabe */
   protected static final String                    SIZE5     ="^([ 0-9KMGT,]{4,5})";
   /** {@value} 2-7 Zeichen mit einer Zahlenangabe */
   protected static final String                    SIZE7     ="([0-9KMGTiB,]{2,7})";
   /** {@value} erkenne SHA256 */
   protected static final String                    SHA256    ="([0-9a-fA-F]{" + SHALEN + "})";
   /** {@value} */
   protected static final String                    NA        ="-n/a-";
   /** {@value} erkenne UUID */
   protected static final String                    UUID      ="([-0-9a-f]{" + UUIDLEN + "})";
   /** {@value} erkenne UUID-mix */
   protected static final String                    UUIDMIX   ="([-0-9a-f]{36}|[-0-9A-F]{9} {27}| {36})";
   /** "&lt;none&gt;" */
   static final List<String>                        MISSING   =Arrays.asList("<none>", "");
   /** "&lt;no vmlinuz&gt;" */
   static final List<String>                        MISSING_V =Arrays.asList("<no vmlinuz>", "");
   /** "&lt;no initrd&gt;" */
   static final List<String>                        MISSING_I =Arrays.asList("<no initrd>", "");
   /** "-n/a-" */
   static final List<String>                        MISSING_IA=Arrays.asList(NA, "");
   /** "fallback","&lt;none&gt;" */
   static final List<String>                        MISSING_F =Arrays.asList("fallback", "<none>");
   /** "-n/a-" */
   static final List<String>                        MISSING_FA=Arrays.asList(NA, "");
   /** "linux51","51" */
   static final List<String>                        EOL_TEST  =Arrays.asList("linux51", "51");
   /** Zwischenspeicher für die Info */
   static Map<Predicate<String>, ArrayList<String>> basis     =new LinkedHashMap<>();
   /** Die Tabelle dieser Zeile */
   protected final Iterable<String>                 info;
   /**
    * Die übergebene Tabelle wird analysiert, und die Länge der Spalten angepasst
    * 
    * @param iterableInfo
    *           Tabelle mit den Texten der einzelnen Spalten
    * @param spalten
    *           Liste mit der Spaltenaufteilung (shared)
    */
   public InfoLine(Iterable<String> iterableInfo, ArrayList<Integer> spalten) {
      info=iterableInfo;
      int index=0;
      for (final String text:info) {
         if (index >= spalten.size())
            spalten.add(0); // Spalte hinzufügen
         if ((text != null) && (text.length() > spalten.get(index)))
            spalten.set(index, text.length()); // Breite der Spalte anpassen
         index++;
      }
   }
   /** Sollen die Farb-ESC-Sequenzen eingefügt werden ? */
   protected static boolean use_color=true;
  
   /** Lösche die Liste der installierten Kernel */
   static void clearQ() {
      Query.MHWD_LI.clear();
   }
   /**
    * 2-dimensionale Tabelle durchsuchen mit dem Suchmuster und textuelle Ergebnisse zurückgeben
    * 
    * @param lls
    *           Tabelle
    * @param pr
    *           Suchmuster
    * @param success
    *           Text bei Erfolg
    * @param error
    *           Text bei Misserfolg
    * @return Ergebnis
    */
   static String deepSearch(List<List<String>> lls, Predicate<String> pr, String success, String error) {
      return lls.stream().flatMap(List::stream).filter(pr).map(s -> success.replaceFirst("§", s)).findAny()
               .orElse(error);
   }
   /**
    * Strom von Informationen zu Kerneln
    * 
    * @return stream mit Kernelinformationen
    */
   static Stream<Entry<Predicate<String>, ArrayList<String>>> getBasisStream() {
      if (basis.isEmpty()) {
         basis=new LinkedHashMap<>();
         final List<List<String>> kernels=Maxi.LIST_ALL.get()
                  ? Query.MHWD_L.getLists(Pattern.compile("[*].*(linux(.*))"))
                  : Query.MHWD_LI.getLists(Pattern.compile("[*].*(linux(.*))"));
         final String r="abCdef";
         // Für alle Kernel
         for (final List<String> k:kernels) {
            /** Infos zu diesem kernel */
            @SuppressWarnings("null")
            final ArrayList<String> info=k.stream().collect(Collectors.toCollection(ArrayList<String>::new));
            /** Kernelnummer */
            final String kNr=info.remove(1);
            /** Erzeuge ein Pattern das diesen kernel erkennen kann */
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
            basis.put(Pattern.compile(search).asPredicate(), info);
         }
      }
      return basis.entrySet().stream();
   }
   /**
    * Ersten Treffer in der Liste (Ausnahmen vorher aus der Liste entfernen)
    * 
    * @param source
    *           liste
    * @param exclude
    *           ausnahme
    * @return
    */
   static List<String> insert(List<String> source, String exclude) {
      return Arrays.asList(UTF_SUM, source.stream().filter(s -> !s.isEmpty()).filter(s -> !s.contains(exclude))
               .map(InfoLine::shortSHA).findFirst().orElse("<?>"));
   }
   // public String getInfo() {
   // Iterator<String> it=info.iterator();
   // return (it.hasNext()) ? it.next() : ".";
   // }
   /**
    * Sucht einen Eintrag in der Liste gemäß Bedingung
    * 
    * @param sl
    *           Tabellenzeile
    * @param pr
    *           Bedingung
    * @param missing
    *           Ersatztexte
    * @return
    */
   static List<String> select(Stream<List<String>> sl, Predicate<String> pr, List<String> missing) {
      return sl.filter(text -> text.stream().anyMatch(pr)).map(fList -> {
         List<String> rList=new ArrayList<String>(fList);
         Collections.reverse(rList);
         return rList;
      }).findAny().orElse(missing);
   }
   /**
    * Verkürzt einen SHA für die Ausgabe
    * 
    * @param sha
    *           langer SHA
    * 
    * @return xxxx~xxxx
    */
   static String shortSHA(String sha) {
      return (sha.length() != SHALEN) ? "<sha?>"
               : sha.substring(0, SHASHORT) + "~" + sha.substring(SHALEN - SHASHORT - 1, SHALEN - 1);
   }
   /**
    * Verkürzte UUID für die Ausgabe
    * 
    * @param uuid
    *           lange UUID
    * 
    * @return XXXX.XXXX
    */
   static String shortUUID(String uuid) {
      return (uuid.length() != UUIDLEN) ? "<uuid?>"
               : uuid.substring(0, UUIDSHORT) + "." + uuid.substring(UUIDLEN - UUIDSHORT - 1, UUIDLEN - 1);
   }
   /**
    * Erzeugt eine Infoline mit Hervorhebungen in Farbe
    * 
    * <pre>
    *    = | ∑ und der erste block in der Zeile werden grün 
    *    Texte zwischen \< und \> werden rot 
    *    Der Rest wird weiß
    * </pre>
    * 
    * @param tabelle
    *           Einzelne Spalten der Zeile
    * @param spalten
    *           Breite der jeweiligen Spalte
    * @return Eine Zeile der Info
    */
   static public String getLine(Iterable<String> tabelle, Iterator<Integer> spalten) {
      final StringBuilder sb=new StringBuilder();
      boolean noSpace=false;
      for (final String block:tabelle) {
         final String text=(block != null) ? block : "";
         final boolean separator=((text.equals("=")) || text.equals("|") || text.equals(UTF_SUM));
         final boolean title=((text.endsWith(":")) || text.startsWith(":"));
         if (use_color)
            if ((sb.length() == 0) || separator || title)
               sb.append(GREEN);// hervorgehobene Spalte
            else
               if (text.startsWith("<"))
                  sb.append(RED);// Fehler
               else
                  sb.append(WHITE);// normaler Text
         int breite=spalten.next();
         if ((!noSpace && !separator && (breite != 0)))
            sb.append(" "/* +": " */);
         sb.append(text);
         for (; breite > text.length(); breite--)
            sb.append(" ");
         noSpace=(separator || title);
      }
      while (' ' == sb.charAt(sb.length() - 1))
         sb.deleteCharAt(sb.length() - 1);
      if (use_color)
         sb.append(RESET);
      return sb.toString();// + "<";
   }
   /**
    * Eine farbige Titelzeile für die Tabelle mit "Info about:"
    * 
    * @param b
    * @return
    */
   static String getHeader(String b) {
      return getHeader("Info about:", b);
   }
   /**
    * Eine farbige Titelzeile für die Tabelle mit 2 Texten in grün und weiß
    * 
    * @param b
    * @return
    */
   static String getHeader(String a, String b) {
      final StringBuilder sb=new StringBuilder();
      if (use_color)
         sb.append(GREEN);
      sb.append(a);
      if (use_color)
         sb.append(WHITE);
      sb.append(b);
      if (use_color)
         sb.append(RESET);
      return sb.toString();
   }
}

package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Listet den Inhalt von mkinitcpioconf
 *
 * <pre>
 * - Warnung wenn die initrdisks erneuert werden müssen
 * - Komprimierter Inhalt von mkinitcpio.conf
 * -
 * </pre>
 *
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public class MkinitcpioInfo extends InfoLine {
   /** {@value} Pfad */
   final static String              MKINITCPIO_ETC    ="/etc/mkinitcpio.conf";
   /** {@value} Warnung */
   final static String              MKINITCPIO_WARNING="Please run 'sudo mkinitcpio -p ";
   /** gemeinsame Liste der Spaltenbreite */
   static ArrayList<Integer>        spalten           =new ArrayList<>();
   /** lokaler Cache */
   private static ArrayList<String> cache;
   /** {@value} Namensteile von bedeutenden VARIABLEN für mkinitcpio */
   static final String              WICHTIG           ="MODULES|HOOKS|COMPRESSION|BINARIES|FILES";
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public MkinitcpioInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /**
    * Info über die Konfiguration von mkinitcpio.
    * 
    * <pre>
    * Ist die {@link  MKINITCPIO_ETC}-Datei neuer als die initramdisks ? Dann empfehle {@link MKINITCPIO_WARNING}
    * </pre>
    * 
    * @return Warnhinweise + Tabelle
    */
   public static Stream<InfoLine> analyseStream() {
      final Stream<List<String>> mkinitcpio=getSelected_C(Pattern //
               .compile("^(#?)((?:" + WICHTIG + ")[A-Z_]*)(=)(.+)"));
      final ArrayList<String[]> tests=new ArrayList<>();
      for (final List<String> list:Query.LS.getLists(Pattern.compile(SIZE4 + ".*(init.*64[.]img)"))) {
         final String b=getBasisStream().filter(e -> e.getKey().test(list.get(1))).map(e -> e.getValue().get(0))
                  .findAny().orElse("linuxXXX");
         final String[] t= {"/boot/" + list.get(1), MKINITCPIO_ETC, MKINITCPIO_WARNING + b + "' :"};
         tests.add(t);
      }
      @SuppressWarnings("null")
      final Stream<TestInfo> testStream=tests.stream().map(Query::test).filter(l -> (l.size() > 1)).map(l -> {
         final ArrayList<String> x=new ArrayList<>(l);
         x.add(1, "<is older than>");
         x.add(0, x.remove(x.size() - 1));
         return new TestInfo(x);
      });
      return Stream.concat(testStream, mkinitcpio.map(ConfigInfo::new));
   }
   /**
    * Titelzeile für {@link MKINITCPIO_ETC}l
    * 
    * @return Titelzeile
    */
   public static String getHeader() {
      return getHeader(MKINITCPIO_ETC);
   }
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
   /**
    * Spezialbehandlung für mkinitcpio
    * 
    * @param pa
    * @return liste
    */
   @Deprecated
   static public List<List<String>> getLists_C(Pattern pa) {
      return getSelected_C(pa).collect(Collectors.toList());// toList();
   }
   /**
    * Spezialbehandlung für mkinitcpio um geteilte Zeilen zusammenzufügen
    * 
    * @param pa
    * @return stream
    */
   synchronized static public Stream<List<String>> getSelected_C(Pattern pa) {
      if (cache == null) { // Filtere Zeilen aus, die nicht abgeschlossen sind
         StringBuilder offen=new StringBuilder();
         cache=new ArrayList<>();
         for (String s:Query.MKINITCPIO.query()) {
            if (offen.length() == 0)
               if (!s.startsWith("#") && s.contains("=(") && !s.contains(")"))
                  offen.append(s); // unvollendete Zeile
               else
                  cache.add(s);
            else {
               offen.append(" ").append(s); // füge sie mit dem Rest der Zeile zusammen
               if (s.contains(")")) { // vollende die Zeile
                  cache.add(offen.toString());
                  offen.setLength(0);
               }
            }
         }
      }
      return cache.stream().map(s -> pa.matcher(s)).filter(Matcher::find).map(IterableMatchResult::new)
               .map(i -> i.stream().collect(Collectors.toList())).collect(Collectors.toList()).stream();
   }
}

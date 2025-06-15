package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Makes some tests on grub and show the results
 *
 * <pre>
 * - test if grub.cfg newer than: initrdisks, /etc/default/grub
 * - show /etc/default/grub and /etc/default/grub.d/*.cfg
 * -
 * </pre>
 *
 * @author Andreas Kielkopf ©2022
 * @version GNU General Public License v3.0
 */
public class GrubInfo extends InfoLine {
   /** {@value} Zeiger auf die aktuell verwendete grub.cfg Datei */
   static final String             GRUB_CFG    ="/boot/grub/grub.cfg";
   /** {@value} Zeiger auf die Konfiguration von Grub in /etc */
   static final String             GRUB_ETC    ="/etc/default/grub";
   /** {@value} Warnhinweis */
   static final String             GRUB_WARNING="Please update grub.cfg:";
   /** gemeinsame Spaltenbreite */
   static final ArrayList<Integer> spalten     =new ArrayList<>();
   /** {@value} Namensteile von bedeutenden VARIABLEN für grub */
   static final String             WICHTIG     ="CMDLINE|DEFAULT|TIMEOUT|DISTRIBUTOR|PRELOAD|_OS_PROBER|THEME";
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public GrubInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /**
    * Info über die Konfiguration von Grub.
    * 
    * <pre>
    * Ist die {@link GRUB_CFG}-Datei neuer als {@link GRUB_ETC} ? Dann empfehle {@link GRUB_WARNING}
    * </pre>
    * 
    * @return Warnhinweise + Tabelle
    */
   public static Stream<InfoLine> analyseStream() {
      /** Teste ob grub.cfg nicht dem neuesten Stand entspricht */
      final ArrayList<String[]> tests=new ArrayList<>(
               Arrays.asList(new String[][] {{GRUB_CFG, GRUB_ETC, GRUB_WARNING}}));
      /** Teste ob irgend eine initrd veraltet ist */
      final List<List<String>> initrd=Query.LS.getLists(Pattern.compile(SIZE4 + ".*(init.*64[.]img)"));
      tests.addAll(initrd.stream().map(l -> new String[] {"/boot/" + l.get(1), GRUB_ETC, GRUB_WARNING})
               .collect(Collectors.toList()));// .toList());
      @SuppressWarnings("null")
      final Stream<TestInfo> testStream=tests.stream().map(Query::test).filter(l -> (l.size() > 1)).map(l -> {
         final ArrayList<String> x=new ArrayList<>(l);
         x.add(1, "<is older than>");
         x.add(0, x.remove(x.size() - 1));
         return new TestInfo(x);
      });
      /** Erzeuge eine Tabelle aus den wichtigen Einträgen in grub.cfg */
      final List<List<String>> grub=Query.GRUB.getLists(Pattern //
               .compile("^(#? *)(GRUB_[A-Z]*(?:" + WICHTIG + ")[A-Z_]*)(=)(.+)"));
      final Stream<ConfigInfo> configStream=grub.stream().map(ConfigInfo::new);
      /** Füge Warnung und Tabelle zusammen */
      return Stream.concat(testStream, configStream);
   }
   /**
    * Titelzeile für Grub-Informationen
    * 
    * @return Titelzeile
    */
   public static String getHeader() {
      return getHeader("/etc/default/grub");
   }
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

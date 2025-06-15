package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Zeigt die EFI-Variable an
 *
 * @author Andreas Kielkopf ©2022
 * @version GNU General Public License v3.0
 */
public class EfiVars extends InfoLine {
   /** gemeinsame Spaltenbreite */
   static ArrayList<Integer> spalten=new ArrayList<>();
   /** Zwischenspeicher für EFI-Tabelle */
   static List<List<String>> efi_var;
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public EfiVars(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /**
    * Tabelle mit EFI-Variablen
    * 
    * @return Tabelle
    */
   public static Stream<InfoLine> analyseStream() {
      return getEfi_VAR()
               .map(l -> l.stream().map(s -> s.replaceFirst(".File.(.+).", "$1")).collect(Collectors.toList()))// .toList())
               .map(EfiVars::new);
   }
   /** {@value} Hex digit */
   static final String HEX      ="[-0-9a-fA-F]";
   /** {@value} */
   static final String BOOT_NR  ="^(Boot" + HEX + "+[*]) ";
   /** {@value} */
   static final String NAME     ="([-:/ a-zA-Z]+)\t";
   /** {@value} */
   static final String PLACE    ="([0-9a-zA-Z]+[(][^)]+[)])";
   /** {@value} */
   static final String FILE_PFAD="(.+File[^)]+[)]|)";        // String pfad
   // ="(?:/File[(]([0-9A-Z.\\]+)[)])|)";
   /** {@value} */
   static final String BGO      ="(..[BG]O|)";
   /** {@value} */
   static final String RESTt    =".*?$";
   /**
    * Tabelle mit möglichen Booteinträgen im UEFI
    * 
    * @return Tabelle
    */
   public static List<List<String>> getBootStanzas() {
      final ConcurrentSkipListMap<String, List<String>> pl=new ConcurrentSkipListMap<String, List<String>>();
      for (List<String> list:EfiInfo.getEfiPartitions())
         pl.put(list.get(0), list);
      List<List<String>> ep=EfiInfo.getEfiPartitions();
      return getEfi_VAR().map(a -> {
         String hd=a.get(2);
         String file=a.get(3);
         a.set(3, file.replaceAll("/File\\((.+)\\)", "$1"));
         for (List<String> epart:ep)
            if (hd.contains(epart.get(0))) {
               a.set(2, epart.get(1));
               a.add(3, InfoLine.shortUUID(epart.get(0)).replace('.', '~'));
            }
         if (a.size() <= 5) {
            a.set(2, "unknown");
            a.add(3, "-");
         }
         return a;
      }).filter(b -> !b.get(2).equals("unknown")).collect(Collectors.toList());
   }
   /**
    * Berechne eine Tabelle mit den EFI-Dateien und versuche zu ermitteln, welchen typ sie haben
    * 
    * @return Liste der EFI-Dateien
    */
   synchronized static Stream<List<String>> getEfi_VAR() {
      if (efi_var == null) /** Pattern um eine Zeile zu zerlegen */
         efi_var=Query.EFI_VAR.getLists(Pattern.compile(BOOT_NR + NAME + PLACE + FILE_PFAD + BGO + RESTt));
      return efi_var.stream();
   }
   /**
    * Titelzeile für EFI-Booteinträge
    * 
    * @return Titelzeile
    */
   public static String getHeader() {
      return getHeader("efi vars (needs efibootmgr)");
   }
   /**
    * Komplette Tabelle
    */
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

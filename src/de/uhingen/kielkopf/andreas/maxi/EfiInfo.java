package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Infos und Tests zu /boot/efi
 *
 * <pre>
 * - Liest die EFI-Dateien, kategorisiert sie und bildet eine Prüfsumme
 * -
 * -
 * </pre>
 *
 * @author Andreas Kielkopf ©2022
 * @version GNU General Public License v3.0
 * 
 */
public class EfiInfo extends InfoLine {
   /** gemeinsame Liste der Spaltenbreite */
   static ArrayList<Integer> spalten=new ArrayList<>();
   /** Zwischenspeicher für EFI-Tabelle */
   static List<List<String>> efi_gr;
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public EfiInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /**
    * Berechne eine Tabelle mit den EFI-Dateien und versuche zu ermitteln, welchen typ sie haben
    * 
    * @return Liste der EFI-Dateien
    */
   public static List<List<String>> getEfi_Gr() {
      if (efi_gr == null)
         efi_gr=Query.GRS_EFI.getLists(
                  Pattern.compile("^*" + SHA256 + " +" + SIZE7 + " +" + "(/[-_a-zA-Z0-9/]+[.]efi)" + " +" + "(.+)"));
      return efi_gr;
   }
   /**
    * Kürzt die SHASUM auf wenige Zeichen
    * 
    * @return Tabelle mit Infos über die EFI-Programme
    */
   public static Stream<InfoLine> analyseStream() {
      for (List<String> list:getEfi_Gr())
         list.set(0, InfoLine.shortSHA(list.get(0)));
      return efi_gr.stream().map(EfiInfo::new);
   }
   /**
    * Titelzeile für EFI-Informationen
    * 
    * @return Titelzeile
    */
   public static String getHeader() {
      return getHeader(" efi bootloaders");
   }
   /**
    * Tabelle mit den erkennbaren Partitionen
    * 
    * @return Tabelle
    */
   public static List<List<String>> getEfiPartitions() {
      return Partition.getPartitions().stream().filter(p -> p.get(6).startsWith("EFI")).collect(Collectors.toList());
   }
   /**
    * Nicht implementiert
    * 
    * @return null
    */
   @Deprecated
   public static Stream<List<String>> getBootLines() {
      return null;
   }
   /**
    * Main um Tests durchzuführen
    * 
    * @param args
    */
   public static void main(String[] args) {
      System.out.println(EfiInfo.getHeader());
      EfiInfo.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(System.out::println);
      System.out.println(EfiVars.getHeader());
      EfiVars.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(System.out::println);
      System.out.println("Erkannte EFI-partitionen:");
      // getEfiPartitions().forEach(System.out::println);
      List<TestInfo> j=EfiVars.getBootStanzas().stream().map(TestInfo::new).collect(Collectors.toList());
      j.forEach(System.out::println);
   }
   /**
    * Komplette Tabelle
    */
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sucht nach allen sichtbaren Partitionen
 *
 * <pre>
 *  b1d3d562-88ff-4ac2-8326-9c5d82892379 sda1      gpt part vfat  953M   EFI System           C579-EF17                           
 *  3ee1dfe1-19af-4102-945d-90d957d3c199 sda2      gpt part btrfs 900G   Linux filesystem     3487ba3d-1cba-4cdc-a043-c420ebca2aca
 *  bdaabdec-4168-4429-8fed-7e0c6ddd0570 sda3      gpt part swap  28,1G  Linux swap           dac5a191-1376-4b6f-9323-f4b2fff4a40b
 * </pre>
 * 
 * @author Andreas Kielkopf ©2022
 * @version GNU General Public License v3.0
 */
public class Partition extends InfoLine {
   /** gemeinsame Liste der Spaltenbreite */
   static ArrayList<Integer> spalten=new ArrayList<>();
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public Partition(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   // static final String FLABEL="([^ ]+)?";
   /** {@value} */
   private static final String KNAME ="([a-z0-9]{3,11})";
   /** {@value} */
   private static final String PTTYPE="([a-z]+)";
   /** {@value} */
   private static final String TYPE  ="(disk|part)";
   /** {@value} */
   private static final String FSTYPE="((?:[a-zA-Z]+)|(?: {5}))";
   /** {@value} */
   private static final String PTNAME="((?:[ a-zA-Z]+?)) +";
   /** Zwischenspeicher für Partitionstabelle */
   static List<List<String>>   partitions;
   /**
    * Tabelle mit den erkennbaren Partitionen
    * 
    * @return tabelle
    */
   public static synchronized List<List<String>> getPartitions() {
      if (partitions == null)
         partitions=Query.LSBLK.getLists(Pattern.compile("^" + UUID + " " + KNAME + " +"//
                  + PTTYPE + " +" + TYPE + " " + FSTYPE + " +" + SIZE7 + " " + PTNAME + UUIDMIX + " *" + "(.+)$"));
      return partitions;
   }
   /**
    * gefilterte Tabelle mit den Infos über die Partitionen
    * 
    * @return tabelle
    */
   public static Stream<InfoLine> analyseStream() {
      return getPartitions().stream().map(l -> {
         if (l.size() > 12) { // BUG
            while (l.size() > 5)
               l.remove(5);
            final String gpt=" -->      " + l.remove(1);
            l.add(1, gpt);
            if (l.get(2) == null) {
               l.remove(4);
               l.remove(3);
            } else
               if (l.get(2).startsWith("EFI ") || l.get(2).startsWith("Linux ")) {
                  l.remove(3);
                  l.remove(1);
               } else
                  if (l.get(2).startsWith("BIOS ")) {
                     l.remove(4);
                     l.remove(1);
                  } else {
                     l.remove(4);
                     l.remove(3);
                  }
         }
         return l;
      }).map(Partition::new);
   }
   /**
    * Titelzeile für Partitionstabelle
    * 
    * @return Titel
    */
   public static String getHeader() {
      return getHeader("visible partitions (needs lsblk)");
   }
   /** Teste ob Partuids doppelt vorhanden sind */
   public static void doubleError() {
      if (partitions != null) {
         ConcurrentHashMap<String, List<String>> l=new ConcurrentHashMap<>();
         StringBuilder sb=new StringBuilder();
         for (List<String> p:partitions) {
            String uuid=p.get(0);
            List<String> err=l.put(uuid, p);// err=p;
            if (err != null) {
               if (!sb.toString().contains(uuid))
                  sb.append("\n").append(err);
               sb.append("\n").append(p);
            }
         }
         if (sb.length() > 0) {
            if (use_color)
               sb.insert(0, WHITE);
            sb.insert(0, "Error: There are partitions with the same UUID");
            if (use_color)
               sb.insert(0, RED);
            System.out.println(sb.toString());
         }
      }
   }
   /**
    * Test für Ausgabe der Partitionen
    * 
    * @param args
    *           leer
    * 
    */
   public static void main(String[] args) {
      System.out.println(Partition.getHeader());
      Partition.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(System.out::println);
      doubleError();
   }
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

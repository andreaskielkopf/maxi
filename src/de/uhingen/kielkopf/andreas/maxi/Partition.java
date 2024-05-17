package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Makes some tests on /boot/efi
 *
 * <pre>
 * - sda        -->      gpt    9a2fddb7-cc8e-43e2-9ea8-3906b11c402d
 * - sda1      EFI System       b1d3d562-88ff-4ac2-8326-9c5d82892379 vfat   C579-EF18
 * - sda2      Linux filesystem 3ee1dfe1-18af-4102-945d-90d957d3c199 btrfs  3487ba3d-1cba-4cdc-a043-c420ebc82aca
 * - sda3      Linux filesystem bda8bdec-4168-4429-8fed-7e0c6ddd0570
 * </pre>
 *
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public class Partition extends InfoLine {
   static ArrayList<Integer> spalten=new ArrayList<>();
   public Partition(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   static final String       FLABEL="([^ ]+)?";
   static final String       KNAME ="([a-z0-9]{3,11})";
   static final String       PTTYPE="([a-z]+)";
   static final String       TYPE  ="(disk|part)";
   static final String       FSTYPE="((?:[a-zA-Z]+)|(?: {5}))";
   static final String       PTNAME="((?:[ a-zA-Z]+?)) +";
   static List<List<String>> partitions;
   public static synchronized List<List<String>> getPartitions() {
      if (partitions == null)
         partitions=Query.LSBLK.getLists(Pattern.compile("^" + UUID + " " + KNAME + " +"//
                  + PTTYPE + " +" + TYPE + " " + FSTYPE + " +" + SIZE7 + " " + PTNAME + UUIDMIX + " *" + "(.+)$"));
      return partitions;
   }
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
   public static String getHeader() {
      final StringBuilder sb=new StringBuilder();
      if (Maxi.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Maxi.COLOR.get())
         sb.append(WHITE);
      sb.append(" visible partitions (needs lsblk)");
      if (Maxi.COLOR.get())
         sb.append(RESET);
      return sb.toString();
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
            if (Maxi.COLOR.get())
               sb.insert(0, WHITE);
            sb.insert(0, "Error: There are partitions with the same UUID");
            if (Maxi.COLOR.get())
               sb.insert(0, RED);
            System.out.println(sb.toString());
         }
      }
   }
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

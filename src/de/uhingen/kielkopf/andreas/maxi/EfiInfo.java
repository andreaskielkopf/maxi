package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Makes some tests on /boot/efi
 *
 * <pre>
 * -
 * -
 * -
 * </pre>
 *
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public class EfiInfo extends InfoLine {
   static ArrayList<Integer> spalten=new ArrayList<>();
   static List<List<String>> efi_gr;
   public EfiInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   public static List<List<String>> getEfi_Gr() {
      if (efi_gr == null)
         efi_gr=Query.GRS_EFI.getLists(
                  Pattern.compile("^*" + SHA256 + " +" + SIZE7 + " +" + "(/[-_a-zA-Z0-9/]+[.]efi)" + " +" + "(.+)"));
      return efi_gr;
   }
   public static Stream<InfoLine> analyseStream() {
      // final List<List<String>> efi_ls =Query.LS_EFI.getLists( //
      // Pattern.compile(SIZE5 + "[^/]*(/[-_a-zA-Z0-9/]+[.]efi)"));
      // final List<List<String>> efi_sha=Query.SHA_EFI
      // .getLists(Pattern.compile("^" + SHA + "[^/]+([-_a-zA-Z0-9/]+[.]efi)"));
      for (List<String> list:getEfi_Gr()) {
         String sha256=list.get(0);
         String shor=InfoLine.shortSHA(sha256);
         list.set(0, shor);
      }
      // return efi_ls.stream().map(fList -> {
      // ArrayList<String>rList=new ArrayList<String>(fList);
      // Collections.reverse(rList);
      // if (Maxi.SHASUM.get()) {
      // final String name=rList.get(0);
      // efi_sha.stream().filter(l -> name.equals(l.get(1))).forEach(l -> {
      // rList.add(UTF_SUM);
      // rList.add(shortSHA(l.get(0)));
      // });
      // }
      // return rList;
      // }).map(EfiInfo::new);
      return efi_gr.stream().map(EfiInfo::new);
   }
   public static String getHeader() {
      final StringBuilder sb=new StringBuilder();
      if (Maxi.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Maxi.COLOR.get())
         sb.append(WHITE);
      sb.append(" efi bootloaders");
      if (Maxi.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   public static List<List<String>> getEfiPartitions() {
      return Partition.getPartitions().stream().filter(p -> p.get(6).startsWith("EFI")).collect(Collectors.toList());
   }
   public static Stream<List<String>> getBootLines() {
      return null;
   }
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
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

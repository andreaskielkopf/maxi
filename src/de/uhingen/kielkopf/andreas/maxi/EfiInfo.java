package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Makes some tests on /boot/efi
 * 
 * <pre>
 * - mkinitcpio.conf newer than: initrdisks, /etc/mkinitcpio.conf 
 * - 
 * -
 * </pre>
 * 
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public class EfiInfo extends InfoLine {
   static String             MKINITCPIO_ETC   ="/etc/mkinitcpio.conf";
   static String             MKINITCPIO_UPDATE="Please update initramdisks:";
   static ArrayList<Integer> spalten          =new ArrayList<>();
   final static List<String> WICHTIG          =Arrays
            .asList(new String[] {"MODULES", "HOOKS", "COMPRESSION", "BINARIES", "FILES"});
   public EfiInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   public static Stream<InfoLine> analyseStream() {
      List<List<String>> efi_ls =Query.LS_EFI.getLists(                                                              //
               Pattern.compile(SIZE5 + "[^/]*(/[-_a-zA-Z0-9/]+[.]efi)"));
      List<List<String>> efi_sha=Query.SHA_EFI.getLists(Pattern.compile("^" + SHA + "[^/]+([-_a-zA-Z0-9/]+[.]efi)"));
      return efi_ls.stream().map(list -> {
         Collections.reverse(list);
         if (Flag.SHASUM.get()) {
            String name=list.get(0);
            efi_sha.stream().filter(l -> name.equals(l.get(1))).forEach(l -> {
               list.add(UTF_SUM);
               list.add(shortSHA(l.get(0)));
            });
         }
         return list;
      }).map(l -> new EfiInfo(l));
   }
   public static String getHeader() {
      StringBuilder sb=new StringBuilder();
      if (Flag.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Flag.COLOR.get())
         sb.append(WHITE);
      sb.append(" efi bootloaders");
      if (Flag.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

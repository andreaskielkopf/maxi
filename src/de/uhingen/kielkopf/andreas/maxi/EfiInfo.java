package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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
   public EfiInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   public static Stream<InfoLine> analyseStream() {
      final List<List<String>> efi_ls =Query.LS_EFI.getLists(                         //
               Pattern.compile(SIZE5 + "[^/]*(/[-_a-zA-Z0-9/]+[.]efi)"));
      final List<List<String>> efi_sha=Query.SHA_EFI
               .getLists(Pattern.compile("^" + SHA + "[^/]+([-_a-zA-Z0-9/]+[.]efi)"));
      return efi_ls.stream().map(list -> {
         Collections.reverse(list);
         if (Flag.SHASUM.get()) {
            final String name=list.get(0);
            efi_sha.stream().filter(l -> name.equals(l.get(1))).forEach(l -> {
               list.add(UTF_SUM);
               list.add(shortSHA(l.get(0)));
            });
         }
         return list;
      }).map(EfiInfo::new);
   }
   public static String getHeader() {
      final StringBuilder sb=new StringBuilder();
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

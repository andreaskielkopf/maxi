package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
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
public class EfiVars extends InfoLine {
   static ArrayList<Integer> spalten=new ArrayList<>();
   public EfiVars(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   public static Stream<InfoLine> analyseStream() {
      final List<List<String>> efi_vars=getEfiVars();
      return efi_vars.stream().map(l -> l.stream().map(s -> s.replaceFirst(".File.(.+).", "$1")).toList())
               .map(EfiVars::new);
   }
   static List<List<String>> getEfiVars() {
      final String  x    ="[-0-9a-fA-F]";
      final String  nr   ="^(Boot" + x + "+[*]) ";
      final String  name ="([-:/ a-zA-Z]+)\t";
      final String  place="([0-9a-zA-Z]+[(][^)]+[)])";
      final String  pfad ="(.+File[^)]+[)]|)";                                   // String pfad
      // ="(?:/File[(]([0-9A-Z.\\]+)[)])|)";
      final String  boo  ="(..[BG]O|)";
      final String  rest =".*?$";
      final Pattern pa   =Pattern.compile(nr + name + place + pfad + boo + rest);
      return Query.EFI_VAR.getLists(pa);
   }
   public static String getHeader() {
      final StringBuilder sb=new StringBuilder();
      if (Maxi.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Maxi.COLOR.get())
         sb.append(WHITE);
      sb.append(" efi vars (needs efibootmgr)");
      if (Maxi.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

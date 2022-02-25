package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
   static String             MKINITCPIO_ETC   ="/etc/mkinitcpio.conf";
   static String             MKINITCPIO_UPDATE="Please update initramdisks:";
   static ArrayList<Integer> spalten          =new ArrayList<>();
   final static List<String> WICHTIG          =Arrays
            .asList(new String[] {"MODULES", "HOOKS", "COMPRESSION", "BINARIES", "FILES"});
   public EfiVars(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   public static Stream<InfoLine> analyseStream() {
      List<List<String>> efi_vars=getEfiVars();
      return efi_vars.stream()
               .map(l -> l.stream().map(s -> s.replaceFirst(".File.(.+).", "$1")).collect(Collectors.toList()))
               .map(l -> new EfiVars(l));
   }
   public static String getHeader() {
      StringBuilder sb=new StringBuilder();
      if (Flag.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Flag.COLOR.get())
         sb.append(WHITE);
      sb.append(" efi vars (needs efibootmgr)");
      if (Flag.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
   static List<List<String>> getEfiVars() {
      String  x    ="[-0-9a-fA-F]";
      String  nr   ="^(Boot" + x + "+[*]) ";
      String  name ="([-:/ a-zA-Z]+)\t";
      String  place="([0-9a-zA-Z]+[(][^)]+[)])";
      String  pfad ="(.+File[^)]+[)]|)";                                   // String pfad
                                                                           // ="(?:/File[(]([0-9A-Z.\\]+)[)])|)";
      String  boo  ="(..[BG]O|)";
      String  rest =".*?$";
      Pattern pa   =Pattern.compile(nr + name + place + pfad + boo + rest);
      return Query.EFI_VAR.getLists(pa);
   }
}

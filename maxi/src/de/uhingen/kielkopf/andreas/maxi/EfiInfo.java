package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Arrays;
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
      List<List<String>>  mkinitcpio=Query.MKINITCPIO
               .getLists(Pattern.compile("^(#?)(" + String.join("|", WICHTIG) + ")(=)(.+)"));
      ArrayList<String[]> tests     =new ArrayList<>();
      List<List<String>>  initrd    =Query.LS.getLists(Pattern.compile(SIZE + ".*(init.*64[.]img)"));
      for (List<String> list:initrd) {
         String[] t=new String[] {"/boot/" + list.get(1), MKINITCPIO_ETC, MKINITCPIO_UPDATE};
         tests.add(t);
      }
      Stream<TestInfo>   testStream  =tests.stream().map(t -> Query.test(t)).filter(l -> (l.size() > 1)).map(l -> {
                                        ArrayList<String> x=new ArrayList<>(l);
                                        x.add(1, "<is older than>");
                                        x.add(0, x.remove(x.size() - 1));
                                        return new TestInfo(x);
                                     });
      Stream<ConfigInfo> configStream=mkinitcpio.stream().map(s -> new ConfigInfo(s));
      return Stream.concat(testStream, configStream);
   }
   public static String getHeader() {
      StringBuilder sb=new StringBuilder();
      if (Flag.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Flag.COLOR.get())
         sb.append(WHITE);
      sb.append(" /etc/default/MKINITCPIO");
      if (Flag.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

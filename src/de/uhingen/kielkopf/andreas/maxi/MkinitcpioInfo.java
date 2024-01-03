package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Makes some tests on mkinitcpio and show the results
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
public class MkinitcpioInfo extends InfoLine {
   static String             MKINITCPIO_ETC   ="/etc/mkinitcpio.conf";
   static String             MKINITCPIO_UPDATE="Please run 'mkinitcpio -p ";
   static ArrayList<Integer> spalten          =new ArrayList<>();
   static final List<String> WICHTIG          =Arrays.asList("MODULES", "HOOKS", "COMPRESSION", "BINARIES", "FILES");
   public MkinitcpioInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   public static Stream<InfoLine> analyseStream() {
      final List<List<String>>  mkinitcpio=Query.MKINITCPIO.getLists_C(Pattern   //
               .compile("^(#?)((?:" + String.join("|", WICHTIG) + ")[A-Z_]*)(=)(.+)"));
      final ArrayList<String[]> tests     =new ArrayList<>();
      for (final List<String> list:Query.LS.getLists(Pattern.compile(SIZE4 + ".*(init.*64[.]img)"))) {
         final String   b=getBasisStream().filter(e -> e.getKey().test(list.get(1))).map(e -> e.getValue().get(0))
                  .findAny().orElse("linuxXXX");
         final String[] t= {"/boot/" + list.get(1), MKINITCPIO_ETC, MKINITCPIO_UPDATE + b + "' :"};
         tests.add(t);
      }
      @SuppressWarnings("null")
      final Stream<TestInfo>   testStream  =tests.stream().map(Query::test).filter(l -> (l.size() > 1)).map(l -> {
                                              final ArrayList<String> x=new ArrayList<>(l);
                                              x.add(1, "<is older than>");
                                              x.add(0, x.remove(x.size() - 1));
                                              return new TestInfo(x);
                                           });
      final Stream<ConfigInfo> configStream=mkinitcpio.stream().map(ConfigInfo::new);
      return Stream.concat(testStream, configStream);
   }
   public static String getHeader() {
      final StringBuilder sb=new StringBuilder();
      if (Maxi.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Maxi.COLOR.get())
         sb.append(WHITE);
      sb.append(" " + MKINITCPIO_ETC);
      if (Maxi.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

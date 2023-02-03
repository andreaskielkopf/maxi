package de.uhingen.kielkopf.andreas.maxi;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Makes some tests on grub and show the results
 *
 * <pre>
 * - grub.cfg newer than: initrdisks, /etc/default/grub
 * -
 * -
 * </pre>
 *
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public class GrubInfo extends InfoLine {
   static String             GRUB_CFG   ="/boot/grub/grub.cfg";
   static String             GRUB_ETC   ="/etc/default/grub";
   static String             GRUB_UPDATE="Please update grub.cfg:";
   static ArrayList<Integer> spalten    =new ArrayList<>();
   final static List<String> WICHTIG    =Arrays.asList("CMDLINE", "DEFAULT", "TIMEOUT", "DISTRIBUTOR", "PRELOAD",
            "_OS_PROBER", "THEME");
   public GrubInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   public static Stream<InfoLine> analyseStream() {
      final List<List<String>>  grub  =Query.GRUB.getLists(Pattern                                      //
               .compile("^(#?)(GRUB_[A-Z]*(?:" + String.join("|", WICHTIG) + ")[A-Z_]*)(=)(.+)"));
      final ArrayList<String[]> tests =new ArrayList<>(
               Arrays.asList(new String[][] {{GRUB_CFG, GRUB_ETC, GRUB_UPDATE}}));
      final List<List<String>>  initrd=Query.LS.getLists(Pattern.compile(SIZE4 + ".*(init.*64[.]img)"));
      tests.addAll(initrd.stream().map(l -> new String[] {"/boot/" + l.get(1), GRUB_ETC, GRUB_UPDATE}).toList());
      @SuppressWarnings("null")
      final Stream<TestInfo>   testStream  =tests.stream().map(Query::test).filter(l -> (l.size() > 1)).map(l -> {
                                              final ArrayList<String> x=new ArrayList<>(l);
                                              x.add(1, "<is older than>");
                                              x.add(0, x.remove(x.size() - 1));
                                              return new TestInfo(x);
                                           });
      final Stream<ConfigInfo> configStream=grub.stream().map(ConfigInfo::new);
      return Stream.concat(testStream, configStream);
   }
   public static String getHeader() {
      final StringBuilder sb=new StringBuilder();
      if (Maxi.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Maxi.COLOR.get())
         sb.append(WHITE);
      sb.append(" /etc/default/grub");
      if (Maxi.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

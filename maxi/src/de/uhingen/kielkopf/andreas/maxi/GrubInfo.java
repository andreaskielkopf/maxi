package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
   final static List<String> WICHTIG    =Arrays
            .asList(new String[] {"DEFAULT", "TIMEOUT", "DISTRIBUTOR", "PRELOAD", "_OS_PROBER", "THEME"});
   public GrubInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   public static Stream<InfoLine> analyseStream() {
      List<List<String>>  grub  =Query.GRUB.getLists(Pattern                                                        //
               .compile("^(#?)(GRUB_[A-Z]*(?:" + String.join("|", WICHTIG) + ")[A-Z_]*)(=)(.+)"));
      ArrayList<String[]> tests =new ArrayList<>(Arrays.asList(new String[][] {{GRUB_CFG, GRUB_ETC, GRUB_UPDATE}}));
      List<List<String>>  initrd=Query.LS.getLists(Pattern.compile(SIZE4 + ".*(init.*64[.]img)"));
      tests.addAll(initrd.stream().map(l -> new String[] {"/boot/" + l.get(1), GRUB_ETC, GRUB_UPDATE})
               .collect(Collectors.toList()));
      Stream<TestInfo>   testStream  =tests.stream().map(t -> Query.test(t)).filter(l -> (l.size() > 1)).map(l -> {
                                        ArrayList<String> x=new ArrayList<>(l);
                                        x.add(1, "<is older than>");
                                        x.add(0, x.remove(x.size() - 1));
                                        return new TestInfo(x);
                                     });
      Stream<ConfigInfo> configStream=grub.stream().map(s -> new ConfigInfo(s));
      return Stream.concat(testStream, configStream);
   }
   public static String getHeader() {
      StringBuilder sb=new StringBuilder();
      if (Flag.COLOR.get())
         sb.append(GREEN);
      sb.append("Info about:");
      if (Flag.COLOR.get())
         sb.append(WHITE);
      sb.append(" /etc/default/grub");
      if (Flag.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

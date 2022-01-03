package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 
 * @author andreas kielkopf
 *
 */
public class ModuleInfo extends InfoLine {
   public static boolean     listExtra=false;
   static ArrayList<Integer> mspalten =new ArrayList<>();
   public ModuleInfo(ArrayList<String> iterableInfo) {
      super(iterableInfo, mspalten);
   }
   public String getLine() {
      return getLine(mspalten.iterator());
   }
   /**
    * Bei mehrfachen analyse müssen diese querys neu gemacht werden
    */
   static public void clear() {
      InfoLine.clear();
      Query.LS.clear();
      Query.CAT_KVER.clear();
      Query.DU_MODULES.clear();
   }
   /**
    * 
    * @return
    */
   public static List<ModuleInfo> analyse() { /// In collect werden die Ergebnisse gesammelt je kernel ein Eintrag
      Map<Predicate<String>, ArrayList<String>> basis    =getBasis();
      List<List<String>>                        kver     =Query.CAT_KVER
               .getList(Pattern.compile("([-0-9.rt]+MANJARO).*"));
      List<List<String>>                        du_module=Query.DU_MODULES
               .getList(Pattern.compile("([0-9]+[KM]?)[^0-9]+/([-0-9rt.]+MANJARO)")/* , "§2 §1" */);
      List<List<String>>                        du_extra =Query.DU_MODULES
               .getList(Pattern.compile("([0-9]+[KM]?)[^0-9]+/(extra.*)"));
      // Für jeden einzelnen kernel untersuchen
      for (Entry<Predicate<String>, ArrayList<String>> entry:basis.entrySet()) {
         Predicate<String> pr  =entry.getKey();
         ArrayList<String> ergs=entry.getValue();
         ergs.add(ergs.remove(0).replace("linux", "modules"));
         //
         String  kernelVersion=deepSearch(kver, pr, "§", "<kver missing>");
         Matcher a            =Pattern.compile("(.*)-rt(.*)-1-(.*)").matcher(kernelVersion);
         if (a.find()) // bugfix wegen rt-kernels
            kernelVersion=new IterableMatchResult(a).replace("§1-1-rt§2-§3");
         Predicate<String> p2=Pattern.compile(kernelVersion, Pattern.LITERAL).asPredicate();
         for (String s:du_module.stream().filter(l -> l.stream().anyMatch(p2)).map(s -> {
            Collections.reverse(s);
            return s;
         }).findAny().orElse(Arrays.asList(new String[] {"<missing>", ""})))
            ergs.add(s);
         //
         for (String s:du_extra.stream().filter(l -> l.stream().anyMatch(pr)).map(s -> {
            Collections.reverse(s);
            return s;
         }).findAny().orElse(Arrays.asList(new String[] {"<missing>", ""})))
            ergs.add(s);
      }
      return basis.values().stream().map(s -> new ModuleInfo(s)).collect(Collectors.toList());
   }
   /**
    * @return
    */
   static public String getHeader() {
      StringBuilder sb=new StringBuilder();
      if (colorize)
         sb.append(green);
      sb.append("Modules in /lib/modules");
      if (colorize)
         sb.append(reset);
      return sb.toString();
   }
}

package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public class ModuleInfo extends InfoLine {
   static ArrayList<Integer> spalten=new ArrayList<>();
   public ModuleInfo(ArrayList<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /**
    * 
    * @return
    */
   public static Stream<ModuleInfo> analyseStream() {
      List<List<String>> kver       =Query.CAT_KVER.getLists(Pattern.compile("([-0-9.rt]+MANJARO).*"));
      List<List<String>> du_module  =Query.DU_MODULES
               .getLists(Pattern.compile("([0-9]+[KM]?)[^0-9]+/([-0-9rt.]+MANJARO)")/* , "§2 §1" */);
      List<List<String>> du_extra   =Query.DU_MODULES.getLists(Pattern.compile("([0-9]+[KM]?)[^0-9]+/(extra.*)"));
      List<List<String>> sha_modules=Flag.SHASUM.get()                                                            //
               ? Query.SHA_MODULES.getLists(Pattern.compile("^.*[/]([0-9.-]+MANJARO) *" + SHA + ".*$"))
               : null;
      List<List<String>> sha_extra  =Flag.SHASUM.get()                                                            //
               ? Query.SHA_MODULES.getLists(Pattern.compile("^.*(extra.+MANJARO) *" + SHA + ".*$"))
               : null;
      // Für jeden einzelnen kernel untersuchen
      return getBasis().entrySet().stream()//
               .map(e -> {
                  Predicate<String> key=e.getKey();
                  ArrayList<String> value=e.getValue();
                  value.add(value.remove(0).replace("linux", "modules"));
                  String kernelVersion=deepSearch(kver, key, "§", "<kver missing>");
                  Matcher ma  =Pattern.compile("(.*)-rt(.*)-1-(.*)").matcher(kernelVersion);
                  if (ma.find()) // bugfix wegen rt-kernels
                     kernelVersion=new IterableMatchResult(ma).replace("§1-1-rt§2-§3");
                  select(du_module.stream(), Pattern.compile(kernelVersion, Pattern.LITERAL).asPredicate(), MISSING)
                           .forEach(s -> value.add(s));
                  select(du_extra.stream(), key, MISSING).forEach(s -> value.add(s));
                  value.add(2, "=");
                  value.add(5, "=");
                  if (sha_modules != null) {
                     value.add(4, UTF_SUM);
                     select(sha_modules.stream(), key, MISSING).forEach(s -> {
                        // System.out.println(s);
                        if (!s.isEmpty() && !s.contains("-"))
                           value.add(5, shortSHA(s));
                     });
                  }
                  if (sha_extra != null) {
                     value.add(9, UTF_SUM);
                     select(sha_extra.stream(), key, MISSING).forEach(s -> {
                        // System.out.println(s);
                        if (!s.isEmpty() && !s.contains("extra"))
                           value.add(10, shortSHA(s));
                     });
                  }
                  return value;
               }).map(s -> new ModuleInfo(s));
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
    * @return
    */
   static public String getHeader() {
      StringBuilder sb=new StringBuilder();
      if (Flag.COLOR.get())
         sb.append(GREEN);
      sb.append("Modules in /lib/modules");
      if (Flag.SHASUM.get()) {
         if (Flag.COLOR.get())
            sb.append(WHITE);
         sb.append(" Checksumming may take a while");
      }
      if (Flag.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

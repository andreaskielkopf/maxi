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
   /** @return */
   @SuppressWarnings("null")
   public static Stream<ModuleInfo> analyseStream() {
      /// Zeige die Kernelversion
      final List<List<String>> kver       =Query.CAT_KVER.getLists(Pattern.compile("([-0-9.rt]+MANJARO).*"));
      final List<List<String>> du_module  =Query.DU_MODULES
               .getLists(Pattern.compile("([0-9]+[KM]?)[^0-9]+/([-0-9rt.]+MANJARO)")/* , "§2 §1" */);
      final List<List<String>> du_extra   =Query.DU_MODULES.getLists(Pattern.compile("([0-9]+[KM]?)[^0-9]+/(extra.*)"));
      final List<List<String>> sha_modules=Maxi.SHASUM.get()
               ? Query.SHA_MODULES.getLists(Pattern.compile("^.*[/]([0-9.-]+MANJARO) *" + SHA256 + ".*$"))
               : null;
      final List<List<String>> sha_extra  =Maxi.SHASUM.get()
               ? Query.SHA_MODULES.getLists(Pattern.compile("^.*(extra.+MANJARO) *" + SHA256 + ".*$"))
               : null;
      // Für jeden einzelnen kernel untersuchen
      return getBasisStream().map(e -> {
         final Predicate<String> key  =e.getKey();
         final ArrayList<String> value=new ArrayList<>(e.getValue());
         value.add(value.remove(0).replace("linux", "modules"));
         String        kernelVersion=deepSearch(kver, key, "§", "<kver missing>");
         final Matcher ma           =Pattern.compile("(.*)-rt(.*)-1-(.*)").matcher(kernelVersion);
         if (ma.find()) // bugfix wegen rt-kernels
            kernelVersion=new IterableMatchResult(ma).replace("§1-1-rt§2-§3");
         select(du_module.stream(), Pattern.compile(kernelVersion, Pattern.LITERAL).asPredicate(), MISSING)
                  .forEach(s -> value.add(s));
         select(du_extra.stream(), key, MISSING).forEach(s -> value.add(s));
         value.add(2, "=");
         value.add(5, "=");
         if (sha_modules != null)
            value.addAll(4, insert(select(sha_modules.stream(), key, MISSING), "-"));
         if (sha_extra != null)
            value.addAll(9, insert(select(sha_extra.stream(), key, MISSING), "extra"));
         return new ModuleInfo(value);
      });
   }
   /** Bei mehrfachen analyse müssen diese querys neu gemacht werden */
   static public void clear() {
      InfoLine.clear();
      Query.LS.clear();
      Query.CAT_KVER.clear();
      Query.DU_MODULES.clear();
   }
   /** @return */
   static public String getHeader() {
      final StringBuilder sb=new StringBuilder();
      if (Maxi.COLOR.get())
         sb.append(GREEN);
      sb.append("Modules in:");
      if (Maxi.COLOR.get())
         sb.append(WHITE);
      sb.append(" /lib/modules");
      if (Maxi.SHASUM.get()) {
         if (Maxi.COLOR.get())
            sb.append(WHITE);
         sb.append(" Checksumming all modules may take a while");
      }
      if (Maxi.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

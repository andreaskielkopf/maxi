package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Informationen über die Kernel-Module
 * 
 * @author Andreas Kielkopf ©2022
 * @version GNU General Public License v3.0
 */
public class ModuleInfo extends InfoLine {
   /** gemeinsame Liste der Spaltenbreite */
   static ArrayList<Integer> spalten=new ArrayList<>();
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public ModuleInfo(ArrayList<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /**
    * Infos über die Module als Stream
    * 
    * @return stream
    */
   @SuppressWarnings("null")
   public static Stream<InfoLine> analyseStream() {
      /// Zeige die Kernelversion
      final List<List<String>> kver=Query.CAT_KVER.getLists(Pattern.compile("([-0-9.rt]+MANJARO).*"));
      final List<List<String>> du_module=Query.DU_MODULES
               .getLists(Pattern.compile("([0-9]+[KM]?)[^0-9]+/([-0-9rt.]+MANJARO)")/* , "§2 §1" */);
      final List<List<String>> du_extra=Query.DU_MODULES.getLists(Pattern.compile("([0-9]+[KM]?)[^0-9]+/(extra.*)"));
      final List<List<String>> sha_modules=Maxi.SHASUM.get()
               ? Query.SHA_MODULES.getLists(Pattern.compile("^.*[/]([0-9.-]+MANJARO) *" + SHA256 + ".*$"))
               : null;
      final List<List<String>> sha_extra=Maxi.SHASUM.get()
               ? Query.SHA_MODULES.getLists(Pattern.compile("^.*(extra.+MANJARO) *" + SHA256 + ".*$"))
               : null;
      // Für jeden einzelnen kernel untersuchen
      return getBasisStream().map(e -> {
         final Predicate<String> key=e.getKey();
         final ArrayList<String> value=new ArrayList<>(e.getValue());
         value.add(value.remove(0).replace("linux", "modules"));
         String kernelVersion=deepSearch(kver, key, "§", "<kver missing>");
         final Matcher ma=Pattern.compile("(.*)-rt(.*)-1-(.*)").matcher(kernelVersion);
         if (ma.find()) // bugfix wegen rt-kernels
            kernelVersion=new IterableMatchResult(ma).replaceP("§1-1-rt§2-§3");
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
   /** Bei mehrfacher Analyse müssen diese Querys jeweils neu gemacht werden */
   static public void clear() {
      Query.MHWD_LI.clear();
      Query.LS.clear();
      Query.CAT_KVER.clear();
      Query.DU_MODULES.clear();
   }
   /**
    * Titelzeile für die Module
    * 
    * @return Titelzeile
    */
   static public String getHeader() {
      if (!Maxi.SHASUM.get())
         return getHeader("/lib/modules");
      final StringBuilder sb=new StringBuilder(getHeader("/lib/modules"));
      if (use_color)
         sb.append(WHITE);
      sb.append(" Checksumming all modules may take a while");
      if (use_color)
         sb.append(RESET);
      return sb.toString();
   }
   /**
    * Testroutine
    * 
    * @param args
    *           von der Commandline
    */
   public static void main(String[] args) {
      System.out.println(ModuleInfo.getHeader());
      ModuleInfo.analyseStream().forEach(System.out::println);
   }
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

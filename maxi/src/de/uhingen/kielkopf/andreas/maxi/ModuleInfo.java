package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author andreas kielkopf
 *
 */
public class ModuleInfo extends InfoLine {
   static ArrayList<Integer> mspalten=new ArrayList<>();
   public ModuleInfo(ArrayList<String> iterableInfo) {
      super(iterableInfo, mspalten);
   }
   public String getLine() {
      return getLine(mspalten.iterator());
   }
   public static List<ModuleInfo> analyse() {
      // String D =DATE;
      /// In collect werden die Ergebnisse gesammelt je kernel ein Eintrag
      /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
      List<List<String>> available=(!KernelInfo.listAll) ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
               : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
      /// Zeige den Kernel und die initramdisks in /boot
      List<List<String>> vmlinuz  =Query.LS.getList(Pattern.compile(DATE+"(vmlinuz.*)"));
      List<List<String>> initrd   =Query.LS.getList(Pattern.compile(DATE+"(init.*64[.]img)"));
      List<List<String>> fallback =Query.LS.getList(Pattern.compile(DATE+"(init.*fallback[.]img)"));
      /// Zeige die Kernelversion
      List<List<String>> kver     =Query.CAT_KVER.getList(Pattern.compile("([-0-9.rt]+MANJARO).*"));
      List<String>       module   =Query.DU_MODULES.getList(Pattern.compile("([0-9]+[KM]?)[^0-9]+([-0-9rt.]+MANJARO)"),
               "§2 §1");
      for (Entry<Predicate<String>, ArrayList<String>> entry:getBasis().entrySet()) {
         Predicate<String> pr  =entry.getKey();
         ArrayList<String> ergs=entry.getValue();
         ergs.add(searchFor(available, pr, "OK", KernelInfo.listAll ? "-" : "<EOL>"));
         ergs.add(searchFor(vmlinuz, pr, "§", "<vmlinuz missing>"));
         ergs.add(searchFor(initrd, pr, "§", "<initrd missing>"));
         ergs.add(searchFor(fallback, pr, "fallback OK", "<no fallback>"));
         ergs.add(searchFor(kver, pr, "§", "<kver missing>"));
         String  kernelVersion=searchFor(kver, pr, "§", "<kver missing>");
         Matcher a            =Pattern.compile("(.*)-rt(.*)-1-(.*)").matcher(kernelVersion);
         if (a.find()) // bugfix wegen rt-kernels
            kernelVersion=new IterableMatchResult(a).replace("§1-1-rt§2-§3");
         Predicate<String> p2=Pattern.compile(kernelVersion, Pattern.LITERAL).asPredicate();
         ergs.add("modules:");
         ArrayList<List<String>> m=new ArrayList<>();
         m.add(module);
         ergs.add(searchFor(m, p2, "§", "<missing>"));
      }
      ArrayList<ModuleInfo> module_list=new ArrayList<>();
      for (Entry<Predicate<String>, ArrayList<String>> moduleInfo:getBasis().entrySet())
         module_list.add(new ModuleInfo(moduleInfo.getValue()));
      return module_list;
   }
}

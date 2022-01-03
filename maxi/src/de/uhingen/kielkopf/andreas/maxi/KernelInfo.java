package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Collects the infos for one kernel and aligns columns
 * 
 * @author andreas kielkopf
 */
public class KernelInfo extends InfoLine {
   public static boolean     listAll =false;
   static ArrayList<Integer> kspalten=new ArrayList<>();
   public KernelInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, kspalten);
   }
   public String getLine() {
      return getLine(kspalten.iterator());
   }
   /**
    * Bei mehrfachen analyse müssen diese querys neu gemacht werden
    */
   static public void clear() {
      InfoLine.clear();
      Query.LS.clear();
      Query.CAT_KVER.clear();
   }
   /**
    * 
    * @return
    */
   static public List<KernelInfo> analyse() { /// In collect werden die Ergebnisse gesammelt je kernel ein Eintrag
      /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
      List<List<String>>                        available=(!listAll)
               ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
               : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
      /// Zeige den Kernel und die initramdisks in /boot
      List<List<String>>                        vmlinuz  =Query.LS.getList(Pattern.compile(DATE+"(vmlinuz.*)"));
      List<List<String>>                        initrd   =Query.LS.getList(Pattern.compile(DATE+"(init.*64[.]img)"));
      List<List<String>>                        fallback =Query.LS
               .getList(Pattern.compile(DATE+"(init.*fallback[.]img)"));
      /// Zeige die Kernelversion
      List<List<String>>                        kver     =Query.CAT_KVER
               .getList(Pattern.compile("([-0-9.rt]+MANJARO).*"));
      Map<Predicate<String>, ArrayList<String>> basis    =getBasis();
      for (Entry<Predicate<String>, ArrayList<String>> entry:basis.entrySet()) {
         Predicate<String> pr  =entry.getKey();
         ArrayList<String> ergs=entry.getValue();
         ergs.add(deepSearch(available, pr, "OK", listAll ? "-" : "<EOL>"));
         ergs.add(deepSearch(vmlinuz, pr, "§", "<vmlinuz missing>"));
         ergs.add(deepSearch(initrd, pr, "§", "<initrd missing>"));
         ergs.add(deepSearch(fallback, pr, "fallback OK", "<no fallback>"));
         ergs.add(deepSearch(kver, pr, "§", "<kver missing>"));
      }
      return basis.values().stream().map(s -> new KernelInfo(s)).collect(Collectors.toList());
   }
   /**
    * 
    * @return
    */
   static public String getHeader() {
      List<List<String>> running=Query.MHWD_LI.getList(Pattern.compile(".*running.*"));
      String             erg    ="";
      for (Iterable<String> imr:running)
         for (String text:imr)
            erg=text;
      if (!colorize)
         erg=(erg.replaceAll(ESC+"\\[[0-9;]+m", ""));
      return erg;
   }
}

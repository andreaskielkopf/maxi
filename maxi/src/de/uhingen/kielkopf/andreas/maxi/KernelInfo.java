package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Collects the infos for one kernel and aligns columns
 * 
 * @author andreas kielkopf
 */
public class KernelInfo extends InfoLine {
   @Override
   public String toString() {
      return getLine(kspalten.iterator());
   }
   public static boolean     listAll =false;
   static ArrayList<Integer> kspalten=new ArrayList<>();
   public KernelInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, kspalten);
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
   static public Stream<KernelInfo> analyseStream() {
      /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
      List<List<String>> available=(!listAll) ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
               : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
      /// Zeige den Kernel und die initramdisks in /boot
      List<List<String>> vmlinuz  =Query.LS.getList(Pattern.compile(SIZE+".*(vmlinuz.*)"));
      List<List<String>> initrd   =Query.LS.getList(Pattern.compile(SIZE+".*(init.*64[.]img)"));
      List<List<String>> fallback =Query.LS.getList(Pattern.compile(SIZE+"[^0-9]+([0-9.]+.+).*(fallback)"));
      /// Zeige die Kernelversion
      List<List<String>> kver     =Query.CAT_KVER.getList(Pattern.compile("([-0-9.rt]+MANJARO).*"));
      return getBasis().entrySet().stream() //
               .map(s -> {
                  Predicate<String> key=s.getKey();
                  ArrayList<String> value=s.getValue();
                  value.add(deepSearch(available, key, "", listAll ? "-" : "<EOL>"));
                  value.add(deepSearch(vmlinuz, key, "§", "<vmlinuz missing>"));
                  // select(vmlinuz.stream(), p, MISSING_V).forEach(v -> l.add(v));
                  select(initrd.stream(), key, MISSING_I).forEach(t -> value.add(t.trim()));
                  select(fallback.stream(), key, MISSING_F).forEach(t -> {
                     if (!t.contains("x"))
                        value.add(t.trim());
                  });
                  value.add(deepSearch(kver, key, "§", "<kver missing>"));
                  value.add(4, "=");
                  value.add(7, "=");
                  value.add(9, "kver:");
                  return value;
               }).map(s -> new KernelInfo(s));
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
      return erg.replaceAll("\\x1b\\[[0-9;]+m", colorize ? WHITE : "").replaceFirst("\\x1b\\[[0-9;]+m",
               colorize ? GREEN : "");
   }
}

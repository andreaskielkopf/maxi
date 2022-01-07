package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Collects the infos for one kernel and aligns columns
 * 
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public class KernelInfo extends InfoLine {
   // public static boolean listAll =false;
   static ArrayList<Integer> spalten=new ArrayList<>();
   public KernelInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /**
    * 
    * @return
    */
   static public Stream<KernelInfo> analyseStream() {
      /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
      List<List<String>> available   =(!Flag.LIST_ALL.get())                                                      //
               ? Query.MHWD_L.getLists(Pattern.compile("[*].*(linux(.*))"))
               : Query.MHWD_LI.getLists(Pattern.compile("[*].*(linux(.*))"));
      /// Zeige den Kernel und die initramdisks in /boot
      List<List<String>> vmlinuz     =Query.LS.getLists(Pattern.compile(SIZE + ".*(vmlinuz.*)"));
      List<List<String>> sha_kernel  =Flag.SHASUM.get()
               ? Query.SHA_BOOT.getLists(Pattern.compile("^" + SHA + ".*(vmlinuz.*)"))
               : null;
      List<List<String>> initrd      =Query.LS.getLists(Pattern.compile(SIZE + ".*(init.*64[.]img)"));
      List<List<String>> fallback    =Query.LS.getLists(Pattern.compile(SIZE + "[^0-9]+([0-9.]+.+).*(fallback)"));
      List<List<String>> sha_fallback=Flag.SHASUM.get()
               ? Query.SHA_BOOT.getLists(Pattern.compile("^" + SHA + ".*(init.*back.*)"))
               : null;
      /// Zeige die Kernelversion
      List<List<String>> kver        =Query.CAT_KVER.getLists(Pattern.compile("([-0-9.rt]+MANJARO).*"));
      return getBasis().entrySet().stream().map(e -> {
         Predicate<String> key  =e.getKey();
         ArrayList<String> value=e.getValue();
         value.add(deepSearch(available, key, "", Flag.LIST_ALL.get() ? "-" : "<EOL>"));
         value.add(deepSearch(vmlinuz, key, "§", "<vmlinuz missing>"));
         select(initrd.stream(), key, MISSING_I).forEach(t -> value.add(t.trim()));
         select(fallback.stream(), key, MISSING_F).forEach(t -> {
            if (!t.contains("x"))
               value.add(t.trim());
         });
         value.add(4, "=");
         value.add(7, "=");
         if (Flag.KVER.get()) {
            value.add(deepSearch(kver, key, "§", "<kver missing>"));
            value.add(9, "kver:");
         }
         if (sha_kernel != null) {
            value.add(3, UTF_SUM);
            select(sha_kernel.stream(), key, MISSING).forEach(s -> {
               if (!s.contains("vmlinuz"))
                  value.add(4, shortSHA(s));
            });
         }
         if (sha_fallback != null) {
            value.add(11, UTF_SUM);
            select(sha_fallback.stream(), key, MISSING).forEach(s -> {
               // System.out.println(s);
               if (!s.isEmpty() && !s.contains("fallback"))
                  value.add(12, shortSHA(s));
            });
         }
         return value;
      }).map(s -> new KernelInfo(s));
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
   static public String getHeader() {
      List<List<String>> running=Query.MHWD_LI.getLists(Pattern.compile(".*running.*"));
      String             erg    ="";
      for (Iterable<String> imr:running)
         for (String text:imr)
            erg=text;
      return erg.replaceAll("\\x1b\\[[0-9;]+m", Flag.COLOR.get() ? WHITE : "").replaceFirst("\\x1b\\[[0-9;]+m",
               Flag.COLOR.get() ? GREEN : "");
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

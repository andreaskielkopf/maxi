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
   static ArrayList<Integer> spalten=new ArrayList<>();
   public KernelInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /** @return */
   static public Stream<KernelInfo> analyseStream() {
      /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
      final List<List<String>> available   =!Flag.LIST_ALL.get()                                             //
               ? Query.MHWD_L.getLists(Pattern.compile("[*].*(linux(.*))"))
               : Query.MHWD_LI.getLists(Pattern.compile("[*].*(linux(.*))"));
      /// Zeige den Kernel und die initramdisks in /boot
      final List<List<String>> vmlinuz     =Query.LS.getLists(Pattern.compile(SIZE4 + ".*(vmlinuz.*)"));
      final List<List<String>> initrd      =Query.LS.getLists(Pattern.compile(SIZE4 + ".*(init.*64[.]img)"));
      final List<List<String>> fallback    =Query.LS
               .getLists(Pattern.compile(SIZE4 + "[^0-9]+([0-9.]+.+).*(fallback)"));
      /// Zeige die Kernelversion
      final List<List<String>> kver        =Flag.KVER.get()
               ? Query.CAT_KVER.getLists(Pattern.compile("([-0-9.rt]+MANJARO).*"))
               : null;
      /// berechne die Prüfsummen
      final List<List<String>> sha_kernel  =Flag.SHASUM.get()
               ? Query.SHA_BOOT.getLists(Pattern.compile("^" + SHA + ".*(vmlinuz.*)"))
               : null;
      final List<List<String>> sha_fallback=Flag.SHASUM.get()
               ? Query.SHA_BOOT.getLists(Pattern.compile("^" + SHA + ".*(init.*back.*)"))
               : null;
      return getBasisStream().map(e -> {
         final Predicate<String> key =e.getKey();
         final ArrayList<String> list=new ArrayList<>(e.getValue());
         list.add(deepSearch(available, key, "", Flag.LIST_ALL.get() ? NA : "<EOL>"));
         final boolean notLocal=list.contains(NA);
         list.add(deepSearch(vmlinuz, key, "§", notLocal ? NA : "<vmlinuz missing>"));
         select(initrd.stream(), key, notLocal ? MISSING_IA : MISSING_I).forEach(t -> list.add(t.trim()));
         select(fallback.stream(), key, notLocal ? MISSING_FA : MISSING_F).forEach(t -> {
            if (!t.contains("x"))
               list.add(t.trim());
         });
         list.add(4, "=");
         list.add(7, "=");
         if (kver != null) {
            list.add(deepSearch(kver, key, "§", "<kver missing>"));
            list.add(9, "kver:");
         }
         if (sha_kernel != null)
            list.addAll(3, insert(select(sha_kernel.stream(), key, MISSING), "vmlinuz"));
         if (sha_fallback != null)
            list.addAll(11, insert(select(sha_fallback.stream(), key, MISSING), "fallback"));
         return new KernelInfo(list);
      });
   }
   /** Bei mehrfachen analyse müssen diese querys neu gemacht werden */
   static public void clear() {
      InfoLine.clear(); // MHWD_LI
      Query.LS.clear();
      Query.CAT_KVER.clear();
   }
   /** @return die Meldung vom MHWD übernehmen */
   static public String getHeader() {
      // teste ob wir in chroot laufen
      final List<List<String>> mounts=Query.CHROOT.getLists(Pattern.compile(" /.* / .*"));
      final String             chroot=(mounts.isEmpty()) ? "running in CHROOT" : "running";
      return Query.MHWD_LI.getLists(Pattern.compile(".*running.*")).stream().flatMap(List::stream).findAny().orElse("")
               .replaceFirst("running", chroot).replaceAll(ANY_ESC, Flag.COLOR.get() ? WHITE : "")
               .replaceFirst(ANY_ESC, Flag.COLOR.get() ? GREEN : "");
   }
   @Override
   public String toString() {
      return getLine(spalten.iterator());
   }
}

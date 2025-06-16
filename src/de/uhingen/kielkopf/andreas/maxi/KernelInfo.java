package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Sammelt Informationen über die Kernel
 *
 * @author Andreas Kielkopf ©2022
 * @version GNU General Public License v3.0
 */
public class KernelInfo extends InfoLine {
   /** gemeinsame Liste der Spaltenbreite */
   static ArrayList<Integer> spalten=new ArrayList<>();
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public KernelInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   /**
    * Stream mit infos zu allen kerneln
    * 
    * @return stream
    */
   @SuppressWarnings("null")
   static public Stream<KernelInfo> analyseStream() {
      /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
      final List<List<String>> available=!Maxi.LIST_ALL.get() //
               ? Query.MHWD_L.getLists(Pattern.compile("[*].*(linux(.*))"))
               : Query.MHWD_LI.getLists(Pattern.compile("[*].*(linux(.*))"));
      /// Zeige den Kernel und die initramdisks in /boot
      final List<List<String>> vmlinuz=Query.LS.getLists(Pattern.compile(SIZE4 + ".*(vmlinuz.*)"));
      final List<List<String>> initrd=Query.LS.getLists(Pattern.compile(SIZE4 + ".*(init.*64[.]img)"));
      final List<List<String>> fallback=Query.LS.getLists(Pattern.compile(SIZE4 + "[^0-9]+([0-9.]+.+).*(fallback)"));
      /// Zeige die Kernelversion
      final List<List<String>> kver=Maxi.KVER.get() ? Query.CAT_KVER.getLists(Pattern.compile("([-0-9.rtc]+MANJARO).*"))
               : null;
      /// berechne die Prüfsummen
      final List<List<String>> sha_kernel=Maxi.SHASUM.get()
               ? Query.SHA_BOOT.getLists(Pattern.compile("^" + SHA256 + ".*(vmlinuz.*)"))
               : null;
      final List<List<String>> sha_fallback=Maxi.SHASUM.get()
               ? Query.SHA_BOOT.getLists(Pattern.compile("^" + SHA256 + ".*(init.*back.*)"))
               : null;
      return getBasisStream().map(e -> {
         final Predicate<String> key=e.getKey();
         final ArrayList<String> list=new ArrayList<>(e.getValue());
         list.add(deepSearch(available, key, "", Maxi.LIST_ALL.get() ? NA : "<EOL>"));
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
   /** Bei mehrfacher Analyse müssen diese Querys jeweils neu gemacht werden */
   static public void clear() {
      Query.MHWD_LI.clear();
      Query.LS.clear();
      Query.CAT_KVER.clear();
   }
   /**
    * Titel für die Kernel
    * 
    * @return Titel
    */
   static public String getHeader() {
      // teste ob wir in chroot laufen
      final List<List<String>> mounts=Query.CHROOT.getLists(Pattern.compile(" /.* / .*"));
      final String chroot=(mounts.isEmpty()) ? "running in CHROOT" : "running";
      return Query.MHWD_LI.getLists(Pattern.compile(".*running.*")).stream().flatMap(List::stream).findAny().orElse("")
               .replaceFirst("running", chroot).replaceAll(ANY_ESC, use_color ? WHITE : "")
               .replaceFirst(ANY_ESC, use_color ? GREEN : "");
   }
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

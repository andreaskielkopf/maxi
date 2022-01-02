/**
 * 
 */
package de.uhingen.kielkopf.andreas.maxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.uhingen.kielkopf.andreas.maxi.Maxi.Query.IterableMatchResult;

/**
 * @author andreas
 *
 */
public class Maxi {
   final static char           ESC       =27;
   final static String         green     =ESC+"[32m";
   final static String         red       =ESC+"[1;31m";
   final static String         white     =ESC+"[0;97m";
   final static String         reset     =ESC+"[0m";
   final static String         TAGD      ="(?:[1-3 ]?[0-9][.] )?";
   final static String         MONAT     ="[A-Z][a-z][a-z] ";
   final static String         TAGE      ="(?:[1-3 ]?[0-9] )?";
   final static String         REST      ="(?:[ 0-9][0-9]{3}[ 0-9]|[0-9:]{5})";
   final static String         DATE      ="("+TAGD+MONAT+TAGE+REST+").*";
   final static ProcessBuilder pb        =new ProcessBuilder();
   static List<KernelInfo>     k_aktuell =null;
   static List<KernelInfo>     last      =null;
   static List<ModuleInfo>     m_aktuell =null;
   final public static String  SHELL     =System.getenv("SHELL");
   final public static boolean zsh       =SHELL.contains("zsh");
   static boolean              listAll   =false;
   static boolean              listOnExit=false;
   static boolean              colorize  =false;
   /**
    * @param args
    */
   public static void main(String[] args) {
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
         @Override
         public void run() {
            if (listOnExit)
               if (k_aktuell!=null)
                  for (KernelInfo kernelInfo:k_aktuell)
                     System.out.println(kernelInfo.getLine());
               else
                  if (last!=null)
                     for (KernelInfo kernelInfo:last)
                        System.out.println(kernelInfo.getLine());
         }
      }));
      // Query.showAll();
      Maxi.start(args);
      // System.exit(20);
   }
   public Maxi() {}
   public static void start(String[] args) {
      String flags=String.join(" ", args);
      listAll=(flags.matches(".*-[a-z]*l.*"));
      for (List<String> list:Query.TPUT.getList(Pattern.compile("([0-9])")))
         if (!list.isEmpty())
            colorize=true;
      System.out.println(KernelInfo.getHeader());
      if (flags.matches(".*-[a-z]*w.*")) { // watch
         listOnExit=true;
         System.out.println("will run until ^c is pressed");
         k_aktuell=KernelInfo.analyse();
         for (KernelInfo kernelInfo:k_aktuell)
            System.out.println("  : 0.   "+kernelInfo.getLine()); // Gib die ektuelle analyse aus
         Instant startZeitpunkt=Instant.now();
         do { // Beobachte Änderungen bis zum Abbruch
            last=k_aktuell;
            KernelInfo.clear();
            try {
               Thread.sleep(1*100L);
            } catch (InterruptedException e) { /* nothing to do */}
            k_aktuell=KernelInfo.analyse();
            ArrayList<KernelInfo> dif=new ArrayList<>();
            kein_dif: for (KernelInfo a:k_aktuell) {
               String b=a.getLine().replaceAll(" ", "");
               for (KernelInfo c:last)
                  if (c.getLine().replaceAll(" ", "").compareTo(b)==0)
                     continue kein_dif;
               dif.add(a);
            }
            if (!dif.isEmpty()) {
               long   milli=Duration.between(startZeitpunkt, Instant.now()).toMillis();
               @SuppressWarnings("boxing")
               String z    =String.format("%2d:%02d.%03d", milli/60000L, (milli/1000L)%60, milli%1000);
               for (KernelInfo kernelInfo:dif)
                  System.out.println(z+kernelInfo.getLine());
            }
         } while (true);
      }
      k_aktuell=KernelInfo.analyse();
      for (KernelInfo kernelInfo:k_aktuell)
         System.out.println(kernelInfo.getLine()); // Gib die ektuelle analyse aus
      m_aktuell=ModuleInfo.analyse();
      for (ModuleInfo moduleInfo:m_aktuell) {
         System.out.println(moduleInfo.getLine());
      }
   }
   public static void show(List<Iterable<String>> f) {
      for (Iterable<String> list:f) {
         for (String s:list)
            System.out.print(s);
         System.out.println();
      }
   }
   private static class Info {
      final Iterable<String> info;
      @SuppressWarnings("boxing")
      public Info(Iterable<String> iterableInfo, ArrayList<Integer> spalten) {
         info=iterableInfo;
         int index=0;
         for (String text:iterableInfo) {
            if (index>=spalten.size())
               spalten.add(0);
            if (text.length()>spalten.get(index))
               spalten.set(index, text.length());
            index++;
         }
      }
      /**
       * 
       * @return one Line of the Info
       */
      public String getLine(Iterator<Integer> len) {
         StringBuilder sb=new StringBuilder();
         for (String text:info) {
            if (colorize)
               if (sb.length()==0)
                  sb.append(green);
               else
                  if (text.endsWith(":"))
                     sb.append(green);
                  else
                     if (text.startsWith("<"))
                        sb.append(red);
                     else
                        sb.append(white);
            sb.append(" ");
            sb.append(text);
            for (@SuppressWarnings("boxing")
            int i=len.next(); i>text.length(); i--)
               sb.append(" ");
         }
         while (' '==sb.charAt(sb.length()-1))
            sb.deleteCharAt(sb.length()-1);
         if (colorize)
            sb.append(reset);
         return sb.toString();
      }
      /**
       * create a list of kernelss to search for
       */
      static public Map<Predicate<String>, ArrayList<String>> getBasis() {
         // Look for a list of kernels
         List<List<String>>                        kernels=listAll ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
                  : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
         Map<Predicate<String>, ArrayList<String>> basis  =new LinkedHashMap<>();
         final String                              r      ="abcdef";
         for (List<String> k:kernels) {
            ArrayList<String> info  =k.stream().collect(Collectors.toCollection(ArrayList<String>::new));
            String            kNr   =info.remove(1);
            // * linux44 * linux515-rt
            String            search="linuxabcdef$"
                     // initramfs-4.4-x86_64.img initramfs-5.15-rt-x86_64-fallback.img
                     +"|^initr.m.s-a[.]bcdef[-.].*img$"
                     // extramodules-4.4-MANJARO extramodules-5.15-rt-MANJAR
                     +"|^.xtr.mo.ul.s-a[.]bcdef-MANJARO$"
                     // cat kver -> 4.4.294-1-MANJARO x64 5.15.5-rt22-1-MANJARO x64
                     +"|^a[.]bc[.][-0-9]*def[-0-9]*MANJARO(?:[ 0-9]+[KM])?"
                     // vmlinuz-4.4-x86_64 vmlinuz-5.15-rt-x86_64
                     +"|^vmlinuz-a[.]bcdef-x86_64$";
            for (int i=0; i<r.length(); i++)
               search=search.replaceAll(""+r.charAt(i), (i<kNr.length() ? ""+kNr.charAt(i) : ""));
            // search becomes a pattern to identify this one kernel
            basis.put(Pattern.compile(search).asPredicate(), info);
         }
         return basis;
      }
      protected static String searchFor(List<List<String>> lls, Predicate<String> pr, String success, String error) {
         Optional<String> erg=lls.stream().flatMap(ims -> ims.stream()).filter(pr).findAny();
         if (!erg.isPresent())
            return error;
         return success.replaceFirst("§", erg.get());
      }
   }

   /**
    * Collects the infos for one kernel and aligns columns
    * 
    * @author andreas kielkopf
    */
   private static class KernelInfo extends Info {
      static ArrayList<Integer> kspalten=new ArrayList<>();
      public KernelInfo(Iterable<String> iterableInfo) {
         super(iterableInfo, kspalten);
      }
      public String getLine() {
         return getLine(kspalten.iterator());
      }
      static public String getHeader() {
         List<List<String>> running=Query.MHWD_LI.getList(Pattern.compile(".*running.*"));
         for (Iterable<String> imr:running)
            for (String text:imr)
               return (text);
         return "";
      }
      static public void clear() {
         Query.LS.clear();
         Query.CAT_KVER.clear();
         Query.DU_MODULES.clear();
         Query.MHWD_LI.clear();
      }
      /**
       * 
       * @return
       */
      static public List<KernelInfo> analyse() { // String D =DATE;
         /// In collect werden die Ergebnisse gesammelt je kernel ein Eintrag
         /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
         List<List<String>>                        available=(!listAll) ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
                  : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
         /// Zeige den Kernel und die initramdisks in /boot
         List<List<String>>                        vmlinuz  =Query.LS.getList(Pattern.compile(DATE+"(vmlinuz.*)"));
         List<List<String>>                        initrd   =Query.LS.getList(Pattern.compile(DATE+"(init.*64[.]img)"));
         List<List<String>>                        fallback =Query.LS.getList(Pattern.compile(DATE+"(init.*fallback[.]img)"));
         /// Zeige die Kernelversion
         List<List<String>>                        kver     =Query.CAT_KVER.getList(Pattern.compile("([-0-9.rt]+MANJARO).*"));
         List<String>                              module   =Query.DU_MODULES.getList(Pattern.compile("([0-9]+[KM]?)[^0-9]+([-0-9rt.]+MANJARO)"), "§2 §1");
         Map<Predicate<String>, ArrayList<String>> basis    =getBasis();
         for (Entry<Predicate<String>, ArrayList<String>> entry:basis.entrySet()) {
            Predicate<String> pr  =entry.getKey();
            ArrayList<String> ergs=entry.getValue();
            ergs.add(searchFor(available, pr, "OK", listAll ? "-" : "<EOL>"));
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
         ArrayList<KernelInfo> kernel_list=new ArrayList<>();
         for (Entry<Predicate<String>, ArrayList<String>> kernelInfo:basis.entrySet())
            kernel_list.add(new KernelInfo(kernelInfo.getValue()));
         return kernel_list;
      }
   }

   /**
    * 
    * @author andreas kielkopf
    *
    */
   private static class ModuleInfo extends Info {
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
         List<List<String>> available=(!listAll) ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
                  : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
         /// Zeige den Kernel und die initramdisks in /boot
         List<List<String>> vmlinuz  =Query.LS.getList(Pattern.compile(DATE+"(vmlinuz.*)"));
         List<List<String>> initrd   =Query.LS.getList(Pattern.compile(DATE+"(init.*64[.]img)"));
         List<List<String>> fallback =Query.LS.getList(Pattern.compile(DATE+"(init.*fallback[.]img)"));
         /// Zeige die Kernelversion
         List<List<String>> kver     =Query.CAT_KVER.getList(Pattern.compile("([-0-9.rt]+MANJARO).*"));
         List<String>       module   =Query.DU_MODULES.getList(Pattern.compile("([0-9]+[KM]?)[^0-9]+([-0-9rt.]+MANJARO)"), "§2 §1");
         for (Entry<Predicate<String>, ArrayList<String>> entry:getBasis().entrySet()) {
            Predicate<String> pr  =entry.getKey();
            ArrayList<String> ergs=entry.getValue();
            ergs.add(searchFor(available, pr, "OK", listAll ? "-" : "<EOL>"));
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

   /**
    * Definition von Abfragen mit eingebautem Caching
    * 
    * @author andreas kielkopf
    */
   enum Query {
      LS("ls", "-lA", "/boot", "/boot/grub", "/lib/modules") //
      , TPUT("tput", "colors")
      // , ZLS(SHELL, "-c", "print -l /boot/*(.) /boot/grub/*(.) /lib/modules/*(/)") //
      , MHWD_L("mhwd-kernel", "-l") //
      , MHWD_LI("mhwd-kernel", "-li") //
      , CAT_KVER(SHELL, "-c", "cat /boot/*.kver") //
      , DU_MODULES(SHELL, "-c", "du -sh /lib/modules/*")//
      , SHA_VMLINUZ(SHELL, "-c", "sha256sum /boot/*img /boot/vmlinuz*") //
      , SHA_M_VMLINUZ(SHELL, "-c", "sha256sum /lib/modules/*/vmlinuz") //
      , SHA_MODULES(SHELL, "-c", "LC_ALL=C;for K in $(find /lib/modules/* -maxdepth 0 -type d|sort);"
               +"do cd $K;echo -n \"$K \";for D in $(find . -type f|sort);do cat $D;done|sha256sum; done")//
      // , ZSHA_MODULES("zsh", "-c", "LC_ALL=C;for K in $(print -o /lib/modules/*(/));"//
      // +"do echo -n \"$K \";for D in $(print -l $K/**/*(.)|sort);do cat $D;done|sha256sum; done;")//
      //
      ;
      private final String[] cmd;
      private List<String>   result=null;
      Query(String... command) {
         cmd=command;
      }
      static void showAll() {
         for (Query q:values()) { // if (q.name().contains("H")) break;
            System.out.println();
            System.out.print(q.name()+": ");
            for (String c:q.cmd)
               System.out.print(c+" ");
            System.out.println();
            q.evaluate();
            for (String s:q.result)
               System.out.println(s);
         }
      }
      void evaluate() {
         try {
            Process           p =pb.command(cmd).redirectErrorStream(true).start();
            InputStreamReader ir=new InputStreamReader(p.getInputStream());
            p.waitFor(5, TimeUnit.SECONDS);
            try (BufferedReader br=new BufferedReader(ir); Stream<String> li=br.lines()) {
               result=li.collect(Collectors.toList());
            }
         } catch (IOException e) {
            e.printStackTrace();
         } catch (InterruptedException e) {
            System.out.println("Timeout: ");
            e.printStackTrace();
         }
      }
      public List<IterableMatchResult> getIterable(Pattern pa) {
         if (result==null)
            evaluate();
         return result.stream().filter(s -> pa.matcher(s).find()).map(s -> toIterable(pa.matcher(s))).collect(Collectors.toList());
      }
      public List<List<String>> getList(Pattern pa) {
         return getIterable(pa).stream()
                  .map(i -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(i.iterator(), Spliterator.ORDERED), false).collect(Collectors.toList()))
                  .collect(Collectors.toList());
      }
      public List<String> getList(Pattern pa, String replace) {
         return getIterable(pa).stream().map(i -> i.replace(replace)).collect(Collectors.toList());
      }
      private static IterableMatchResult toIterable(Matcher ma) {
         return ma.find() ? new IterableMatchResult(ma) : null;
      }
      public void clear() {
         result=null;
      }
      /**
       * Iterator über Matchresult Ein Match ohne Klammern liefert einen GesamtString Ein Match mit Klammern liefert für jede Klammer einen String, aber keinen
       * Gesamtstring
       * 
       * @author andreas kielkopf
       */
      public static class IterableMatchResult implements Iterable<String> {
         final MatchResult matchResult;
         final int         start;
         public IterableMatchResult(MatchResult mr) {
            matchResult=mr;
            start=(matchResult.groupCount()==0) ? 0 : 1;
         }
         @Override
         public Iterator<String> iterator() {
            return new Iterator<String>() {
               private int index=start;
               @Override
               public String next() {
                  return matchResult.group(index++);
               }
               @Override
               public boolean hasNext() {
                  return index<=matchResult.groupCount();
               }
            };
         }
         public String replace(String replacement) {
            if (matchResult.groupCount()==0)
               return replacement.replaceAll("$0", matchResult.group(0));
            String erg=replacement;
            char   nr ='1';
            for (String s:this) {
               String q="§"+nr++;
               erg=erg.replaceAll(q, s);
            }
            return erg;
         }
      }
   }
}

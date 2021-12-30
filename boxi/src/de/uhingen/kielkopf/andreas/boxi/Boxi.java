/**
 * 
 */
package de.uhingen.kielkopf.andreas.boxi;

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

/**
 * @author andreas
 *
 */
public class Boxi {
   final static char           ESC     =27;
   final static String         green   =ESC+"[32m";
   final static String         red     =ESC+"[1;31m";
   final static String         white   =ESC+"[0;97m";
   final static String         reset   =ESC+"[0m";
   final static String         TAGD    ="(?:[1-3 ]?[0-9][.] )?";
   final static String         MONAT   ="[A-Z][a-z][a-z] ";
   final static String         TAGE    ="(?:[1-3 ]?[0-9] )?";
   final static String         REST    ="(?:[ 0-9][0-9]{3}[ 0-9]|[0-9:]{5})";
   final static String         DATE    ="("+TAGD+MONAT+TAGE+REST+").*";
   final static boolean        colorize=true;
   final static ProcessBuilder pb      =new ProcessBuilder();
   static List<KernelInfo>     aktuell =null;
   static List<KernelInfo>     last    =null;
   /**
    * @param args
    */
   public static void main(String[] args) {
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
         @Override
         public void run() {
            // System.out.println("---");
            if (aktuell!=null)
               for (KernelInfo kernelInfo:aktuell)
                  System.out.println(kernelInfo.getLine());
            else
               if (last!=null)
                  for (KernelInfo kernelInfo:last)
                     System.out.println(kernelInfo.getLine());
         }
      }));
      Boxi.start(args);
   }
   public Boxi() {}
   public static void start(String[] args) {
      String flags=String.join(" ", args);
      KernelInfo.listAll=(flags.matches(".*-[a-z]*l.*"));
      System.out.println(KernelInfo.getHeader());
      aktuell=KernelInfo.analyse();
      if (flags.matches(".*-[a-z]*w.*")) {
         for (KernelInfo kernelInfo:aktuell)
            System.out.println("  :  .   "+kernelInfo.getLine()); // Gib die ektuelle analyse aus
         System.out.println("will run until ^c is pressed");
         Instant startZeitpunkt=Instant.now();
         do { // Beobachte Änderungen bis zum Abbruch
            last=aktuell;
            try {
               Thread.sleep(1*100L);
            } catch (InterruptedException e) {}
            KernelInfo.clear();
            Instant jetzt=Instant.now();
            aktuell=KernelInfo.analyse();
            ArrayList<KernelInfo> dif=new ArrayList<>();
            kein_dif: for (KernelInfo a:aktuell) {
               String b=a.getLine().replaceAll(" ", "");
               for (KernelInfo c:last) {
                  String d=c.getLine().replaceAll(" ", "");
                  if (d.compareTo(b)==0)
                     continue kein_dif;
               }
               dif.add(a);
            }
            Duration zeit =Duration.between(startZeitpunkt, jetzt);
            long     milli=zeit.toMillis();
            long     min  =milli/60000L;
            long     sec  =(milli/1000L)%60;
            milli%=1000;
            String z=String.format("%2d:%02d.%03d", min, sec, milli);
            for (KernelInfo kernelInfo:dif)
               System.out.println(z+kernelInfo.getLine());
         } while (true);
      }
      // for (KernelInfo kernelInfo:aktuell)
      // System.out.println(kernelInfo.getLine()); // Gib die ektuelle analyse aus
   }
   public static void show(List<Iterable<String>> f) {
      for (Iterable<String> list:f) {
         for (String s:list)
            System.out.print(s);
         System.out.println();
      }
   }
   /**
    * Collects the infos for one kernel and aligns columns
    * 
    * @author andreas kielkopf
    */
   private static class KernelInfo {
      public static boolean          listAll=false;
      private final Iterable<String> info;
      static ArrayList<Integer>      maxlen =new ArrayList<>();
      public KernelInfo(Iterable<String> iterableInfo) {
         info=iterableInfo;
         int index=0;
         for (String text:iterableInfo) {
            if (index>=maxlen.size())
               maxlen.add(0);
            if (text.length()>maxlen.get(index))
               maxlen.set(index, text.length());
            index++;
         }
      }
      static public String getHeader() {
         List<Iterable<String>> running=Query.MHWD_LI.getIterable(Pattern.compile(".*running.*"));
         for (Iterable<String> imr:running)
            for (String text:imr)
               return (text);
         return "";
      }
      public String getLine() {
         Iterator<Integer> len=maxlen.iterator();
         StringBuilder     sb =new StringBuilder();
         for (String text:info) {
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
            for (int i=len.next()+1; i>text.length(); i--)
               sb.append(" ");
            sb.append(text);
         }
         sb.append(reset);
         return sb.toString();
      }
      static public void clear() {
         Query.LS.clear();
         Query.CAT_KVER.clear();
         Query.DU_MODULES.clear();
         Query.MHWD_LI.clear();
      }
      static public List<KernelInfo> analyse() {
         // Look for kernels installed
         List<List<String>>                        kernels =listAll ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
                  : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
         /// In collect werden die Ergebnisse gesammelt je kernel ein Eintrag
         Map<Predicate<String>, ArrayList<String>> sammlung=new LinkedHashMap<>();
         for (List<String> k:kernels) {
            ArrayList<String> info    =k.stream().collect(Collectors.toCollection(ArrayList<String>::new));
            String            kernelNr=info.remove(1);
            String            rp      ="abcdef";
            String            stext   ="linuxabcdef$"
                     // * linux44 * linux515-rt
                     // initramfs-4.4-x86_64.img initramfs-5.15-rt-x86_64-fallback.img
                     +"|^initr.m.s-a[.]bcdef[-.].*img$"
                     // extramodules-4.4-MANJARO extramodules-5.15-rt-MANJAR
                     +"|^.xtr.mo.ul.s-a[.]bcdef-MANJARO$"
                     // cat kver -> 4.4.294-1-MANJARO x64 5.15.5-rt22-1-MANJARO x64
                     +"|^a[.]bc[.][-0-9]*def[-0-9]*MANJARO(?:[ 0-9]+[KM])?"
                     // vmlinuz-4.4-x86_64 vmlinuz-5.15-rt-x86_64
                     +"|^vmlinuz-a[.]bcdef-x86_64$";
            for (int i=0; i<rp.length(); i++)
               stext=stext.replaceAll(""+rp.charAt(i), (i<kernelNr.length() ? ""+kernelNr.charAt(i) : ""));
            // System.out.println(stext);
            sammlung.put(Pattern.compile(stext).asPredicate(), info);
         }
         /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
         List<List<String>> available=(!listAll) ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
                  : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
         /// Zeige den Kernel und die initramdisks in /boot
         List<List<String>> vmlinuz  =Query.LS.getList(Pattern.compile(DATE+"(vmlinuz.*)"));
         List<List<String>> initrd   =Query.LS.getList(Pattern.compile(DATE+"(init.*64[.]img)"));
         List<List<String>> fallback =Query.LS.getList(Pattern.compile(DATE+"(init.*fallback[.]img)"));
         /// Zeige die Kernelversion
         List<List<String>> kver     =Query.CAT_KVER.getList(Pattern.compile("([-0-9.rt]+MANJARO).*"));
         List<List<String>> module   =Query.DU_MODULES.getList(Pattern.compile("([0-9]+[KM]?)[^0-9]+([-0-9rt.]+MANJARO)"));
         for (List<String> m:module)
            m.add(m.remove(1)+" "+m.remove(0)); // swap ???
         for (Entry<Predicate<String>, ArrayList<String>> entry:sammlung.entrySet()) {
            Predicate<String> pr  =entry.getKey();
            ArrayList<String> ergs=entry.getValue();
            ergs.add(searchFor2(available, pr, "OK", listAll ? "-" : "<EOL>"));
            ergs.add(searchFor2(vmlinuz, pr, "§", "<vmlinuz missing>"));
            ergs.add(searchFor2(initrd, pr, "§", "<initrd missing>"));
            ergs.add(searchFor2(fallback, pr, " fallback OK ", "<no fallback>"));
            ergs.add(searchFor2(kver, pr, "§", "<kver missing>"));
            String            kernelVersion=searchFor2(kver, pr, "§", "<kver missing>");
            Predicate<String> p2           =Pattern.compile(kernelVersion, Pattern.LITERAL).asPredicate();
            ergs.add("modules:");
            ergs.add(searchFor2(module, p2, "§", "<missing>"));
         }
         ArrayList<KernelInfo> kernel_list=new ArrayList<>();
         for (Entry<Predicate<String>, ArrayList<String>> kernelInfo:sammlung.entrySet())
            kernel_list.add(new KernelInfo(kernelInfo.getValue()));
         return kernel_list;
      }
      /*
       * static private String searchFor(List<Iterable<String>> imr, Predicate<String> pr, String success, String error) { Optional<String>
       * erg=imr.stream().flatMap(ims -> ims.stream()).filter(pr).findAny(); if (!erg.isPresent()) return error; return success.replaceFirst("§", erg.get()); }
       */
      static private String searchFor2(List<List<String>> lls, Predicate<String> pr, String success, String error) {
         Optional<String> erg=lls.stream().flatMap(ims -> ims.stream()).filter(pr).findAny();
         if (!erg.isPresent())
            return error;
         return success.replaceFirst("§", erg.get());
      }
   }

   /**
    * Definition von Abfragen mit eingebautem caching
    * 
    * @author andreas kielkopf
    */
   enum Query {
      LS("/bin/ls", "-lA", "/boot", "/boot/grub", "/lib/modules"), //
      MHWD_L("/bin/mhwd-kernel", "-l"), //
      MHWD_LI("/bin/mhwd-kernel", "-li"), //
      CAT_KVER("/bin/sh", "-c", "/bin/cat /boot/*.kver"), //
      DU_MODULES("/bin/sh", "-c", "/bin/du -sh /lib/modules/*");//
      final String[]       cmd;
      private List<String> result=null;
      Query(String... command) {
         cmd=command;
      }
      private void evaluate() {
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
            System.out.println("Timeout");
            e.printStackTrace();
         }
      }
      public List<Iterable<String>> getIterable(Pattern pa) {
         if (result==null)
            evaluate();
         return result.stream().filter(s -> pa.matcher(s).find()).map(s -> toIterable(pa.matcher(s))).collect(Collectors.toList());
      }
      public List<List<String>> getList(Pattern pa) {
         return getIterable(pa).stream()
                  .map(i -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(i.iterator(), Spliterator.ORDERED), false).collect(Collectors.toList()))
                  .collect(Collectors.toList());
      }
      private Iterable<String> toIterable(Matcher ma) {
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
      private class IterableMatchResult implements Iterable<String> {
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
         /*
          * public Stream<String> stream() { return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false); }
          */
      }
   }
}

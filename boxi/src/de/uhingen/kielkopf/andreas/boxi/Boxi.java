/**
 * 
 */
package de.uhingen.kielkopf.andreas.boxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
   static String               DATE    ="("+TAGD+MONAT+TAGE+REST+").*";
   final static boolean        colorize=true;
   final static ProcessBuilder pb      =new ProcessBuilder();
   /**
    * @param args
    */
   public static void main(String[] arwgs) {
      // new Boxi();
      Boxi.start(arwgs);
   }
   public Boxi() {}
   public static void start(String[] aregs) {
      // for (String s:args)
      // Map<String, Query> infomap=new LinkedHashMap<>();
      for (KernelInfo kernelInfo:KernelInfo.analyse())
         kernelInfo.println();
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
      private final Iterable<String> info;
      static ArrayList<Integer>      maxlen=new ArrayList<>();
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
      public void println() {
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
         System.out.println(sb.toString());
      }
      static public Iterable<KernelInfo> analyse() {
         List<Iterable<String>> running=Query.MHWD_LI.getIterable(Pattern.compile(".*running.*"));
         for (Iterable<String> imr:running)
            for (String text:imr)
               System.out.println(text);
         // Look for kernels installed
         List<List<String>>                        installiert=Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
         /// In collect werden die Ergebnisse gesammelt je kernel ein Eintrag
         Map<Predicate<String>, ArrayList<String>> sammlung   =new LinkedHashMap<>();
         for (List<String> k:installiert) {
            ArrayList<String> info    =k.stream().collect(Collectors.toCollection(ArrayList<String>::new));
            String            kernelNr=info.remove(1);
            String            stext   ="uxabc$|uxabc-|s-a[.]bc[-.]|^a[.]bc[.]|uz-a[.]bc-";
            for (int i=0; i<=2; i++)
               stext=stext.replaceAll(""+"abc".charAt(i), (i<kernelNr.length() ? ""+kernelNr.charAt(i) : ""));
            sammlung.put(Pattern.compile(stext).asPredicate(), info);
         }
         /// Prüfe ob der Kernel noch unterstützt wird OK/[EOL]
         List<List<String>> available=Query.MHWD_L.getList(Pattern.compile(".*linux.*"));
         /// Zeige den Kernel und die initramdisks in /boot
         List<List<String>> vmlinuz  =Query.LS.getList(Pattern.compile(DATE+"(vmlinuz.*)"));
         List<List<String>> initrd   =Query.LS.getList(Pattern.compile(DATE+"(init.*64[.]img)"));
         List<List<String>> fallback =Query.LS.getList(Pattern.compile(DATE+"(init.*fallback[.]img)"));
         /// Zeige die Kernelversion
         List<List<String>> kver     =Query.CAT_KVER.getList(Pattern.compile("([-0-9.]+MANJARO).*"));
         List<List<String>> module   =Query.DU_MODULES.getList(Pattern.compile("([0-9]+[KM])[^0-9]+([-0-9.]+MANJARO)"));
         for (List<String> m:module)
            m.add(m.remove(1)+" "+m.remove(0)); // swap ???
         for (Entry<Predicate<String>, ArrayList<String>> entry:sammlung.entrySet()) {
            Predicate<String> pr  =entry.getKey();
            ArrayList<String> ergs=entry.getValue();
            ergs.add(searchFor2(available, pr, "OK", "<EOL>"));
            ergs.add(searchFor2(vmlinuz, pr, "§", "<vmlinuz missing>"));
            ergs.add(searchFor2(initrd, pr, "§", "<initrd missing>"));
            ergs.add(searchFor2(fallback, pr, "fallback OK", "<fallback missing>"));
            ergs.add(searchFor2(kver, pr, "§", "<kver missing>"));
            String            kernelVersion=searchFor2(kver, pr, "§", "<kver missing>");
            Predicate<String> p2           =Pattern.compile(kernelVersion, Pattern.LITERAL).asPredicate();
            ergs.add("modules:");
            ergs.add(searchFor2(module, p2, "§", "<missing>"));
         }
         ArrayList<KernelInfo> kernels=new ArrayList<>();
         for (Entry<Predicate<String>, ArrayList<String>> kernelInfo:sammlung.entrySet())
            kernels.add(new KernelInfo(kernelInfo.getValue()));
         return kernels;
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
      private void evaluate(Pattern pa) {
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
            evaluate(null);
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
         result.clear();
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

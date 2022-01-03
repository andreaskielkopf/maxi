/**
 * 
 */
package de.uhingen.kielkopf.andreas.maxi;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author andreas
 *
 */
public class Maxi {
   static List<KernelInfo>     k_aktuell =null;
   static List<KernelInfo>     last      =null;
   static List<ModuleInfo>     m_aktuell =null;
   final public static String  SHELL     =System.getenv("SHELL");
   final public static boolean zsh       =Maxi.SHELL.contains("zsh");
   static boolean              listOnExit=false;
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
      KernelInfo.listAll=(flags.matches(".*-[a-z]*l.*"));
      for (List<String> list:Query.TPUT.getList(Pattern.compile("([0-9])")))
         if (!list.isEmpty())
            InfoLine.colorize=true;
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
}

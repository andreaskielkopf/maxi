/**
 * 
 */
package de.uhingen.kielkopf.andreas.maxi;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 * @version 0.2
 * @date 7.1.2022
 */
public class Maxi {
   static List<KernelInfo>    k_aktuell=null;
   static List<KernelInfo>    last     =null;
   static List<ModuleInfo>    m_aktuell=null;
   final public static String SHELL    =System.getenv("SHELL");
   public Maxi() {}
   /**
    * @param args
    */
   public static void main(String[] args) {
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
         @Override
         public void run() {
            if (!Flag.HELP.get() && Flag.LISTONEXIT.get()) {
               System.out.println();
               if (k_aktuell != null)
                  for (KernelInfo kernelInfo:k_aktuell)
                     System.out.println(kernelInfo);
               else
                  if (last != null)
                     for (KernelInfo kernelInfo:last)
                        System.out.println(kernelInfo);
            }
         }
      }));
      Flag.ZSH.set(SHELL.contains("zsh"));
      Flag.setArgs(args, "-g");// -km
      if (Flag.HELP.get())
         System.exit(9);
      if (Flag.WATCH.get() && Flag.SHASUM.get()) {
         Flag.SHASUM.set(false);
         StringBuilder sb=new StringBuilder();
         if (Flag.COLOR.get())
            sb.append(InfoLine.RED);
         sb.append("When --watch is enabled, checksumming is disabled");
         if (Flag.COLOR.get())
            sb.append(InfoLine.RESET);
         System.out.println(sb.toString());
      }
      start();
   }
   public static void show(List<Iterable<String>> f) {
      for (Iterable<String> list:f) {
         for (String s:list)
            System.out.print(s);
         System.out.println();
      }
   }
   public static void start() {
      for (List<String> list:(Flag.ZSH.get() ? Query.TERMINFO : Query.TPUT).getLists(Pattern.compile("([0-9])")))
         if (!list.isEmpty())
            Flag.COLOR.set(true);
      if (Flag.WATCH.get()) { // watch
         long pause=100L;
         try {
            String s=Flag.WATCH.getParameter();
            int    i=Integer.parseInt(s);
            if ((i > 0) && (i <= 60000))
               pause=i;
         } catch (NumberFormatException e1) {
            System.exit(10);
         }
         Flag.LISTONEXIT.set(true);
         System.out.println(KernelInfo.getHeader());
         System.out.println("will run until ^c is pressed");
         k_aktuell=KernelInfo.analyseStream().collect(Collectors.toList());
         for (KernelInfo kernelInfo:k_aktuell)
            System.out.println("  : 0.   " + kernelInfo); // Gib die aktuelle analyse aus
         Instant startZeitpunkt=Instant.now();
         do { // Beobachte Änderungen bis zum Abbruch
            last=k_aktuell;
            KernelInfo.clear();
            // System.out.println(Flag.WATCH.getParameter());
            try {
               Thread.sleep(pause);
            } catch (InterruptedException e) { /* nothing to do */}
            k_aktuell=KernelInfo.analyseStream().collect(Collectors.toList());
            ArrayList<KernelInfo> dif=new ArrayList<>();
            kein_dif: for (KernelInfo a:k_aktuell) {
               String b=a.toString().replaceAll(" ", "");
               for (KernelInfo c:last)
                  if (c.toString().replaceAll(" ", "").compareTo(b) == 0)
                     continue kein_dif;
               dif.add(a);
            }
            if (!dif.isEmpty()) {
               long   milli=Duration.between(startZeitpunkt, Instant.now()).toMillis();
               @SuppressWarnings("boxing")
               String z    =String.format("%2d:%02d.%03d", milli / 60000L, (milli / 1000L) % 60, milli % 1000);
               for (KernelInfo kernelInfo:dif)
                  System.out.println(z + kernelInfo);
            }
         } while (true);
      }
      System.out.println(KernelInfo.getHeader());
      if (Flag.KERNEL.get() || Flag.LIST_ALL.get())
         KernelInfo.analyseStream().collect(Collectors.toList()).forEach(System.out::println);
      // Gib dieaktuelle analyse aus
      if (Flag.MODULES.get()) {
         System.out.println(ModuleInfo.getHeader());
         ModuleInfo.analyseStream().collect(Collectors.toList()).forEach(System.out::println);
      }
      if (Flag.GRUB.get()) {
         System.out.println(GrubInfo.getHeader());
         GrubInfo.analyseStream().collect(Collectors.toList()).forEach(System.out::println);
      }
   }
}

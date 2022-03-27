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
 * @version 0.6.9
 * @date 26.3.2022
 */
public class Maxi {
   static List<KernelInfo>       k_aktuell  =null;
   static List<KernelInfo>       last       =null;
   static List<ModuleInfo>       m_aktuell  =null;
   final public static String    SHELL      =System.getenv("SHELL");
   final public static String    BACKTICKS  ="```";
   final public static String    DETAILS0   ="[details=\"maxi";
   final public static String    DETAILS1   ="\"]";
   final public static String    DETAILS2   ="[/details]";
   final static ClipboardSupport clipSupport=new ClipboardSupport();
   public Maxi() {}
   /**
    * @param args
    */
   public static void main(String[] args) {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         if (!Flag.HELP.get() && Flag.LISTONEXIT.get()) {
            clipSupport.println();
            if (k_aktuell != null)
               for (final KernelInfo kernelInfo1:k_aktuell)
                  clipSupport.println(kernelInfo1);
            else
               if (last != null)
                  for (final KernelInfo kernelInfo2:last)
                     clipSupport.println(kernelInfo2);
            if (Flag.FORUM.get()) {
               clipSupport.clipln(BACKTICKS);
               clipSupport.clipln(DETAILS2);
               clipSupport.transfer();
            }
         }
      }));
      Flag.ZSH.set(SHELL.contains("zsh"));
      //
      Flag.setArgs(args, "-km");// -km -efgikmpsv
      //
      if (Flag.HELP.get())
         System.exit(9);
      if (Flag.USAGE.get())
         System.exit(8);
      if (Flag.KVER.get())
         Flag.KERNEL.set(true);
      if (Flag.FORUM.get()) {
         clipSupport.clipln(DETAILS0 + Flag.arg + DETAILS1);
         clipSupport.clipln(BACKTICKS);
      }
      if (Flag.WATCH.get() && Flag.SHASUM.get()) {
         Flag.SHASUM.set(false);
         final StringBuilder sb=new StringBuilder();
         if (Flag.COLOR.get())
            sb.append(InfoLine.RED);
         sb.append("When --watch is enabled, checksumming is disabled");
         if (Flag.COLOR.get())
            sb.append(InfoLine.RESET);
         clipSupport.println(sb.toString());
      }
      start();
   }
   public static void show(List<Iterable<String>> f) {
      for (final Iterable<String> list:f) {
         for (final String s:list)
            clipSupport.print(s);
         clipSupport.println();
      }
   }
   public static void start() {
      for (final List<String> list:(Flag.ZSH.get() ? Query.TERMINFO : Query.TPUT).getLists(Pattern.compile("([0-9])")))
         if (!list.isEmpty())
            Flag.COLOR.set(true);
      if (Flag.WATCH.get()) { // watch
         long pause=100L;
         try {
            final String s=Flag.WATCH.getParameter();
            final int    i=Integer.parseInt(s);
            if ((i > 0) && (i <= 60000))
               pause=i;
         } catch (final NumberFormatException e1) {
            System.exit(10);
         }
         Flag.LISTONEXIT.set(true);
         clipSupport.println(KernelInfo.getHeader());
         clipSupport.printonly("will run until ^c is pressed");
         k_aktuell=KernelInfo.analyseStream().collect(Collectors.toList());
         for (final KernelInfo kernelInfo:k_aktuell)
            clipSupport.println("  : 0.   " + kernelInfo); // Gib die aktuelle analyse aus
         final Instant startZeitpunkt=Instant.now();
         do { // Beobachte Änderungen bis zum Abbruch
            last=k_aktuell;
            KernelInfo.clear();
            // slipboardSupport.println(Flag.WATCH.getParameter());
            try {
               Thread.sleep(pause);
            } catch (final InterruptedException e) { /* nothing to do */}
            k_aktuell=KernelInfo.analyseStream().collect(Collectors.toList());
            final ArrayList<KernelInfo> dif=new ArrayList<>();
            kein_dif: for (final KernelInfo a:k_aktuell) {
               final String b=a.toString().replaceAll(" ", "");
               for (final KernelInfo c:last)
                  if (c.toString().replaceAll(" ", "").compareTo(b) == 0)
                     continue kein_dif;
               dif.add(a);
            }
            if (!dif.isEmpty()) {
               final long   milli=Duration.between(startZeitpunkt, Instant.now()).toMillis();
               @SuppressWarnings("boxing")
               final String z    =String.format("%2d:%02d.%03d", milli / 60000L, (milli / 1000L) % 60, milli % 1000);
               for (final KernelInfo kernelInfo:dif)
                  clipSupport.println(z + kernelInfo);
            }
         } while (true);
      }
      clipSupport.println(KernelInfo.getHeader());
      if (Flag.KERNEL.get() || Flag.LIST_ALL.get())
         KernelInfo.analyseStream().collect(Collectors.toList()).forEach(clipSupport::println);
      // Gib dieaktuelle analyse aus
      if (Flag.MODULES.get()) {
         clipSupport.println(ModuleInfo.getHeader());
         ModuleInfo.analyseStream()// .map(p -> { slipboardSupport.print(p.getInfo() + " "); return p; })
                  .collect(Collectors.toList()).forEach(clipSupport::println);
      }
      if (Flag.GRUB.get()) {
         clipSupport.println(GrubInfo.getHeader());
         GrubInfo.analyseStream().collect(Collectors.toList()).forEach(clipSupport::println);
      }
      if (Flag.MKINITCPIO.get()) {
         clipSupport.println(MkinitcpioInfo.getHeader());
         MkinitcpioInfo.analyseStream().collect(Collectors.toList()).forEach(clipSupport::println);
      }
      if (Flag.EFI.get()) {
         clipSupport.println(EfiInfo.getHeader());
         EfiInfo.analyseStream().collect(Collectors.toList()).forEach(clipSupport::println);
         clipSupport.println(EfiVars.getHeader());
         EfiVars.analyseStream().collect(Collectors.toList()).forEach(clipSupport::println);
      }
      if (Flag.PARTITIONS.get()) {
         clipSupport.println(Partition.getHeader());
         Partition.analyseStream().collect(Collectors.toList()).forEach(clipSupport::println);
      }
      if (Flag.FORUM.get()) {
         clipSupport.clipln(BACKTICKS);
         clipSupport.clipln(DETAILS2);
         clipSupport.transfer();
      }
   }
}

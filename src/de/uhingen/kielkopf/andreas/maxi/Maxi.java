/**
 *
 */
package de.uhingen.kielkopf.andreas.maxi;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.uhingen.kielkopf.andreas.beans.cli.Flag;

/**
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 * @version 0.7.1
 * @date 18.4.2022
 */
public class Maxi {
   static List<KernelInfo>       k_aktuell  =null;
   static List<KernelInfo>       last       =null;
   static List<ModuleInfo>       m_aktuell  =null;
   final public static String    SHELL      =System.getenv("SHELL");       // get the used shell
   final public static String    BACKTICKS  ="```";                        // comments in manjaro forum
   final public static String    DETAILS0   ="[details=\"maxi";
   final public static String    DETAILS1   ="\"]";
   final public static String    DETAILS2   ="[/details]";
   final static ClipboardSupport clipSupport=new ClipboardSupport();       // suport the clipboard when used in GUI
   final static Flag             COLOR      =new Flag('c', "color");
   // final static Flag DATES=new Flag('d',"dates");
   final static Flag             EFI        =new Flag('e', "efi");
   final static Flag             FORUM      =new Flag('f', "forum");
   final static Flag             GRUB       =new Flag('g', "grub");
   final static Flag             HELP       =new Flag('h', "help");        //
   final static Flag             MKINITCPIO =new Flag('i', "mkinitcpio");
   final static Flag             KERNEL     =new Flag('k', "kernel");
   final static Flag             LIST_ALL   =new Flag('l', "list_all");
   final static Flag             MODULES    =new Flag('m', "modules");
   final static Flag             PARTITIONS =new Flag('p', "partitions");
   final static Flag             SHASUM     =new Flag('s', "shasum");
   final static Flag             USAGE      =new Flag('u', "usage");
   final static Flag             KVER       =new Flag('v', "kver");
   final static Flag             WATCH      =new Flag('w', "watch", "100");
   final static Flag             LISTONEXIT =new Flag('x', "listonexit");  // intern
   final static Flag             ZSH        =new Flag('z');                // intern
   public Maxi() {}
   /**
    * Das Hauptprogramm das die Parameter annimmt
    * @param args
    */
   public static void main(String[] args) {
      // Beim Shutdown soll noch folgendes erledigt werden (z.B. nach -w)
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         if (!HELP.get() && LISTONEXIT.get()) {
            clipSupport.println(); // Kernelinfo ausgeben
            if (k_aktuell != null) 
               for (final KernelInfo kernelInfo1:k_aktuell)
                  clipSupport.println(kernelInfo1);
            else
               if (last != null)
                  for (final KernelInfo kernelInfo2:last)
                     clipSupport.println(kernelInfo2);
            if (FORUM.get()) { // für forum formatieren und ans clipboard übergeben
               clipSupport.clipln(BACKTICKS);
               clipSupport.clipln(DETAILS2);
               clipSupport.transfer();
            }
         }
      }));
      ZSH.set(SHELL.contains("zsh"));
      // Flag.setArgs(args, "-km");// -km -efgikmpsv
      Flag.setArgs(args, "-km");// -km -efgikmpsv
      //
      if (HELP.get())
         System.exit(9);
      if (USAGE.get())
         System.exit(8);
      if (KVER.get())
         KERNEL.set(true);
      if (FORUM.get()) {
         clipSupport.clipln(DETAILS0 + Flag.getArgs() + DETAILS1);
         clipSupport.clipln(BACKTICKS);
      }
      if (WATCH.get() && SHASUM.get()) {
         SHASUM.set(false);
         final StringBuilder sb=new StringBuilder();
         if (COLOR.get())
            sb.append(InfoLine.RED);
         sb.append("When --watch is enabled, checksumming is disabled");
         if (COLOR.get())
            sb.append(InfoLine.RESET);
         clipSupport.println(sb.toString());
      }
      start();
   }
   /**
    * Gib die 2-dimensionale Liste von Strings aus mit Zeilenvorschub nur in einer dimension
    * @param f
    */
   public static void show(List<Iterable<String>> f) {
      for (final Iterable<String> list:f) {
         for (final String s:list)
            clipSupport.print(s);
         clipSupport.println();
      }
   }
   
   public static void start() {
      // können wir farbig ausgeben ?
      for (final List<String> list:(ZSH.get() ? Query.TERMINFO : Query.TPUT).getLists(Pattern.compile("([0-9])")))
         if (!list.isEmpty())
            COLOR.set(true);
      // wurde -w gewählt ?
      if (WATCH.get()) { // watch
         long pause=100L;
         try {
            final String s=WATCH.getParameter();
            final int    i=Integer.parseInt(s);
            if ((i > 0) && (i <= 60000))
               pause=i;
         } catch (final NumberFormatException e1) {
            System.exit(10);
         }
         LISTONEXIT.set(true);
         clipSupport.println(KernelInfo.getHeader());
         clipSupport.printonly("will run until ^c is pressed");
         k_aktuell=KernelInfo.analyseStream().toList();
         for (final KernelInfo kernelInfo:k_aktuell)
            clipSupport.println("  : 0.   " + kernelInfo); // Gib die aktuelle analyse aus
         final Instant startZeitpunkt=Instant.now();
         do { // Beobachte Änderungen bis zum Abbruch durch ctrl-c
            last=k_aktuell;
            KernelInfo.clear();
            // slipboardSupport.println(Flag.WATCH.getParameter());
            try {
               Thread.sleep(pause);
            } catch (final InterruptedException e) { /* nothing to do */}
            k_aktuell=KernelInfo.analyseStream().toList();
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
      if (KERNEL.get() || LIST_ALL.get())
         KernelInfo.analyseStream().toList().forEach(clipSupport::println);
      // Gib dieaktuelle analyse aus
      if (MODULES.get()) {
         clipSupport.println(ModuleInfo.getHeader());
         ModuleInfo.analyseStream()// .map(p -> { slipboardSupport.print(p.getInfo() + " "); return p; })
                  .toList().forEach(clipSupport::println);
      }
      if (GRUB.get()) {
         clipSupport.println(GrubInfo.getHeader());
         GrubInfo.analyseStream().toList().forEach(clipSupport::println);
      }
      if (MKINITCPIO.get()) {
         clipSupport.println(MkinitcpioInfo.getHeader());
         MkinitcpioInfo.analyseStream().toList().forEach(clipSupport::println);
      }
      if (EFI.get()) {
         clipSupport.println(EfiInfo.getHeader());
         EfiInfo.analyseStream().toList().forEach(clipSupport::println);
         clipSupport.println(EfiVars.getHeader());
         EfiVars.analyseStream().toList().forEach(clipSupport::println);
      }
      if (PARTITIONS.get()) {
         clipSupport.println(Partition.getHeader());
         Partition.analyseStream().toList().forEach(clipSupport::println);
      }
      if (FORUM.get()) {
         clipSupport.clipln(BACKTICKS);
         clipSupport.clipln(DETAILS2);
         clipSupport.transfer();
      }
   }
}

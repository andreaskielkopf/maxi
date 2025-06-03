package de.uhingen.kielkopf.andreas.maxi;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.uhingen.kielkopf.andreas.beans.cli.Flag;

/**
 * @author Andreas Kielkopf ©2022, 2024, 2025
 * @license GNU General Public License v3.0
 * @version 0.7.1
 * @dates 18.4.2022, 16.12.2024
 * 
 *        <pre>
 * Ein Programm um in einem manjaro-system zu prüfen ob eventuell notwendige Dateien für dennächsten Boot fehlen
 * * Welche kernels installiert und vorhanden sind
 * * Ob alle intramdisks einen Inhalt haben (Länge)
 * * Ob eine Configdatei für Grub aktuell ist
 * * Ob UEFI-Booteinträge existieren
 * * Ob in der EFI-Partition bootloader da sind 
 * 
 *        Aus Kompatibilitätsgründen wird ein Zweig für java8 abgespalten. Ausserdem ein Zweig für java21 und für
 *        </pre>
 */
public class Maxi {
   static List<KernelInfo>       k_aktuell  =null;
   static List<KernelInfo>       last       =null;
   static List<ModuleInfo>       m_aktuell  =null;
   final public static String    SHELL      =System.getenv("SHELL");                                                   // get the used shell
   final public static String    BACKTICKS  ="```";                                                                    // comments in manjaro forum
   final public static String    DETAILS0   ="[details=\"maxi";
   final public static String    DETAILS1   ="\"]";
   final public static String    DETAILS2   ="[/details]";
   static final ClipboardSupport clipSupport=new ClipboardSupport();                                                   // suport the clipboard when
                                                                                                                       // used in GUI
   static final Flag             COLOR      =new Flag('c', "color", "colorize output unconditionally");
   // static final Flag DATES=new Flag('d',"dates");
   static final Flag             EFI        =new Flag('e', "efi", "efi bootloaders");
   static final Flag             FORUM      =new Flag('f', "forum",
            "frame with backticks and [details] and copy to clipboard");
   static final Flag             GRUB       =new Flag('g', "grub", "/boot/grub/grub.cfg, /etc/default/grub");
   static final Flag             HELP       =new Flag('h', "help", "print this page");                                 //
   static final Flag             MKINITCPIO =new Flag('i', "mkinitcpio", "/etc/mkinitcpio.conf");
   static final Flag             KERNEL     =new Flag('k', "kernel", "installed kernels, initrd, chroot");
   static final Flag             LIST_ALL   =new Flag('l', "list_all", "all kernels (not only installed)");
   static final Flag             MODULES    =new Flag('m', "modules", "list modules and extramodules");
   static final Flag             PARTITIONS =new Flag('p', "partitions", "info about visible partitions");
   static final Flag             SHASUM     =new Flag('s', "shasum", "produce short hash to compare kernel & modules");
   // static final Flag USAGE =new Flag('u', "usage","print this page");
   static final Flag             KVER       =new Flag('v', "kver", "kernelversion (includes -k)");
   static final Flag             WATCH      =new Flag('w', "watch", "watch how everything changes over time", "100");
   static final Flag             LISTONEXIT =new Flag('x', "listonexit", "internal use");                                        // intern
   static final Flag             ZSH        =new Flag('z', "zsh", "internal use");
   static final String           VERSION    ="maxi v0.7.22 (03.06.2025) ";
   public Maxi() {}
   /**
    * Das Hauptprogramm das die Parameter annimmt
    * 
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
      Flag.setArgs(args, "-km");// -km -efgikmpsv
      clipSupport.println(VERSION);
      if (HELP.get()) {
         for (String line:Flag.getUsage(HELP, COLOR, KERNEL, LIST_ALL, KVER, MODULES, SHASUM, WATCH, GRUB, MKINITCPIO,
                  EFI, FORUM, PARTITIONS))
            System.out.println(line);
         System.exit(9);
      }
      // if (USAGE.get())
      // System.exit(8);
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
    * 
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
            final int i=Integer.parseInt(s);
            if ((i > 0) && (i <= 60000))
               pause=i;
         } catch (final NumberFormatException e1) {
            System.exit(10);
         }
         LISTONEXIT.set(true);
         clipSupport.println(KernelInfo.getHeader());
         clipSupport.printonly("will run until ^c is pressed");
         k_aktuell=KernelInfo.analyseStream().collect(Collectors.toList());// .toList();
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
            k_aktuell=KernelInfo.analyseStream().collect(Collectors.toList());// .toList();
            final ArrayList<KernelInfo> dif=new ArrayList<>();
            kein_dif: for (final KernelInfo a:k_aktuell) {
               final String b=a.toString().replaceAll(" ", "");
               for (final KernelInfo c:last)
                  if (c.toString().replaceAll(" ", "").compareTo(b) == 0)
                     continue kein_dif;
               dif.add(a);
            }
            if (!dif.isEmpty()) {
               final long milli=Duration.between(startZeitpunkt, Instant.now()).toMillis();
               final String z=String.format("%2d:%02d.%03d", milli / 60000L, (milli / 1000L) % 60, milli % 1000);
               for (final KernelInfo kernelInfo:dif)
                  clipSupport.println(z + kernelInfo);
            }
         } while (true);
      }
      clipSupport.println(KernelInfo.getHeader());
      if (KERNEL.get() || LIST_ALL.get())
         KernelInfo.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(clipSupport::println);
      // Gib dieaktuelle analyse aus
      if (MODULES.get()) {
         clipSupport.println(ModuleInfo.getHeader());
         ModuleInfo.analyseStream()// .map(p -> { slipboardSupport.print(p.getInfo() + " "); return p; })
                  .collect(Collectors.toList())/* .toList() */.forEach(clipSupport::println);
      }
      if (GRUB.get()) {
         clipSupport.println(GrubInfo.getHeader());
         GrubInfo.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(clipSupport::println);
      }
      if (MKINITCPIO.get()) {
         clipSupport.println(MkinitcpioInfo.getHeader());
         MkinitcpioInfo.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(clipSupport::println);
      }
      if (EFI.get()) {
         clipSupport.println(EfiInfo.getHeader());
         EfiInfo.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(clipSupport::println);
         clipSupport.println(EfiVars.getHeader());
         EfiVars.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(clipSupport::println);
      }
      if (PARTITIONS.get()) {
         clipSupport.println(Partition.getHeader());
         Partition.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(clipSupport::println);
      }
      if (FORUM.get()) {
         clipSupport.clipln(BACKTICKS);
         clipSupport.clipln(DETAILS2);
         clipSupport.transfer();
      }
   }
}

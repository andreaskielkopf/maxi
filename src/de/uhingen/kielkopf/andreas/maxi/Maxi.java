package de.uhingen.kielkopf.andreas.maxi;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.uhingen.kielkopf.andreas.beans.cli.Flag;

/**
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
 * 
 * @author Andreas Kielkopf ©2022, 2024, 2025
 * @version GNU General Public License v3.0
 * @since 18.4.2022, 16.12.2024, 15.6.2025
 */
public class Maxi {
   private static List<KernelInfo> k_aktuell =null;
   private static List<KernelInfo> last      =null;
   /** Ermittle die aktive SHELL */
   final public static String    SHELL      =System.getenv("SHELL");                                                   // get the used shell
   /** {@value} */
   final public static String    BACKTICKS  ="```";                                                                    // comments in manjaro forum
   /** {@value} */
   final public static String    DETAILS0   ="[details=\"maxi";
   /** {@value} */
   final public static String    DETAILS1   ="\"]";
   /** {@value} */
   final public static String    DETAILS2   ="[/details]";
   /** Flag('c', "use_color", "colorize output unconditionally") */
   static final Flag               COLOR     =new Flag('c', "use_color", "colorize output unconditionally");
   /** Flag('e', "efi", "efi bootloaders") */
   static final Flag             EFI        =new Flag('e', "efi", "efi bootloaders");
   /** Flag('f', "forum", "copy with backticks and [details] to clipboard") */
   static final Flag               FORUM     =new Flag('f', "forum", "copy with backticks and [details] to clipboard");
   /** Flag('g', "grub", "/boot/grub/grub.cfg, /etc/default/grub") */
   static final Flag             GRUB       =new Flag('g', "grub", "/boot/grub/grub.cfg, /etc/default/grub");
   /** Flag('h', "help", "print this page") */
   static final Flag               HELP      =new Flag('h', "help", "print this page");
   /** Flag('i', "mkinitcpio", "/etc/mkinitcpio.conf") */
   static final Flag             MKINITCPIO =new Flag('i', "mkinitcpio", "/etc/mkinitcpio.conf");
   /** Flag('k', "kernel", "installed kernels, initrd, chroot") */
   static final Flag             KERNEL     =new Flag('k', "kernel", "installed kernels, initrd, chroot");
   /** Flag('l', "list_all", "all kernels (not only installed)") */
   static final Flag             LIST_ALL   =new Flag('l', "list_all", "all kernels (not only installed)");
   /** Flag('m', "modules", "list modules and extramodules") */
   static final Flag             MODULES    =new Flag('m', "modules", "list modules and extramodules");
   /** Flag('p', "partitions", "info about visible partitions") */
   static final Flag             PARTITIONS =new Flag('p', "partitions", "info about visible partitions");
   /** Flag('s', "shasum", "produce short hash to compare kernel &amp; modules") */
   static final Flag             SHASUM     =new Flag('s', "shasum", "produce short hash to compare kernel & modules");
   /** Flag('v', "kver", "kernelversion (includes -k)") */
   static final Flag             KVER       =new Flag('v', "kver", "kernelversion (includes -k)");
   /** Flag('w', "watch", "watch how everything changes over time", "100") */
   static final Flag             WATCH      =new Flag('w', "watch", "watch how everything changes over time", "100");
   /** Flag('x', "listonexit", "internal use") */
   static final Flag               LISTONEXIT=new Flag('x', "listonexit", "internal use");
   /** Flag('z', "zsh", "internal use") */
   static final Flag             ZSH        =new Flag('z', "zsh", "internal use");
   /** {@value} */
   static final String             VERSION   ="maxi v0.7.27 (15.06.2025) for java 8";
   /**
    * Konstruktor ungenutzt weil alles statisch abläuft
    */
   public Maxi() {}
   /**
    * Das Hauptprogramm das die Parameter annimmt
    * 
    * @param args
    *           von der Commandline
    */
   public static void main(String[] args) {
      // Beim Shutdown soll noch folgendes erledigt werden (z.B. nach -w)
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         if (!HELP.get() && LISTONEXIT.get()) {
            ClipboardSupport.println(); // Kernelinfo ausgeben
            if (k_aktuell != null)
               for (final KernelInfo kernelInfo1:k_aktuell)
                  ClipboardSupport.println(kernelInfo1);
            else
               if (last != null)
                  for (final KernelInfo kernelInfo2:last)
                     ClipboardSupport.println(kernelInfo2);
            if (FORUM.get()) { // für forum formatieren und ans clipboard übergeben
               ClipboardSupport.clipln(BACKTICKS);
               ClipboardSupport.clipln(DETAILS2);
               ClipboardSupport.transfer();
            }
         }
      }));
      ZSH.set(SHELL.contains("zsh"));
      Flag.setArgs(args, "-km");// -km -efgikmpsv
      ClipboardSupport.println(VERSION);
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
         ClipboardSupport.clipln(DETAILS0 + Flag.getArgs() + DETAILS1);
         ClipboardSupport.clipln(BACKTICKS);
      }
      if (WATCH.get() && SHASUM.get()) {
         SHASUM.set(false);
         final StringBuilder sb=new StringBuilder();
         if (COLOR.get())
            sb.append(InfoLine.RED);
         sb.append("When --watch is enabled, checksumming is disabled");
         if (COLOR.get())
            sb.append(InfoLine.RESET);
         ClipboardSupport.println(sb.toString());
      }
      start();
   }
   /**
    * Gib die 2-dimensionale Liste von Strings aus mit Zeilenvorschub nur in einer dimension
    * 
    * @param f
    *           Liste mit anzuzeigenden Zeilen
    */
   static void show(List<Iterable<String>> f) {
      for (final Iterable<String> list:f) {
         for (final String s:list)
            ClipboardSupport.print(s);
         ClipboardSupport.println();
      }
   }
   /**
    * Hauptprogramm
    */
   static void start() {
      // können wir farbig ausgeben ?
      for (final List<String> list:(ZSH.get() ? Query.TERMINFO : Query.TPUT).getLists(Pattern.compile("([0-9])")))
         if (!list.isEmpty())
            COLOR.set(true);
      InfoLine.use_color=COLOR.get();
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
         ClipboardSupport.println(KernelInfo.getHeader());
         ClipboardSupport.printonly("will run until ^c is pressed");
         k_aktuell=KernelInfo.analyseStream().collect(Collectors.toList());// .toList();
         for (final KernelInfo kernelInfo:k_aktuell)
            ClipboardSupport.println("  : 0.   " + kernelInfo); // Gib die aktuelle analyse aus
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
                  ClipboardSupport.println(z + kernelInfo);
            }
         } while (true);
      }
      ClipboardSupport.println(KernelInfo.getHeader());
      if (KERNEL.get() || LIST_ALL.get())
         KernelInfo.analyseStream().collect(Collectors.toList())/* .toList() */.forEach(ClipboardSupport::println);
      // Gib dieaktuelle analyse aus
      if (MODULES.get()) {
         ClipboardSupport.println(ModuleInfo.getHeader());
         ClipboardSupport.println(ModuleInfo.analyseStream());
      }
      if (GRUB.get()) {
         ClipboardSupport.println(GrubInfo.getHeader());
         ClipboardSupport.println(GrubInfo.analyseStream());
      }
      if (MKINITCPIO.get()) {
         ClipboardSupport.println(MkinitcpioInfo.getHeader());
         ClipboardSupport.println(MkinitcpioInfo.analyseStream());
      }
      if (EFI.get()) {
         ClipboardSupport.println(EfiInfo.getHeader());
         ClipboardSupport.println(EfiInfo.analyseStream());
         ClipboardSupport.println(EfiVars.getHeader());
         ClipboardSupport.println(EfiVars.analyseStream());
      }
      if (PARTITIONS.get()) {
         ClipboardSupport.println(Partition.getHeader());
         ClipboardSupport.println(Partition.analyseStream());
      }
      if (FORUM.get()) {
         ClipboardSupport.clipln(BACKTICKS);
         ClipboardSupport.clipln(DETAILS2);
         ClipboardSupport.transfer();
      }
   }
}

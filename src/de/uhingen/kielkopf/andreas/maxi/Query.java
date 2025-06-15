package de.uhingen.kielkopf.andreas.maxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

/**
 * Definition von Abfragen mit eingebautem Caching
 *
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public enum Query {
   /** Liste die Dateien mit der Endung .kver in /boot */
   CAT_KVER(Maxi.SHELL, "-c", "cat /boot/*.kver"),
   /** Versuche harrauszufinden ob wir in chroot sind */
   CHROOT(Maxi.SHELL, "-c", "cat /proc/1/mountinfo"),
   /** Liste alle Module in /lib/modules */
   DU_MODULES(Maxi.SHELL, "-c", "du -sh /lib/modules/*"),
   /** Hole die EFI-Variablen mit efibootmgr */
   EFI_VAR(Maxi.SHELL, "-c", "efibootmgr -v"),
   /** Lies /etc/default/grub und grub.d/* */
   GRUB(Maxi.SHELL, "-c", "cat /etc/default/grub.d/*.cfg|cat /etc/default/grub -"),
   // , ZSHA_MODULES("zsh", "-c", "LC_ALL=C;for K in $(print -o /lib/modules/*(/));"//
   // +"do echo -n \"$K \";for D in $(print -l $K/**/*(.)|sort);do cat $D;done|sha256sum; done;"),
   /** Liste der Verzeichnisse /boot, /boot/grub und /lib/modules */
   LS("ls", "-sh1", "/boot", "/boot/grub", "/lib/modules"),
   // LS_EFI(Maxi.SHELL, "-c", "for F in $(sudo find /efi /boot -iname \"*.efi\"); do sudo ls -sh1 $F ;done"),
   /** grub-probe */
   GRUB_PROBE(Maxi.SHELL, "-c", "grub-probe -t efi_hints /boot/grub"),
   /**
    * Berechne Prüfsummen über alle EFI-Dateien
    * 
    * <pre>
    * Suche mit sudo nach *.efi-dateien
    * berechne sha256 von 50kByte und entferne den zeilenumbruch
    * berechne dateigrösse und namen und entferne den zeilenumbruch
    * Hänge den text -unknown- an den Inhalt der datei und
    * wähle die Kennung in diesem Stream zwischen grub,refind,shell,memtest86 oder -unknown-
    * füge das Ergebnis als Schlusszeile in die pipe ein
    * </pre>
    */
   GRS_EFI(Maxi.SHELL, "-c", "for F in $( find /efi /boot -iname '*.efi');do "//
            + "dd if=$F bs=1024 count=50 | sha256sum |grep -Eo '[0-f]{64}'|tr '\\n' ' ';"// 
            + "ls -sh1 $F|tr '\\n' ' ';"// 
            + "echo \"-unknown-\"|cat $F -|"// 
            + "grep -Eiao --max-count=1 '[@a-z/A-Z0-9(),]*/grub|refind,0.{6}|shell/RELEASE|load memtest86 |-unknown-';"// erkenne den typ
            + "done"),
   /** fest definierte Liste der Partitionen */
   LSBLK(Maxi.SHELL, "-c",
            "lsblk -o partuuid,kname,pttype,type,fstype,size,parttypename,uuid"
                     /* +",mountpoint" */ + ",partlabel,label"),
   // BLKID(Maxi.SHELL, "-c", "blkid"),
   // , ZLS(SHELL, "-c", "print -l /boot/*(.) /boot/grub/*(.) /lib/modules/*(/)")
   /** Liste der verfügbaren kernel */
   MHWD_L("mhwd-kernel", "-l"),
   /** Liste der installierten Kernel */
   MHWD_LI("mhwd-kernel", "-li"),
   /** Liste /etc/mkinitcpio.conf */
   MKINITCPIO(Maxi.SHELL, "-c", "cat /etc/mkinitcpio.conf"),
   /** liste die aktuellen mounts */
   MOUNTS(Maxi.SHELL, "-c", "cat /proc/mounts"),
   /** Berechne die Prüfsummen der ramdisks */
   SHA_BOOT(Maxi.SHELL, "-c", "sha256sum /boot/*fallback* /boot/vmlinuz*"),
   // SHA_EFI(Maxi.SHELL, "-c", "for F in $(sudo find /efi /boot -iname \"*.efi\"); do sudo sha256sum $F ;done"),
   // , SHA_M_VMLINUZ(Maxi.SHELL, "-c", "sha256sum /lib/modules/*/vmlinuz")
   // SHA_MODULES(Maxi.SHELL, "-c",
   // "LC_ALL=C;for K in $(find /lib/modules/* -maxdepth 0 -type d|sort);"
   // + "do cd $K;echo -n \"$K \";for D in $(find . -type f|sort);do cat $D;done|sha256sum; done"),
   // SHA_MODULES(Maxi.SHELL, "-c", "LC_ALL=C;find /lib/modules/* -maxdepth 0 -type d|sort|parallel -j$(nproc) "//
   // + "'cd {};echo -n \\\"{} \\\";find . -type f|sort|xargs cat|sha256sum'"),
   /** Berechne Prüfsummen rekursiv über alle Module */
   SHA_MODULES(Maxi.SHELL, "-c",
            "LC_ALL=C; find /lib/modules/* -maxdepth 0 -type d | sort | parallel -j$(nproc) 'cd {};"
                     + " echo -n \"{} \"; find . -type f | sort | xargs cat | sha256sum'"),
   /** Sind Farben erlaubt ? */
   TERMINFO(Maxi.SHELL, "-c", "echoti colors"),
   /** Sind Farben erlaubt ? */
   TPUT("tput", "colors");
   final static private List<String>   EMPTY    =new ArrayList<>();
   final static private ProcessBuilder pb       =new ProcessBuilder();
   final static private List<String>   TEST_OK  =Arrays.asList(new String[] {"OK"});
   // /* private */ List<String> cache =null; // new ArrayList<String>();
   final private String[]              cmd;
   /* private */ boolean               hasResult=false;
   private List<String>                qCache   =null;                              // new ArrayList<String>();
   /**
    * Definiert die Abfrage auf der Kommandozeile
    *
    * @param command
    */
   Query(String... command) {
      cmd=command;
   }
   // @Override
   // public String toString() {
   // return String.join("', '", cmd);
   // }
   /**
    * Vergleicht das Datum der letzten Änderung verschiedener Dateien
    * 
    * @param test
    * @return TEST_OK / null
    */
   public static List<String> test(String[] test) {
      final ArrayList<String> command=new ArrayList<>();
      if (Maxi.SHELL.contains("zsh"))
         command.add(Maxi.SHELL);
      else
         command.add("/bin/bash");
      command.add("-c");
      command.add("[[ " + test[0] + " -nt " + test[1] + " ]] && echo 't' || echo 'f'");
      try {
         final Process p=pb.command(command).redirectErrorStream(true).start();
         final InputStreamReader ir=new InputStreamReader(p.getInputStream());
         // p.waitFor(1, TimeUnit.SECONDS);
         try (BufferedReader br=new BufferedReader(ir); Stream<String> li=br.lines()) {
            final List<String> erg=li.collect(Collectors.toList())/* .toList() */;
            for (final String s:erg) {
               if (s.startsWith("t"))
                  return TEST_OK;
               if (s.startsWith("f"))
                  return Arrays.asList(test);
            }
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }
      return EMPTY;
   }
   /** Lösche die vorhandenen Informationen im cache */
   public void clear() {
      // cache=null;
      hasResult=false;
      qCache=null;
   }
   /**
    * Gibt alle Zeilen zurück, die zum Pattern passen
    * 
    * @param pa
    *           Pattern
    * @return liste
    */
   public List<List<String>> getLists(Pattern pa) {
      return getSelected(pa).collect(Collectors.toList());
   }
   /**
    * Liefert selektierte Daten aus dem cache oder aus einer neuen Abfrage
    *
    * @param pa
    *           Pattern
    * @return stream
    */
   public Stream<List<String>> getSelected(Pattern pa) {
      return query().stream().map(s -> pa.matcher(s)).filter(Matcher::find).map(IterableMatchResult::new)
               .map(i -> i.stream().collect(Collectors.toList()));
      // .collect(Collectors.toList()).stream();
   }
   /**
    * macht die Abfrage und füllt den cache
    *
    * @return abfrage
    */
   public List<String> query() {
      if (qCache == null) // Allererster Durchlauf -> Abfrage machen
         try {
            final Process p=pb.command(cmd).redirectErrorStream(true).start();
            final InputStreamReader ir=new InputStreamReader(p.getInputStream());
            try (BufferedReader br=new BufferedReader(ir); Stream<String> li=br.lines()) {
               hasResult=true;
               qCache=li.collect(Collectors.toList());
            }
         } catch (final IOException e) {
            qCache=new ArrayList<>();
         }
      return qCache;
   }
}

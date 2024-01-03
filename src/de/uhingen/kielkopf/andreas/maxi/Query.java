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
   CAT_KVER(Maxi.SHELL, "-c", "cat /boot/*.kver"),
   CHROOT(Maxi.SHELL, "-c", "cat /proc/1/mountinfo"),
   DU_MODULES(Maxi.SHELL, "-c", "du -sh /lib/modules/*"),
   EFI_VAR(Maxi.SHELL, "-c", "efibootmgr -v"),
   GRUB(Maxi.SHELL, "-c", "cat /etc/default/grub.d/*.cfg|cat /etc/default/grub -"),
   // , ZSHA_MODULES("zsh", "-c", "LC_ALL=C;for K in $(print -o /lib/modules/*(/));"//
   // +"do echo -n \"$K \";for D in $(print -l $K/**/*(.)|sort);do cat $D;done|sha256sum; done;"),
   LS("ls", "-sh1", "/boot", "/boot/grub", "/lib/modules"),
   // LS_EFI(Maxi.SHELL, "-c", "for F in $(sudo find /efi /boot -iname \"*.efi\"); do sudo ls -sh1 $F ;done"),
   GRS_EFI(Maxi.SHELL, "-c", "for F in $( find /efi /boot -iname '*.efi');do " // suche mit sudo nach *.efi-dateien
            + "sha256sum $F|grep -Eo '[0-f]{64}'|tr '\\n' ' ';" // berechne sha256 und entferne den zeilenumbruch
            + "ls -sh1 $F|tr '\\n' ' ';" // berechne dateigrösse und namen und entferne den zeilenumbruch
            + "echo \"-unknown-\"|cat $F -|" // füge eine Schlusszeile in die pipe ein
            + "grep -Eiao --max-count=1 '[@a-z/A-Z]*/grub|refind,0.{6}|shell/RELEASE|load memtest86 |-unknown-';"// erkenne den typ
            + "done"),
   // GR_EFI(Maxi.SHELL, "-c", "for F in $(find /efi /boot -iname '*.efi');"
   // + "do sha256sum $F|grep -Eo '[0-f]{64}'|tr '\\n' ' ';" + "ls -sh1 $F|tr '\\n' ' ';"
   // + "echo \"-unknown-\"|cat $F -|"
   // + "grep -Eiao --max-count=1 '@/boot/grub|refind,0.{5}|shell/RELEASE|load memtest86 |-unknown-'; done"),
   LSBLK(Maxi.SHELL, "-c",
            "lsblk -o partuuid,kname,pttype,type,fstype,size,parttypename,uuid"
                     /* +",mountpoint" */ + ",partlabel,label"),
   // LSBLK(Maxi.SHELL, "-c", "lsblk -o kname,pttype,ptuuid,parttypename,partuuid,partlabel,fstype,uuid,label"),
   // BLKID(Maxi.SHELL, "-c", "blkid"),
   // , ZLS(SHELL, "-c", "print -l /boot/*(.) /boot/grub/*(.) /lib/modules/*(/)")
   MHWD_L("mhwd-kernel", "-l"),
   MHWD_LI("mhwd-kernel", "-li"),
   MKINITCPIO(Maxi.SHELL, "-c", "cat /etc/mkinitcpio.conf"),
   SHA_BOOT(Maxi.SHELL, "-c", "sha256sum /boot/*fallback* /boot/vmlinuz*"),
   // SHA_EFI(Maxi.SHELL, "-c", "for F in $(sudo find /efi /boot -iname \"*.efi\"); do sudo sha256sum $F ;done"),
   // , SHA_M_VMLINUZ(Maxi.SHELL, "-c", "sha256sum /lib/modules/*/vmlinuz")
   // SHA_MODULES(Maxi.SHELL, "-c",
   // "LC_ALL=C;for K in $(find /lib/modules/* -maxdepth 0 -type d|sort);"
   // + "do cd $K;echo -n \"$K \";for D in $(find . -type f|sort);do cat $D;done|sha256sum; done"),
   // SHA_MODULES(Maxi.SHELL, "-c", "LC_ALL=C;find /lib/modules/* -maxdepth 0 -type d|sort|parallel -j$(nproc) "//
   // + "'cd {};echo -n \\\"{} \\\";find . -type f|sort|xargs cat|sha256sum'"),
   SHA_MODULES(Maxi.SHELL, "-c",
            "LC_ALL=C; find /lib/modules/* -maxdepth 0 -type d | sort | parallel -j$(nproc) 'cd {};"
                     + " echo -n \"{} \"; find . -type f | sort | xargs cat | sha256sum'"),
   TERMINFO(Maxi.SHELL, "-c", "echoti colors"),
   TPUT("tput", "colors");
   static private List<String> EMPTY    =new ArrayList<>();
   static final ProcessBuilder pb       =new ProcessBuilder();
   static private List<String> TEST_OK  =Arrays.asList(new String[] {"OK"});
   private List<String>        cache    =null;                              // new ArrayList<String>();
   private final String[]      cmd;
   private boolean             hasResult=false;
   /**
    * Definiert die Abfrage auf der Kommandozeile
    *
    * @param command
    */
   Query(String... command) {
      cmd=command;
   }
   /**
    *
    * @param test
    * @return
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
   public void clear() {
      cache=null;
      hasResult=false;
   }
   public List<List<String>> getLists(Pattern pa) {
      return getSelected(pa).collect(Collectors.toList());// toList();
   }
   public List<List<String>> getLists_C(Pattern pa) {
      return getSelected_C(pa).collect(Collectors.toList());// toList();
   }
   /**
    * liefert selektierte Daten aus dem cache
    *
    * @param pa
    * @return
    */
   public Stream<List<String>> getSelected(Pattern pa) {
      if (!hasResult)
         cache=query();
      List<List<String>> q=cache.stream().map(s -> pa.matcher(s)).filter(Matcher::find).map(IterableMatchResult::new)
               .map(i -> i.stream().collect(Collectors.toList())).collect(Collectors.toList());
      // filter(Matcher::find).
      // map(IterableMatchResult::new)
      // .map(i -> i.stream().collect(Collectors.toList()) .toList());
      return q.stream();
      // return cache.stream().map(s -> pa.matcher(s)).filter(Matcher::find).map(IterableMatchResult::new)
      // .map(i -> i.stream().collect(Collectors.toList()));// .toList());
   }
   public Stream<List<String>> getSelected_C(Pattern pa) {
      if (!hasResult) {
         StringBuilder offen=new StringBuilder();
         cache=new ArrayList<>();
         for (String s:query()) {
            if (offen.length() == 0)
               if (s.contains("=(") && !s.startsWith("#") && !s.contains(")"))
                  offen.append(s);
               else
                  cache.add(s);
            else {
               offen.append(" ").append(s);
               if (s.contains(")")) {
                  cache.add(offen.toString());
                  offen.setLength(0);
               }
            }
         }
      }
      List<List<String>> q=cache.stream().map(s -> pa.matcher(s)).filter(Matcher::find).map(IterableMatchResult::new)
               .map(i -> i.stream().collect(Collectors.toList())).collect(Collectors.toList());
      return q.stream();
   }
   /**
    * macht die Abfrage und füllt den cache
    *
    * @return
    */
   private List<String> query() {
      try {
         final Process p=pb.command(cmd).redirectErrorStream(true).start();
         final InputStreamReader ir=new InputStreamReader(p.getInputStream());
         try (BufferedReader br=new BufferedReader(ir); Stream<String> li=br.lines()) {
            hasResult=true;
            return li.collect(Collectors.toList());// return li.toList();
         }
      } catch (final IOException e) {
         return new ArrayList<>();
      }
   }
}

package de.uhingen.kielkopf.andreas.maxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Definition von Abfragen mit eingebautem Caching
 * 
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public enum Query {
   CAT_KVER(Maxi.SHELL, "-c", "cat /boot/*.kver"),
   DU_MODULES(Maxi.SHELL, "-c", "du -sh /lib/modules/*"),
   GRUB(Maxi.SHELL, "-c", "cat /etc/default/grub"),
   // , ZSHA_MODULES("zsh", "-c", "LC_ALL=C;for K in $(print -o /lib/modules/*(/));"//
   // +"do echo -n \"$K \";for D in $(print -l $K/**/*(.)|sort);do cat $D;done|sha256sum; done;"),
   LS("ls", "-sh1", "/boot", "/boot/grub", "/lib/modules"),
   LS_EFI(Maxi.SHELL, "-c", "for F in $(find /boot -iname \"*.efi\"); do ls -sh1 $F ;done"),
   // , ZLS(SHELL, "-c", "print -l /boot/*(.) /boot/grub/*(.) /lib/modules/*(/)")
   MHWD_L("mhwd-kernel", "-l"),
   MHWD_LI("mhwd-kernel", "-li"),
   MKINITCPIO(Maxi.SHELL, "-c", "cat /etc/mkinitcpio.conf"),
   SHA_BOOT(Maxi.SHELL, "-c", "sha256sum /boot/*fallback* /boot/vmlinuz*"),
   // , SHA_M_VMLINUZ(Maxi.SHELL, "-c", "sha256sum /lib/modules/*/vmlinuz")
   SHA_MODULES(Maxi.SHELL, "-c",
            "LC_ALL=C;for K in $(find /lib/modules/* -maxdepth 0 -type d|sort);"
                     + "do cd $K;echo -n \"$K \";for D in $(find . -type f|sort);do cat $D;done|sha256sum; done"),
   TERMINFO(Maxi.SHELL, "-c", "echoti colors"),
   TPUT("tput", "colors");
   private static List<String> EMPTY    =new ArrayList<String>();
   final static ProcessBuilder pb       =new ProcessBuilder();
   private static List<String> TEST_OK  =Arrays.asList(new String[] {"OK"});
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
      ArrayList<String> command=new ArrayList<>();
      command.add(Maxi.SHELL);
      command.add("-c");
      command.add("[[ " + test[0] + " -nt " + test[1] + " ]] && echo 't' || echo 'f'");
      try {
         Process           p =pb.command(command).redirectErrorStream(true).start();
         InputStreamReader ir=new InputStreamReader(p.getInputStream());
         // p.waitFor(1, TimeUnit.SECONDS);
         try (BufferedReader br=new BufferedReader(ir); Stream<String> li=br.lines()) {
            List<String> erg=li.collect(Collectors.toList());
            for (String s:erg) {
               if (s.startsWith("t"))
                  return TEST_OK;
               if (s.startsWith("f"))
                  return Arrays.asList(test);
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return EMPTY;
   }
   public void clear() {
      cache=null;
      hasResult=false;
   }
   public List<List<String>> getLists(Pattern pa) {
      return getSelected(pa).collect(Collectors.toList());
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
      return cache.stream().map(s -> pa.matcher(s)).filter(m -> m.find()).map(m -> new IterableMatchResult(m))
               .map(i -> i.stream().collect(Collectors.toList()));
   }
   /**
    * macht die Abfrage und füllt den cache
    * 
    * @return
    */
   private List<String> query() {
      try {
         // System.out.println(name());
         Process           p =pb.command(cmd).redirectErrorStream(true).start();
         InputStreamReader ir=new InputStreamReader(p.getInputStream());
         try (BufferedReader br=new BufferedReader(ir); Stream<String> li=br.lines()) {
            hasResult=true;
            return li.collect(Collectors.toList());
         }
      } catch (IOException e) {
         return new ArrayList<String>();
      }
   }
}

package de.uhingen.kielkopf.andreas.maxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
   LS("ls", "-sh1", "/boot", "/boot/grub", "/lib/modules /etc/default/grub"),
   // , ZLS(SHELL, "-c", "print -l /boot/*(.) /boot/grub/*(.) /lib/modules/*(/)")
   MHWD_L("mhwd-kernel", "-l"),
   MHWD_LI("mhwd-kernel", "-li"),
   SHA_BOOT(Maxi.SHELL, "-c", "sha256sum /boot/*fallback* /boot/vmlinuz*"),
   // , SHA_M_VMLINUZ(Maxi.SHELL, "-c", "sha256sum /lib/modules/*/vmlinuz")
   SHA_MODULES(Maxi.SHELL, "-c",
            "LC_ALL=C;for K in $(find /lib/modules/* -maxdepth 0 -type d|sort);"
                     + "do cd $K;echo -n \"$K \";for D in $(find . -type f|sort);do cat $D;done|sha256sum; done"),
   TERMINFO(Maxi.SHELL, "-c", "echoti colors"),
   TPUT("tput", "colors");
   private static List<String> EMPTY  =new ArrayList<String>();
   final static ProcessBuilder pb     =new ProcessBuilder();
   private static List<String> TEST_OK=Arrays.asList(new String[] {"OK"});
   private final String[]      cmd;
   private List<String>        result =null;
   /**
    * Definiert die Abfrage auf der Kommandozeile
    * 
    * @param command
    */
   Query(String... command) {
      cmd=command;
   }
   public static List<String> test(String[] test) {
      ArrayList<String> command=new ArrayList<>();
      command.add(Maxi.SHELL);
      command.add("-c");
      StringBuilder sb=new StringBuilder();
      sb.append("[[ ");
      sb.append(test[0]);
      sb.append(" -nt ");
      sb.append(test[1]);
      sb.append(" ]] && echo 't' || echo 'f'");
      command.add(sb.toString());
      try {
         Process           p =pb.command(command).redirectErrorStream(true).start();
         InputStreamReader ir=new InputStreamReader(p.getInputStream());
         p.waitFor(1, TimeUnit.SECONDS);
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
      } catch (InterruptedException e) {
         System.out.println("Timeout: ");
         e.printStackTrace();
      }
      return EMPTY;
   }
   public void clear() {
      result=null;
   }
   /*
    * static void showAll() { for (Query q:values()) { System.out.println(); System.out.print(q.name()+": "); for
    * (String c:q.cmd) System.out.print(c+" "); System.out.println(); q.evaluate(); for (String s:q.result)
    * System.out.println(s); } }
    */
   /**
    * Führt die Abfrage durch und speichert das Ergebnis im einer ArrayList für mehrfache Verwendung
    */
   private void evaluate() {
      try {
         Process           p =pb.command(cmd).redirectErrorStream(true).start();
         InputStreamReader ir=new InputStreamReader(p.getInputStream());
         p.waitFor(20, TimeUnit.SECONDS);
         try (BufferedReader br=new BufferedReader(ir); Stream<String> li=br.lines()) {
            result=li.collect(Collectors.toList());
         }
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InterruptedException e) {
         System.out.println("Timeout: ");
         e.printStackTrace();
      }
   }
   /**
    * Gibt eine Liste mit den per Pattern gefilterten Ergebnissen zurück.
    * 
    * Benutzt dazu den cache(result) Sofern das Pattern auf die Zeile zutrifft, wird die Zeile anhand des Pattern
    * zerlegt und der Matcher wird zurückgegeben
    * 
    * @param pa
    * @return
    */
   public List<IterableMatchResult> getIterable(Pattern pa) {
      if (result == null)
         evaluate();
      return result.stream().map(s -> pa.matcher(s)).filter(m -> m.find()).map(m -> new IterableMatchResult(m))
               .collect(Collectors.toList());
   }
   public List<String> getList(Pattern pa, String replace) {
      return getIterable(pa).stream().map(i -> i.replace(replace)).collect(Collectors.toList());
   }
   public List<List<String>> getLists(Pattern pa) {
      return getIterable(pa).stream()
               .map(i -> StreamSupport
                        .stream(Spliterators.spliteratorUnknownSize(i.iterator(), Spliterator.ORDERED), false)
                        .collect(Collectors.toList()))
               .collect(Collectors.toList());
   }
}

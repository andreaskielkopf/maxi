package de.uhingen.kielkopf.andreas.maxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Definition von Abfragen mit eingebautem Caching
 * 
 * @author andreas kielkopf
 */
public enum Query {
   LS("ls", "-lA", "/boot", "/boot/grub", "/lib/modules") //
   , TPUT("tput", "colors")
   // , ZLS(SHELL, "-c", "print -l /boot/*(.) /boot/grub/*(.) /lib/modules/*(/)") //
   , MHWD_L("mhwd-kernel", "-l") //
   , MHWD_LI("mhwd-kernel", "-li") //
   , CAT_KVER(Maxi.SHELL, "-c", "cat /boot/*.kver") //
   , DU_MODULES(Maxi.SHELL, "-c", "du -sh /lib/modules/*")//
   , SHA_VMLINUZ(Maxi.SHELL, "-c", "sha256sum /boot/*img /boot/vmlinuz*") //
   , SHA_M_VMLINUZ(Maxi.SHELL, "-c", "sha256sum /lib/modules/*/vmlinuz") //
   , SHA_MODULES(Maxi.SHELL, "-c", "LC_ALL=C;for K in $(find /lib/modules/* -maxdepth 0 -type d|sort);"
            +"do cd $K;echo -n \"$K \";for D in $(find . -type f|sort);do cat $D;done|sha256sum; done")//
   // , ZSHA_MODULES("zsh", "-c", "LC_ALL=C;for K in $(print -o /lib/modules/*(/));"//
   // +"do echo -n \"$K \";for D in $(print -l $K/**/*(.)|sort);do cat $D;done|sha256sum; done;")//
   //
   ;
   final static ProcessBuilder pb    =new ProcessBuilder();
   private final String[]      cmd;
   private List<String>        result=null;
   Query(String... command) {
      cmd=command;
   }
   static void showAll() {
      for (Query q:values()) { // if (q.name().contains("H")) break;
         System.out.println();
         System.out.print(q.name()+": ");
         for (String c:q.cmd)
            System.out.print(c+" ");
         System.out.println();
         q.evaluate();
         for (String s:q.result)
            System.out.println(s);
      }
   }
   void evaluate() {
      try {
         Process           p =pb.command(cmd).redirectErrorStream(true).start();
         InputStreamReader ir=new InputStreamReader(p.getInputStream());
         p.waitFor(5, TimeUnit.SECONDS);
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
   public List<IterableMatchResult> getIterable(Pattern pa) {
      if (result==null)
         evaluate();
      return result.stream().filter(s -> pa.matcher(s).find()).map(s -> toIterable(pa.matcher(s)))
               .collect(Collectors.toList());
   }
   public List<List<String>> getList(Pattern pa) {
      return getIterable(pa).stream()
               .map(i -> StreamSupport
                        .stream(Spliterators.spliteratorUnknownSize(i.iterator(), Spliterator.ORDERED), false)
                        .collect(Collectors.toList()))
               .collect(Collectors.toList());
   }
   public List<String> getList(Pattern pa, String replace) {
      return getIterable(pa).stream().map(i -> i.replace(replace)).collect(Collectors.toList());
   }
   private static IterableMatchResult toIterable(Matcher ma) {
      return ma.find() ? new IterableMatchResult(ma) : null;
   }
   public void clear() {
      result=null;
   }
}

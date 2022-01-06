package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InfoLine {
   final static String       GREEN    ="\u001b[0;32m";
   final static String       RED      ="\u001b[1;31m";
   final static String       WHITE    ="\u001b[0;97m";
   final static String       RESET    ="\u001b[0m";
   final static String       TAGD     ="(?:[1-3 ]?[0-9][.] )?";
   final static String       MONAT    ="[A-Z][a-z][a-z] ";
   final static String       TAGE     ="(?:[1-3 ]?[0-9] )?";
   final static String       REST     ="(?:[ 0-9][0-9]{3}[ 0-9]|[0-9:]{5})";
   final static String       DATE     ="("+TAGD+MONAT+TAGE+REST+").*";
   final static String       SIZE     ="^([ 0-9KMG,]{4})";
   final static List<String> MISSING  =Arrays.asList(new String[] {"<missing>", ""});
   final static List<String> MISSING_V=Arrays.asList(new String[] {"<vmlinuz missing>", ""});
   final static List<String> MISSING_I=Arrays.asList(new String[] {"<initrd missing>", ""});
   final static List<String> MISSING_F=Arrays.asList(new String[] {"fallback", "<no>"});
   final static List<String> EOL_TEST =Arrays.asList(new String[] {"linux51", "51"});
   final Iterable<String>    info;
   @SuppressWarnings("boxing")
   public InfoLine(Iterable<String> iterableInfo, ArrayList<Integer> spalten) {
      info=iterableInfo;
      int index=0;
      for (String text:iterableInfo) {
         if (index>=spalten.size())
            spalten.add(0);
         if (text.length()>spalten.get(index))
            spalten.set(index, text.length());
         index++;
      }
   }
   /**
    * 
    * @return one Line of the Info
    */
   public String getLine(Iterator<Integer> len) {
      StringBuilder sb     =new StringBuilder();
      boolean       noSpace=false;
      for (String text:info) {
         if (Flag.COLOR.get())
            if ((sb.length()==0)||(text.equals("="))||(text.endsWith(":")))
               sb.append(GREEN);// hervorgehobene Spalte
            else
               if (text.startsWith("<"))
                  sb.append(RED);// Fehler
               else
                  sb.append(WHITE);
         @SuppressWarnings("boxing")
         int width=len.next();
         if (!(noSpace||text.equals("=")||(width==0)))
            sb.append(" ");
         sb.append(text);
         for (; width>text.length(); width--)
            sb.append(" ");
         noSpace=(text.equals("=")||text.endsWith(":"));
      }
      while (' '==sb.charAt(sb.length()-1))
         sb.deleteCharAt(sb.length()-1);
      if (Flag.COLOR.get())
         sb.append(RESET);
      return sb.toString();
   }
   static void clear() {
      Query.MHWD_LI.clear();
   }
   /**
    * @return a list of kernels to search for
    */
   static Map<Predicate<String>, ArrayList<String>> getBasis() {
      List<List<String>>                        kernels=Flag.LIST_ALL.get()
               ? Query.MHWD_L.getList(Pattern.compile("[*].*(linux(.*))"))
               : Query.MHWD_LI.getList(Pattern.compile("[*].*(linux(.*))"));
      Map<Predicate<String>, ArrayList<String>> basis  =new LinkedHashMap<>();
      final String                              r      ="abcdef";
      for (List<String> k:kernels) {
         ArrayList<String> info  =k.stream().collect(Collectors.toCollection(ArrayList<String>::new));
         String            kNr   =info.remove(1);
         // * linux44 * linux515-rt
         String            search="linuxabcdef$"
                  // initramfs-4.4-x86_64.img initramfs-5.15-rt-x86_64-fallback.img
                  +"|^initr.m.s-a[.]bcdef[-.][^r].*img$"
                  // extramodules-4.4-MANJARO extramodules-5.15-rt-MANJAR
                  +"|^.xtr.mo.ul.s-a[.]bcdef-MANJARO$"
                  // cat kver -> 4.4.294-1-MANJARO x64 5.15.5-rt22-1-MANJARO x64
                  +"|^a[.]bc[.][-0-9]*def[-0-9]*MANJARO(?:[ 0-9]+[KM])?"
                  // vmlinuz-4.4-x86_64 vmlinuz-5.15-rt-x86_64
                  +"|^vmlinuz-a[.]bcdef-x86_64$"
                  // initramfs-5.15-x86_64-fallback.img
                  +"|^a[.]bcdef-x86_64-"
         //
         ;
         for (int i=0; i<r.length(); i++)
            search=search.replaceAll(""+r.charAt(i), (i<kNr.length() ? ""+kNr.charAt(i) : ""));
         // search becomes a pattern to identify this one kernel
         basis.put(Pattern.compile(search).asPredicate(), info);
      }
      return basis;
   }
   /**
    * 
    * @param lls
    * @param pr
    * @param success
    * @param error
    * @return
    */
   static String deepSearch(List<List<String>> lls, Predicate<String> pr, String success, String error) {
      Optional<String> erg=lls.stream().flatMap(ims -> ims.stream()).filter(pr).findAny();
      if (!erg.isPresent())
         return error;
      return success.replaceFirst("§", erg.get());
   }
   static List<String> search(List<List<String>> lls, Predicate<String> pr) {
      Optional<List<String>> erg=lls.stream().filter(s -> s.stream().anyMatch(pr)).findAny();
      if (!erg.isPresent())
         return new ArrayList<>();
      return erg.get();
   }
   static Iterable<String> select(Stream<List<String>> a, Predicate<String> pr) {
      return a.filter(l -> l.stream().anyMatch(pr)).map(s -> {
         Collections.reverse(s);
         return s;
      }).findAny().orElse(Arrays.asList(new String[] {"<missing>", ""}));
   }
   static Iterable<String> select(Stream<List<String>> a, Predicate<String> pr, List<String> missing) {
      return a.filter(l -> l.stream().anyMatch(pr)).map(s -> {
         Collections.reverse(s);
         return s;
      }).findAny().orElse(missing);
   }
}

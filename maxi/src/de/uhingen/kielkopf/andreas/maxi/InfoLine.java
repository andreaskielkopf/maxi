package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InfoLine {
   final static char      ESC     =27;
   final static String    green   =ESC+"[32m";
   final static String    red     =ESC+"[1;31m";
   final static String    white   =ESC+"[0;97m";
   final static String    reset   =ESC+"[0m";
   public static boolean  colorize=false;
   final static String    TAGD    ="(?:[1-3 ]?[0-9][.] )?";
   final static String    MONAT   ="[A-Z][a-z][a-z] ";
   final static String    TAGE    ="(?:[1-3 ]?[0-9] )?";
   final static String    REST    ="(?:[ 0-9][0-9]{3}[ 0-9]|[0-9:]{5})";
   final static String    DATE    ="("+TAGD+MONAT+TAGE+REST+").*";
   final Iterable<String> info;
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
      StringBuilder sb=new StringBuilder();
      for (String text:info) {
         if (colorize)
            if (sb.length()==0)
               sb.append(green);
            else
               if (text.endsWith(":"))
                  sb.append(green);
               else
                  if (text.startsWith("<"))
                     sb.append(red);
                  else
                     sb.append(white);
         sb.append(" ");
         sb.append(text);
         for (@SuppressWarnings("boxing")
         int i=len.next(); i>text.length(); i--)
            sb.append(" ");
      }
      while (' '==sb.charAt(sb.length()-1))
         sb.deleteCharAt(sb.length()-1);
      if (colorize)
         sb.append(reset);
      return sb.toString();
   }
   /**
    * @return a list of kernels to search for
    */
   static public Map<Predicate<String>, ArrayList<String>> getBasis() {
      List<List<String>>                        kernels=KernelInfo.listAll
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
                  +"|^initr.m.s-a[.]bcdef[-.].*img$"
                  // extramodules-4.4-MANJARO extramodules-5.15-rt-MANJAR
                  +"|^.xtr.mo.ul.s-a[.]bcdef-MANJARO$"
                  // cat kver -> 4.4.294-1-MANJARO x64 5.15.5-rt22-1-MANJARO x64
                  +"|^a[.]bc[.][-0-9]*def[-0-9]*MANJARO(?:[ 0-9]+[KM])?"
                  // vmlinuz-4.4-x86_64 vmlinuz-5.15-rt-x86_64
                  +"|^vmlinuz-a[.]bcdef-x86_64$";
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
   protected static String searchFor(List<List<String>> lls, Predicate<String> pr, String success, String error) {
      Optional<String> erg=lls.stream().flatMap(ims -> ims.stream()).filter(pr).findAny();
      if (!erg.isPresent())
         return error;
      return success.replaceFirst("§", erg.get());
   }
}

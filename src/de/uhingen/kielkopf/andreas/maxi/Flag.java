package de.uhingen.kielkopf.andreas.maxi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Definiert und behandet parameter die über die Kommandozeile übergeben werden
 *
 * @author Andreas Kielkopf ©2022
 * @license GNU General Public License v3.0
 */
public enum Flag {
   COLOR('c'),
   // DATES('d'),
   EFI('e'),
   FORUM('f'),
   GRUB('g'),
   HELP('h'),
   KERNEL('k'),
   KVER('v'),
   LIST_ALL('l'),
   LISTONEXIT('x'), // intern
   MKINITCPIO('i'),
   MODULES('m'),
   PARTITIONS('p'),
   SHASUM('s'),
   USAGE('u'),
   WATCH('w', "100"),
   ZSH('z'); // intern
   static String        arg      =null;
   static final char    KEIN_KURZ=0;
   final private char   c;
   private Boolean      flag     =null;
   final private String p;
   /**
    * Flag mit einem langen Namen
    *
    * @param kurzer
    *           name
    */
   Flag() {
      this(KEIN_KURZ, null);
   }
   /**
    * Flag mit einem langen und kurzen Namen
    *
    * @param kurzer
    *           name
    */
   Flag(char kurz) {
      this(kurz, null);
   }
   /**
    * Flag mit einem möglichen Parameter
    *
    * @param kurz
    * @param standard
    */
   Flag(char kurz, String standard) {
      c=kurz;
      p=standard;
   }
   /**
    * @param args
    *           vom System übergeben
    * @param standard_args
    *           (wenn keine anderen übergeben wurden)
    */
   static void setArgs(String[] args, String standard_args) {
      arg=String.join(" ", args);
      if (arg.isEmpty())
         arg=standard_args;
      arg=" " + arg + " ";
   }
   /**
    * Lazy auswertung der Flags (optimiert)
    *
    * @return boolean ist dieses Flag gesetzt
    */
   @SuppressWarnings("boxing")
   boolean get() {
      if (flag == null) {
         final String findLong=".* --" + name().toLowerCase().replaceAll("_", "-") + " .*";
         flag=arg.matches(findLong);
         if (c != KEIN_KURZ) {
            final String findShort=".* -[a-z]*" + c + "[a-z]* .*";
            flag|=arg.matches(findShort);
         }
      }
      return flag;
   }
   /**
    * Lazy auswertung der Parameter (nicht optimiert)
    *
    * @return
    */
   String getParameter() {
      if (p != null) {
         if (c != KEIN_KURZ) {
            final String  findShort=".* -[a-z]*" + c + " ([^- ]+).*";
            final Matcher ma       =Pattern.compile(findShort).matcher(arg);
            if (ma.find())
               return ma.group(1);
         }
         final String  findLong=".* --" + name().toLowerCase().replaceAll("_", "-") + " ([^-]+).*";
         final Matcher ma2     =Pattern.compile(findLong).matcher(arg);
         if (ma2.find())
            return ma2.group(1);
      }
      return p;
   }
   /**
    * Manuelles setzen oder löschen von Flags
    *
    * @param b
    */
   @SuppressWarnings("boxing")
   void set(boolean b) {
      flag=b;
   }
}

package de.uhingen.kielkopf.andreas.beans.cli;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Framework zur Handhabung von CLI-Flags unter linux (needs java 21)
 * 
 * <pre>
 *    Speichere Argumente die beim Programmstart übergeben werden und ermögliche die Analyse "on demand"
 * 
 *       * Flags beginnend mit - oder -! 
 *       * Gruppen von Flags beginnend mit - 
 *       * Flags beginnend mit -- (langform) 
 *       * Parameter für jedes Flag durch = vom Flag getrennt
 *       * Argumente durch Leerzeichen getrennt
 * </pre>
 * 
 * @author Andreas Kielkopf
 * @since 2025 06 01
 */
public class Flag {
   /** Liste aller definierten Flags */
   @Nonnull static private List<Flag> flagList     =new CopyOnWriteArrayList<>();
   /** Die beim Programmstart übergebenen Flags, Parameter und Argumente */
   @Nonnull static private String     args         =" ";
   /** Die beim Programmstart übergebenen Argumente */
   static private List<String>        argumentListe=null;
   /** Anzahl der Zeichen für den langen Namen der Flags reservieren */
   static private Integer             spalten      =null;
   /** Kurzname des Flag (Ein Buchstabe) -x oder -!x */
   @Nullable final private Character  kurz;
   /** Langname des Flag (Text ohne Leerzeichen oder Sonderzeichen) --extra oder --!extra */
   @Nullable final private String     lang;
   /** Parameter (sofern vorhanden) -x=hallo oder --extra=hallo --extra=200 */
   @Nullable private String           param;
   /** Usage-Text für dieses Flag */
   @Nullable final private String     usage;
   /** Ist dieses Flag vorhanden und gesetzt */
   private Boolean                    flag         =null;
   /** Defaultwert für dieses Flag */
   private boolean                    standard     =false;
   /** Flag nur mit langer Bezeichnung */
   // public Flag(String lang1, String usage1) {
   // this(null, lang1, null, usage1);
   // }
   /** Flag nur mit kurzer Bezeichnung */
   // public Flag(char kurz1, String usage1) {
   // this(Character.valueOf(kurz1), null, null, usage1);
   // }
   /**
    * Flag mit langer und kurzer Bezeichnung und Hilfe-text
    * 
    * @param kurz1
    *           Kurzname des Flag
    * @param lang1
    *           Langname des Flag
    * @param usage1
    *           Text der bei usage angezeigt werden soll
    */
   public Flag(char kurz1, String lang1, String usage1) {
      this(Character.valueOf(kurz1), lang1, null, usage1);
   }
   /**
    * Flag mit langer und kurzer Bezeichnung, Hilfe-Text und default Parameter
    * 
    * @param kurz1
    *           Kurzname des Flag
    * @param lang1
    *           Langname des Flag
    * @param parameter1
    *           default-parameter des Flag
    * @param usage1
    *           Text der bei usage angezeigt werden soll
    */
   public Flag(char kurz1, String lang1, String usage1, String parameter1) {
      this(Character.valueOf(kurz1), lang1, parameter1, usage1);
   }
   /**
    * Interner Konstruktor für ein Flag
    * 
    * @param kurz1
    *           Kurzname des Flag
    * @param lang1
    *           Langname des Flag
    * @param parameter1
    *           default-parameter des Flag
    * @param usage1
    *           Text der bei usage angezeigt werden soll
    */
   private Flag(@Nullable Character kurz1, @Nullable String lang1, @Nullable String parameter1,
            @Nullable String usage1) {
      kurz=(kurz1 != null && Character.isLetter(kurz1)) ? kurz1 : null;
      lang=(lang1 != null && lang1.matches("[a-zA-Z_]+")) ? lang1 : null;
      if (kurz == null && lang == null)
         throw new IllegalArgumentException("Flag needs to be [a-zA-Z]*");
      usage=(usage1 != null && !usage1.isBlank()) ? usage1 : "";
      param=parameter1;
      flagList.add(this);
   }
   /**
    * Übergabe der Argumente an alle Flags
    * 
    * @param args1
    *           vom System beim Programmstart übergeben
    * @param standard_args
    *           (diese gelten, wenn keine anderen übergeben wurden)
    */
   public static void setArgs(String[] args1, String standard_args) {
      synchronized (args) {
         args=String.join(" ", args1);
         if (args.isEmpty())
            args=standard_args;
         args=" " + args + " ";
         argumentListe=null;
         for (Flag flag:flagList) {
            flag.param=null;
            flag.flag=null;
            flag.standard=false;
         }
      }
      // hier erfolgt keine Auswertung (lazy)
   }
   /**
    * Gibt die Argumente zurück, die momentan gelten
    * 
    * @return args
    */
   public static String getArgs() {
      return args;
   }
   /**
    * Auswertung dieses Flags (lazy)
    *
    * @return boolean ist dieses Flag gesetzt
    */
   public boolean get() {
      if (flag == null) { // noch nicht ausgewertet
         flag=false;
         /// @todo !(not) unterstützen
         if (lang != null) { // suche langform --extra
            final String findLong=".* --" + lang.toLowerCase().replaceAll("_", "-") + " .*";
            /// @todo relpaceAll unnötig ?
            flag|=args.matches(findLong);
            /// @todo langform mit parameter noch nicht unterstützt
         }
         if (kurz != null) { // suche kurzform (einzeln oder gruppiert) -x
            final String findShort=".* -[a-z]*" + kurz + "[a-z]* .*";
            flag|=args.matches(findShort);
            // suche kurzform mit parameter (einzeln oder gruppiert) -x=hallo
            final String findShortP=".* -[a-z]" + kurz + "=.* ";
            flag|=args.matches(findShortP);
         }
         flag|=standard;
         if (kurz != null) { // suche !kurzform (einzeln oder gruppiert) -!x
            final String findShort=".* -[a-z]*" + "!" + kurz + "[a-z]* .*";
            flag&=!args.matches(findShort);
         }
      }
      return flag;
   }
   /**
    * Auswertung des Parameters zu diesem Flag (lazy)
    *
    * @return parameter
    */
   public String getParameter() {
      if (param == null) {
         if (kurz != null) {// suche kurzform mit parameter
            final String findShort=" -" + kurz + "=([^- =]+)";
            final Matcher ma=Pattern.compile(findShort).matcher(args);
            if (ma.find()) // ersetze den bisherigen parameter
               param=ma.group(1);
         }
         if (lang != null) { // suche langform mit parameter
            final String findLong=" --" + lang.toLowerCase().replaceAll("_", "-") + "=([^- =]+)";
            final Matcher ma2=Pattern.compile(findLong).matcher(args);
            if (ma2.find())// ersetze den bisherigen parameter
               param=ma2.group(1);
         }
      }
      return param;
   }
   /**
    * Holt den Parameter oder einen vorgegebenen Default
    * 
    * Wenn der Ersatz ein Integer ist, wird der Parameter als Integer zurückgegeben
    * 
    * @param ersatz
    *           defaultwert (String oder Integer)
    * @return zum Flag gehörender Parameter (String oder Integer)
    */
   public Object getParameterOrDefault(Object ersatz) {
      String parameter=getParameter();
      if ((parameter == null) || parameter.isBlank())
         return ersatz;
      /// @todo switch case über den Typ
      if (ersatz instanceof Integer)
         try {
            return Integer.decode(parameter);
         } catch (Exception ignore) { /* Dann wars eben kein Integer */
            return ersatz;
         }
      return parameter;
   }
   /**
    * Holt sonstige Argumente aus der Commandline, die nicht zu den Flags gehören
    * 
    * @param index
    *           des gewünschten Arguments (0-based)
    * @return Argument auf der Kommandozeile (String)
    */
   public static String getArgument(int index) {
      return getArgumentOrDefault(index, "");
   }
   /**
    * Holt sonstige Argumente aus der Commandline (mit Default)
    * 
    * @param index
    *           des gewünschten Arguments (0-based)
    * @param standard
    *           Default Argument, wenn der Index leer ist
    * @return Argument auf der Kommandozeile oder Default (String)
    */
   public static String getArgumentOrDefault(int index, String standard) {
      if (argumentListe == null)
         getArgumentList();
      return (index > argumentListe.size()) ? standard : argumentListe.get(index);
   }
   /**
    * Bilde die {@link argumentListe} aus den {@link args}.
    * 
    * Dabei wird zwischen {@link Flag} und parametern unterschieden Flags beginnen mit ' -' oder ' !' und parameter mit was anderem
    * 
    * @return Liste mit zusätzlichen Argumenten (ohne Flags und Parameter)
    */
   public static List<String> getArgumentList() {
      synchronized (args) {
         if (argumentListe == null) {
            ArrayList<String> p=new ArrayList<>();
            /**
             * Argument getrennt durch Leerzeichen und startet nicht mit einem - oder ! oder -!
             */
            final Matcher ma3=Pattern.compile(" [^ -!][^ ]*").matcher(args);
            while (ma3.find())
               p.add(ma3.group().trim());
            argumentListe=p;
         }
      }
      return new ArrayList<>(argumentListe); // gib dein Original nicht aus der Hand
   }
   /**
    * Manuelles setzen oder löschen von Flags durch das Programm
    *
    * @param b1
    *           zustand für das Flag
    * 
    */
   public void set(boolean b1) {
      flag=b1;
   }
   /**
    * Manuelles setzen oder löschen des defaultwertes für ein Flag
    * 
    * Wenn das Flag nicht explizit in den [args] vorkommt wird dieser Status angenommen
    *
    * @param b1 defaultwert für das Flag
    * @return Flag
    */
   synchronized public Flag setDefault(boolean b1) {
      synchronized (args) {
         standard=b1;
      }
      return this;
   }
   /**
    * Main um einige Tests zu fahren
    * 
    * @param argumente Argumente von der Commandline
    */
   public static void main(String[] argumente) {
      Flag.setArgs(argumente, "-a test test1 -c /home /usr/local/bin --zweihundert=200 -f=16");
      for (String p:getArgumentList())
         System.out.println(p);
   }
   /**
    * Manuelles setzen des Paramters zu diesem Flag
    * 
    * @param p Setzt den Parameter für dieses Flag
    */
   public void setParameter(String p) {
      param=p;
   }
   // static public final int parseIntOrDefault(String s, int def) {
   // if (s != null)
   // try {
   // return Integer.parseInt(s);
   // } catch (NumberFormatException ignore) {
   // System.err.println(ignore.getMessage() + ":" + s);
   // }
   // return def;
   // }
   /**
    * Gibt eine Liste der Hilfetexte für die übergebenen Flags zurück
    * 
    * @param filter
    *           Liste der Flags die übergeben werden sollen
    * @return Liste mit Hilfetexten zu diesen Flags
    */
   static public ArrayList<String> getUsage(Flag... filter) {
      ArrayList<String> alle=new ArrayList<>();
      alle.add(" ");
      alle.add("Usage:");
      alle.add("------");
      for (Flag flag:(filter != null && filter.length > 0) ? Arrays.asList(filter) : flagList)
         alle.add(flag.getHilfe());
      return alle;
   }
   /** Gibt eine Hilfe-Zeile für dieses Flag zurück 
    * @return Hilfezeile für dieses Flag
    */
   public String getHilfe() {
      StringBuilder zeile=new StringBuilder(" * ");
      zeile.append((kurz == null) ? "  " : "-" + kurz).append(" ");
      if (breite() > 0) {
         if (lang != null && !lang.isBlank())
            zeile.append("--").append(lang).append(" ".repeat(breite() - lang.length() + 1));
         else
            zeile.append(" ".repeat(breite() + 3));
      }
      if (usage != null && !usage.isBlank())
         zeile.append(usage).append(" ");
      if (param != null && !param.isBlank())
         zeile.append("[=").append(param).append("] ");
      return zeile.toString();// .stripTrailing();
   }
   /** ermittle wie breit die Spalte für lange flags in den Hilfezeilen sein muß */
   static private int breite() {
      if (spalten == null) {
         int s=0;
         for (Flag flag:flagList)
            if (flag.lang != null)
               s=Math.max(s, flag.lang.length());
         spalten=s;
      }
      return spalten;
   }
   @Override
   public String toString() {
      return new StringBuilder("Flag[")//
               .append(kurz).append(" ")//
               .append(lang).append(" ")//
               .append(param).append(" ")//
               .append("]").toString();
   }
}

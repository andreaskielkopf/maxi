package de.uhingen.kielkopf.andreas.maxi;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.Console;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Unterstützt das Weitergeben von Texten ins Clipboard insbesondere wenn eine GUI existiert
 * 
 * @author Andreas Kielkopf
 * @since 2025 06 04
 *
 */
public class ClipboardSupport {
   private ClipboardSupport() {
      throw new UnsupportedOperationException("This Class is meant to be used static only");
   }
   /** Internes Array mit den Texten fürs Clipboard */
   static private final CopyOnWriteArrayList<String> clip=new CopyOnWriteArrayList<>();
   /** Die letzte unvollständige Zeile */
   static private String                             line="";
   /** Hält die Selection bis zum Programmende */
   static private StringSelection                    selection;
   /**
    * Druckt den Text ins Clipboard.
    * 
    * Löscht dabei alle auf der Kommandozeile genutzten Farb-ESC-Sequernzen aus dem Text
    * 
    * @param text
    *           zum clipboard hinzufügen
    */
   public static void clipln(String text) {
      clip.add(text.replaceAll(InfoLine.ANY_ESC, ""));
   }
   /**
    * Druckt den Text in den Zeilenpuffer
    * 
    * @param text
    *           an die Zeile anfügen
    */
   public static void print(String text) {
      line+=text;
   }
   /**
    * Druckt einen Zeilenvorschub auf die Konnandozeile.
    * 
    * Druckt dabei den restlichen Text im Zeilenpuffer, und löscht den Zeilenpuffer
    */
   public static void println() {
      println(line);
      line="";
   }
   /**
    * Druckt die Infoline auf die Kommandozeile.
    * 
    * Dabei werden ESC-Sequenzen für Farben eingefügt
    * 
    * @param info
    *           {@link InfoLine}
    */
   public static void println(InfoLine info) {
      println(info.toString());
   }
   /**
    * Druckt den Stream als Tabelle auf die Kommandozeile.
    * 
    * Dabei werden ESC-Sequenzen für Farben eingefügt
    * 
    * @param info
    *           {@link InfoLine}
    */
   public static void println(Stream<? extends InfoLine> info) {
      info.forEach(t -> println(t));
   }
   /**
    * Druckt die Zeile ins Clipboard und auf die Konsole
    * 
    * @param text
    *           ins Clipboard und die Konsole ausgeben
    */
   public static void println(String text) {
      clipln(text);
      System.out.println(text);
   }
   /**
    * Gib den Text nur auf der Commandozeile aus
    * 
    * @param text
    *           in die Konsole ausgeben
    */
   public static void printonly(String text) {
      System.out.println(text);
   }
   /**
    * Übertrage den kompletten Inhalt von clip ins Clipboard (wenn möglich).
    * 
    * Der Inhalt wird zum Clipboard verzeigert, und bleibt daher nur bis zum Programmende erhalten Deswegen wird wenn möglich auf einen Tastendruck
    * gewartet.
    */
   public static void transfer() {
      if (!clip.isEmpty()) {
         try {
            final Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
            selection=new StringSelection(String.join(System.lineSeparator(), clip));
            clipboard.setContents(selection, selection);
            final Console console=System.console();
            if (console != null) {
               printonly("\nPress ENTER to proceed.\n");
               // halte den Inhalt im Clipboard bis die Taste gedrückt ist
               console.readLine();
            }
         } catch (final HeadlessException e) {
            System.err.println("Copying to clipboard is not possible");
         }
         clip.clear();
      }
   }
}

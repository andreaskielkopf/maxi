package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;

/**
 * Tabellarische Info über eine Config-Datei mit eigenständiger Spaltenaufteilung.
 * 
 * Wird von {@link GrubInfo} und {@link MkinitcpioInfo} genutzt
 * 
 * @author Andreas Kielkopf
 *
 */
public class ConfigInfo extends InfoLine {
   /** gemeinsame Liste der Spaltenbreite */
   private static ArrayList<Integer> spalten=new ArrayList<>();
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste mit den Texten (Spaltenweise)
    */
   public ConfigInfo(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   @Override
   public String toString() {
      return getLine(info, spalten.iterator());
   }
}

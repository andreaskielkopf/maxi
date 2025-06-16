package de.uhingen.kielkopf.andreas.maxi;

import java.util.ArrayList;

/**
 * Wo ist EFI gemountet
 * 
 * @author Andreas Kielkopf
 *
 */
public class EfiMount extends InfoLine {
   /** gemeinsame Liste der Spaltenbreite */
   static ArrayList<Integer> spalten=new ArrayList<>();
   /**
    * Konstruktor mit gemeinsamer Spaltenbreite
    * 
    * @param iterableInfo
    *           Liste der Infos
    *
    */
   public EfiMount(Iterable<String> iterableInfo) {
      super(iterableInfo, spalten);
   }
   @SuppressWarnings("unused") private static final String VFAT=" vfat ";
   @SuppressWarnings("unused") private static final String EFI ="efi ";
}

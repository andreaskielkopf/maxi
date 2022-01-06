package de.uhingen.kielkopf.andreas.maxi;

/**
 * 
 * @author andreas kielkopf
 *
 */
public enum Flag {
   DEFAULT(), HELP('h'), COLOR('c'), WATCH('w', "100"), LIST_ALL('l'), MODULES('m'), KERNEL('k'), GRUB('g'), DATES('d'),
   ZSH('z'), LISTONEXIT('x'), EFI('e');
   final private char   c;
   final private String p;
   private Boolean      flag  =null;
   static String        joined=null;
   Flag() {
      c=0;
      this.p=null;
   }
   Flag(char flag1) {
      c=flag1;
      this.p=null;
   }
   Flag(char flag1, String parameter) {
      c=flag1;
      p=parameter;
   }
   static void init(String[] args) {
      joined=" "+String.join(" ", args)+" ";
      System.out.println(joined);
   }
   @SuppressWarnings("boxing")
   boolean get() {
      if (flag==null)
         if (this==DEFAULT) {
            flag=(joined.length()<3);
         } else {
            String findLong =".* --"+name().toLowerCase().replaceAll("_", "-")+" .*";
            String findShort=".* -[a-z]*"+c+"[a-z] .*";
            flag=(joined.matches(findLong)||joined.matches(findShort));
         }
      return flag;
   }
   @SuppressWarnings("boxing")
   void set(boolean b) {
      flag=b;
   }
}

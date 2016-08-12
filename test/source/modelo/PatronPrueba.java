package source.modelo;

import java.util.ArrayList;
import java.util.List;

import source.patron.GeneradorID;
import source.patron.Patron;
import source.patron.PatronDictionaryFinalEvent;
import source.restriccion.RIntervalo;

public class PatronPrueba extends PatronDictionaryFinalEvent {

   public static List<List<String>> incompatibles = new ArrayList<List<String>>();

   String name = "";

   public PatronPrueba(GeneradorID genID, String[] tipos, String name){
      this(tipos, true);
      this.name = name;
      this.patternID=genID.nextID();
   }

   public PatronPrueba(String[] tipos, boolean savePatternInstances) {
      super(tipos, savePatternInstances);
   }
   public PatronPrueba(String[] tipos, List<RIntervalo> restricciones,
         boolean savePatternInstances) {
      super(tipos, restricciones, savePatternInstances);
   }
   public PatronPrueba(String[] tipos, Patron patron) {
      super(tipos, patron);
      if(patron instanceof PatronPrueba){
         setName(((PatronPrueba) patron).getName());
      }
   }
   public PatronPrueba(Patron patron) {
      super(patron);
      if(patron instanceof PatronPrueba){
         setName(((PatronPrueba) patron).getName());
      }
   }
   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }

   private boolean lookForIncompatiblity(Patron patron){
      if(patron instanceof PatronPrueba){
         String name2 = ((PatronPrueba)patron).getName();
         if(!name2.equals("") && !name.equals("")){
            System.out.println("Names: " + name + ", " + name2);
            for(List<String> incompatible: incompatibles){
               if(incompatible.contains(name) && incompatible.contains(name2)){
                  System.out.println("No compatibles");
                  return true;
               }
            }
         }
      }
      return false;
   }

   @Override
   public boolean combinar(Patron patron) {
      boolean exit = lookForIncompatiblity(patron);
      if(exit){
         return false;
      }
      return super.combinar(patron);
   }
   @Override
   public boolean combinar(Patron patron, int indice_ausente) {
      boolean exit = lookForIncompatiblity(patron);
      if(exit){
         return false;
      }
      return super.combinar(patron, indice_ausente);
   }



   @Override
   public String toString(){
      return "<PatronPrueba: \"" + getTipos() + "\">";
   }

   public boolean equals(Object obj){
      if(!(obj instanceof PatronPrueba)) return false;
      return name.equals(((PatronPrueba)obj).name);
   }

}

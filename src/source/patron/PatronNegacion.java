package source.patron;

import java.util.List;

import source.restriccion.RIntervalo;

public class PatronNegacion extends Patron {

   private final String[] negados;

   public PatronNegacion(String[] tipos, String[] negados, boolean savePatternInstances) {
      super(tipos, savePatternInstances);
      this.negados = negados;
   }

   public PatronNegacion(String[] tipos, String[] negados, List<RIntervalo> restricciones,
         boolean savePatternInstances) {
      super(tipos, restricciones, savePatternInstances);
      this.negados = negados;
   }

   public PatronNegacion(String[] tipos, String[] negados, int[][] matriz,
         Boolean savePatternInstances) {
      super(tipos, matriz, savePatternInstances);
      this.negados = negados;
   }

   public PatronNegacion(Patron patron) {
      super(patron);
      if(patron instanceof PatronNegacion){
         this.negados = ((PatronNegacion)patron).negados;
      }else{
         this.negados = new String[0];
      }
   }

   public PatronNegacion(String[] tipos, String[] negados, Patron patron) {
      super(tipos, patron);
      this.negados = negados;
      //TODO sería así? uqe integrarlos de otra forma?
   }

}

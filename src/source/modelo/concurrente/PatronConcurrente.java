package source.modelo.concurrente;

import java.util.List;

import source.patron.Patron;
import source.restriccion.RIntervalo;

public class PatronConcurrente extends Patron {

   /*
    * Constructores
    */
   public PatronConcurrente(String[] tipos, List<RIntervalo> restricciones, boolean savePatternInstances){
      super(tipos,restricciones,savePatternInstances);
   }

   protected PatronConcurrente(String[] tipos, boolean savePatternInstances){
      super(tipos, savePatternInstances);
   }

   public PatronConcurrente(String[] tipos, int[][] matriz, Boolean savePatternInstances){
      super(tipos, matriz, savePatternInstances);
   }

   public PatronConcurrente(Patron patron){
      super(patron);
   }

   public PatronConcurrente(String[] tipos, Patron patron){
      super(tipos, patron);
   }

   /*
    * Métodos
    */

   /*
    * Sincroniza el método
    * (non-Javadoc)
    * @see source.patron.Patron#addOcurrencia(int, int[])
    */
   protected synchronized void addOcurrencia(int sid, int[] ocurrencia){
      super.addOcurrencia(sid, ocurrencia);
   }


}

package source.modelo.concurrente;

import java.util.List;

import source.patron.Patron;
import source.patron.PatronEventoFinal;
import source.restriccion.RIntervalo;

public class PatronConcurrenteEventoFinal extends PatronEventoFinal{

   /*
    * Constructores heredados.
    */

   public PatronConcurrenteEventoFinal(String[] tipos, List<RIntervalo> restricciones, boolean savePatternInstances){
      super(tipos,restricciones, savePatternInstances);
   }

   protected PatronConcurrenteEventoFinal(String[] tipos, boolean savePatternInstances){
      super(tipos, savePatternInstances);
   }

   public PatronConcurrenteEventoFinal(Patron patron){
      super(patron);
   }

   public PatronConcurrenteEventoFinal(String[] tipos, Patron patron){
      super(tipos, patron);
   }

   /*
    * Métodos específicos.
    */

   /*
    * Sincroniza
    * (non-Javadoc)
    * @see source.patron.PatronEventoFinal#encontrado()
    */
   @Override
   public synchronized void encontrado(){
      super.encontrado();
   }

   /*
    * Sincroniza
    * (non-Javadoc)
    * @see source.patron.Patron#addOcurrencia(int, int[])
    */
   @Override
   protected synchronized void addOcurrencia(int sid, int[] ocurrencia){
      super.addOcurrencia(sid, ocurrencia);
   }

}

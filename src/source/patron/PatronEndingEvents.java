package source.patron;

import java.util.List;

import source.restriccion.RIntervalo;

/**
 * Como  {@link PatronEventoFinal} pero con un array de {@code boolean} en lugar de una lista
 * con los tipos.
 * @author vanesa.graino
 *
 */
public class PatronEndingEvents extends Patron {

   boolean[] endingEvents;

   public PatronEndingEvents(String[] tipos, boolean savePatternInstances) {
      super(tipos, savePatternInstances);
   }

   public PatronEndingEvents(String[] tipos, List<RIntervalo> restricciones,
         boolean savePatternInstances) {
      super(tipos, restricciones, savePatternInstances);
   }

   public PatronEndingEvents(String[] tipos, int[][] matriz,
         Boolean savePatternInstances) {
      super(tipos, matriz, savePatternInstances);
   }

   public PatronEndingEvents(Patron patron) {
      super(patron);
   }

   public PatronEndingEvents(String[] tipos, Patron patron) {
      super(tipos, patron);
   }

}

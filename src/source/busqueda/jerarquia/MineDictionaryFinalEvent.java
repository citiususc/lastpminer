package source.busqueda.jerarquia;

import java.util.logging.Logger;

import source.modelo.clustering.IClustering;

/**
 * Cambia respecto a MineDictionary que utiliza ModeloDictionaryFinalEvent en lugar de ModeloDictionary
 * @author vanesa.graino
 *
 */
public class MineDictionaryFinalEvent extends MineDictionary { //extends Mine
   private static final Logger LOGGER = Logger.getLogger(MineDictionaryFinalEvent.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   {
      associationClassName = "ModeloDictionaryFinalEvent";
      patternClassName = "PatronDictionaryFinalEvent";
   }

   public MineDictionaryFinalEvent(String executionId, boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
   }

}

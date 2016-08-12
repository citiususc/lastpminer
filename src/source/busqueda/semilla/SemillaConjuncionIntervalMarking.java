package source.busqueda.semilla;

import java.util.logging.Logger;

import source.modelo.clustering.IClustering;

/**
 * TODO implementar
 * @author vanesa.graino
 *
 */
public class SemillaConjuncionIntervalMarking extends SemillaConjuncionDictionaryFinalEvent {
   private static final Logger LOGGER = Logger.getLogger(SemillaConjuncionIntervalMarking.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   public SemillaConjuncionIntervalMarking(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents, boolean saveAllAnnotations,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, saveAllAnnotations, clustering, removePatterns);
   }


   //TODO implementar metodo de calculo de soporte (copiar de la otra clase de IM)
}

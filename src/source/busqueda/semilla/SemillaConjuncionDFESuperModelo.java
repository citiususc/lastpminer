package source.busqueda.semilla;

import java.util.List;
import java.util.logging.Logger;

import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.SuperModelo;
import source.modelo.semilla.ModeloSemilla;

//TODO implementar con Semilla
public class SemillaConjuncionDFESuperModelo extends SemillaConjuncionDictionaryFinalEvent {
   private static final Logger LOGGER = Logger.getLogger(SemillaConjuncionDFESuperModelo.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }
   /*
    * Atributos propios
    */

   protected SuperModelo supermodelo;


   /*
    * Constructores
    */

   public SemillaConjuncionDFESuperModelo(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         boolean saveAllAnnotations, IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents,
            saveAllAnnotations, clustering, removePatterns);
   }

   /*
    * Métodos propios
    */

   @Override
   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> candidatos,
         int win, String[] tiposSemilla, List<ModeloSemilla> semillas,
         List<List<IAsociacionTemporal>> semNivel, int cSize) throws FactoryInstantiationException{
      super.inicializaEstructuras(tipos, candidatos, win, tiposSemilla, semillas, semNivel, cSize);
      supermodelo = new SuperModelo(tipos.toArray(new String[tipos.size()]), win);
   }


}

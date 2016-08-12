package source.busqueda.paralela;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.Mine;
import source.evento.IColeccion;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

/**
 * Clase que implementa el algoritmo ASTPminer con paralelismo
 * en el cálculo del soporte. Cada hilo gestionar una asociación temporal en cada iteración.
 * @author vanesa.graino
 *
 */
public class ParallelMine extends Mine implements IBusquedaParalela{
   private static final Logger LOGGER = Logger.getLogger(ParallelMine.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   private int numHilosPar;

   public ParallelMine(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.numHilosPar = numHilos;
      associationClassName += "Paralelo";
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      List<Thread> hilos = new ArrayList<Thread>();
      //Usar class.getSimpleName() en lugar de una cadena con el nombre protege frente a renombrados de la clase
      ParallelHelper.runAndExcuteThreads(HiloSoporte.class.getSimpleName(), hilos, numHilosPar, coleccion, candidatas, this, 0, null);
   }


}

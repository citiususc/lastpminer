package source.busqueda.paralela.episodios;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.episodios.MineEpisodes;
import source.busqueda.paralela.IBusquedaParalela;
import source.busqueda.paralela.ParallelHelper;
import source.evento.IColeccion;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

/**
 * Clase que implementa la variante del algoritmo ASTPminer que trabaja con
 * episodio utilizando paralelismo en el cálculo del soporte.
 * Todos los métodos son un copy/paste de MineEpisodes.
 * @author vanesa.graino
 *
 */
public class ParallelMineEpisodes extends MineEpisodes implements IBusquedaParalela{
   private static final Logger LOGGER = Logger.getLogger(ParallelMineEpisodes.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   private int numHilosPar;

   /*
    * Constructores
    */

   public ParallelMineEpisodes(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns, int numHilos){
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.numHilosPar = numHilos;
      associationClassName += "Paralelo";
   }

   /*
    * Redefinición de métodos plantilla y métodos propios
    */
   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      List<Thread> hilos = new ArrayList<Thread>();
      ParallelHelper.runAndExcuteThreads("HiloSoporte", hilos, numHilosPar, coleccion, candidatas, this, 0, null);
   }

}

package source.busqueda.paralela.episodios;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.episodios.MineCompleteEpisodes;
import source.busqueda.paralela.IBusquedaParalela;
import source.busqueda.paralela.ParallelHelper;
import source.evento.IColeccion;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

/**
 * Clase que implementa la variante del algoritmo ASTPminer que trabaja con
 * episodio utilizando paralelismo en el cálculo del soporte.
 * Todos los métodos son un copy/paste de MineCompleteEpisodes.
 * @author vanesa.graino
 *
 */
public class ParallelMineCompleteEpisodes extends MineCompleteEpisodes implements IBusquedaParalela{
   private static final Logger LOGGER = Logger.getLogger(ParallelMineCompleteEpisodes.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   private int numHilosPar;

   public ParallelMineCompleteEpisodes(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns, int numHilos){
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.numHilosPar = numHilos;
      associationClassName += "Paralelo";
   }


   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      if(candidatasGeneradas != null && !candidatasGeneradas.isEmpty()){
         // Si no hay candidatos, evitar hacer el cálculo de frecuencia
         List<Thread> hilos = new ArrayList<Thread>();
         ParallelHelper.runAndExcuteThreads("HiloSoporte", hilos, numHilosPar, coleccion, candidatas, this, 0, null);
      }
   }
}

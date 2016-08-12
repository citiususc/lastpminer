package source.busqueda.paralela.semilla;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.paralela.IBusquedaParalelaSecuencia;
import source.busqueda.paralela.ParallelHelper;
import source.busqueda.semilla.SemillaConjuncionCompleteEpisodes;
import source.evento.IColeccion;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

public class ParallelSemillaConjuncionCompleteEpisodes extends SemillaConjuncionCompleteEpisodes implements IBusquedaParalelaSecuencia {
   private static final Logger LOGGER = Logger.getLogger(ParallelSemillaConjuncionCompleteEpisodes.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   private int numHilosPar;
   protected int sidActual = -1, borrados, restantes;

   public ParallelSemillaConjuncionCompleteEpisodes(String executionId, boolean savePatternInstances,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.numHilosPar = numHilos;
      associationClassName += "Paralelo";
   }

   /* El método calcularFrecuencia es igual al método de ParallelMine EXCEPTO la primera línea  */

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion, boolean esSemilla){
      if(!esSemilla && candidatasGeneradas.isEmpty()){ return; }

      List<Thread> hilos = new ArrayList<Thread>();
      ParallelHelper.runAndExcuteThreads("HiloSoporte", hilos, numHilosPar, coleccion, candidatas, this, 0, null);
   }

   /* COPIADO de ParallelSemillaConjuncion */

   protected void calcularSoporteSemilla(List<IAsociacionTemporal> candidatas, IColeccion coleccion, int ventana){
      borrados=0;
      restantes=0;
      windowSize = ventana;

      List<Thread> hilos = new ArrayList<Thread>();
      ParallelHelper.runAndExcuteThreads("HiloSoporteSemilla", hilos, numHilosPar, coleccion, candidatas, this, 0, null);

      imprimirEliminados(LOGGER, borrados, restantes);
   }

   @Override
   public int getSiguienteSecuencia(IColeccion coleccion){
      synchronized(coleccion){
         if(sidActual < coleccion.size()-1){
            return ++sidActual;
         }
      }
      return -1;
   }

}

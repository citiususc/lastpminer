package source.busqueda.concurrente.episodios;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.concurrente.ConcurrenteHelper;
import source.busqueda.concurrente.HiloConcurrente;
import source.busqueda.concurrente.IBusquedaConcurrenteSecuencia;
import source.busqueda.episodios.MineEpisodes;
import source.evento.IColeccion;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

public class ConcurrenteMineEpisodes extends MineEpisodes implements IBusquedaConcurrenteSecuencia{
   private static final Logger LOGGER = Logger.getLogger(ConcurrenteMineEpisodes.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int sidActual = -1, borrados, restantes;

   public ConcurrenteMineEpisodes(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.numHilos = numHilos;
   }

   /* Copiado de ConcurrenteMine */
   @Override
   public int getSiguienteSecuencia(IColeccion coleccion){
      synchronized(coleccion){
         if(sidActual < coleccion.size()-1){
            return ++sidActual;
         }
      }
      return -1;
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      borrados=0;
      restantes=0;
      sidActual=-1;

      List<Thread> hilos = new ArrayList<Thread>();

      for(int i=0; i<numHilos; i++){
         Thread hilo = new Thread(new HiloConcurrente(i, this, coleccion, mapa/*, candidatas*/));
         hilo.start();
         hilos.add(hilo);
      }

      for(Thread hilo : hilos){
         try {
            hilo.join();
         } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Excepción al hacer join de los hilos de concurrente", e);
         }
      }
      ConcurrenteHelper.agregarResultados(/*candidatas*/);
      imprimirEliminados(LOGGER, borrados, restantes);
   }

}

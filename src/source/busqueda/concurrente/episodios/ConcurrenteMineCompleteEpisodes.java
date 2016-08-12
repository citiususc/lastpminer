package source.busqueda.concurrente.episodios;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.concurrente.ConcurrenteHelper;
import source.busqueda.concurrente.HiloConcurrente;
import source.busqueda.concurrente.IBusquedaConcurrenteSecuencia;
import source.busqueda.episodios.MineCompleteEpisodes;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

public class ConcurrenteMineCompleteEpisodes extends MineCompleteEpisodes implements IBusquedaConcurrenteSecuencia{
   private static final Logger LOGGER = Logger.getLogger(ConcurrenteMineCompleteEpisodes.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int sidActual = -1, borrados, restantes;

   public ConcurrenteMineCompleteEpisodes(String executionId,
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

   protected List<Thread> crearHilos(IColeccion coleccion, int tamActual) throws FactoryInstantiationException{
      List<Thread> hilos = new ArrayList<Thread>();
      for(int i=0; i<numHilos; i++){
         Thread hilo = new Thread(new HiloConcurrente(i, this, coleccion, mapa/*, candidatas*/));
         hilo.start();
         hilos.add(hilo);
      }
      return hilos;
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      if(candidatasGeneradas == null || candidatasGeneradas.isEmpty()){
         return;
      }
      borrados=0;
      restantes=0;
      sidActual=-1;

      try {
         List<Thread> hilos = crearHilos(coleccion, -1);
         for(Thread hilo : hilos){
            hilo.join();
         }
         ConcurrenteHelper.agregarResultados(/*candidatas*/);
         imprimirEliminados(LOGGER, borrados, restantes);
      } catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Excepción al crear los hilos", e);
      } catch (InterruptedException e) {
         LOGGER.log(Level.SEVERE, "Excepción al hacer join de los hilos de concurrente", e);
      }
   }

}

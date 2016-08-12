package source.busqueda.concurrente.jerarquia;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.concurrente.ConcurrenteHelper;
import source.busqueda.concurrente.HiloConcurrenteFactory;
import source.busqueda.concurrente.IBusquedaConcurrenteSecuenciaAnotaciones;
import source.busqueda.jerarquia.MineDictionary;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

public class ConcurrenteMineDictionary extends MineDictionary implements IBusquedaConcurrenteSecuenciaAnotaciones {
   private static final Logger LOGGER = Logger.getLogger(ConcurrenteMineDictionary.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int sidActual = -1, borrados, restantes;

   public ConcurrenteMineDictionary(String executionId,
         boolean savePatternInstances, boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
      this.numHilos = numHilos;
   }

   protected List<Thread> crearHilos(IColeccion coleccion, int tamActual) throws FactoryInstantiationException{
      String hiloClassName = "HiloConcurrente";
      if(tamActual==3){
         hiloClassName = "HiloConcurrenteTam3";
      }else if(tamActual>3){
         hiloClassName = "HiloConcurrenteTam4";
      }
      List<Thread> hilos = new ArrayList<Thread>();
      for(int i=0; i<numHilos; i++){
         Thread hilo = new Thread(HiloConcurrenteFactory.getHiloConcurrente(hiloClassName, i, this, coleccion, mapa, tamActual, null));
         hilo.start();
         hilos.add(hilo);
      }
      return hilos;
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      borrados=0;
      restantes=0;
      sidActual=-1;

      int tamActual = candidatas.get(0).size();
      try {
         List<Thread> hilos = crearHilos(coleccion, tamActual);
         for(Thread hilo : hilos){
            hilo.join();
         }
         ConcurrenteHelper.agregarResultados(/*candidatas*/);
         anotaciones.guardarAnotaciones();
         imprimirEliminados(LOGGER, borrados, restantes);
      } catch (InterruptedException e) {
         LOGGER.log(Level.SEVERE, "Excepción al hacer join de los hilos de concurrente", e);
      } catch (FactoryInstantiationException e) {
         LOGGER.log(Level.SEVERE, "Excepción al crear los hilos de concurrente", e);
      }
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

}

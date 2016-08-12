package source.busqueda.concurrente;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

/**
 * Versión paralelización a nivel de secuencia de ASTP. Los hilos piden secuencias y las procesan.
 * Los modelos tienen variables de conteo para cada hilo por lo que se evita la sincronización en ese punto.
 * @author vanesa.graino
 *
 */
public class ConcurrenteMine extends source.busqueda.Mine implements IBusquedaConcurrenteSecuencia{
   private static final Logger LOGGER = Logger.getLogger(ConcurrenteMine.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int sidActual = -1, borrados, restantes;

   /*
    * Constructores
    */
   public ConcurrenteMine(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.numHilos = numHilos;
   }

   //TODO ahora se sincroniza dentro del método
   @Override
   public int getSiguienteSecuencia(IColeccion coleccion){
      synchronized(coleccion){
         if(sidActual < coleccion.size()-1){
            return ++sidActual;
         }
      }
      return -1;
   }

   protected List<Thread> crearHilos(IColeccion coleccion) throws FactoryInstantiationException{
      List<Thread> hilos = new ArrayList<Thread>();
      for(int i=0; i<numHilos; i++){
         Thread hilo = new Thread(new HiloConcurrente(i, this, coleccion, mapa));
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

      try {
         List<Thread> hilos = crearHilos(coleccion);

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

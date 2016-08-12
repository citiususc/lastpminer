package source.busqueda.concurrente.semilla;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.concurrente.ConcurrenteHelper;
import source.busqueda.concurrente.HiloConcurrenteFactory;
import source.busqueda.concurrente.IBusquedaConcurrenteSecuencia;
import source.busqueda.semilla.SemillaConjuncionCompleteEpisodes;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

public class ConcurrenteSemillaConjuncionCE extends SemillaConjuncionCompleteEpisodes implements IBusquedaConcurrenteSecuencia{
   private static final Logger LOGGER = Logger.getLogger(ConcurrenteSemillaConjuncionCE.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int sidActual = -1, borrados, restantes;

   public ConcurrenteSemillaConjuncionCE(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.numHilos = numHilos;
   }

   @Override
   protected void calcularSoporteSemilla(IColeccion coleccion){
      calcularSoporte(null, coleccion, true);
   }

   protected List<Thread> crearHilos(IColeccion coleccion, boolean esSemilla) throws FactoryInstantiationException{
      String hiloClassName = esSemilla? "HiloConcurrenteSemilla" : "HiloConcurrente";
      List<Thread> hilos = new ArrayList<Thread>();
      for(int i=0; i<numHilos; i++){
         Thread hilo = new Thread(HiloConcurrenteFactory.getHiloConcurrente(hiloClassName, i, this, coleccion, mapa, 0, null));
         hilo.start();
         hilos.add(hilo);
      }
      return hilos;
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion, boolean esSemilla){
      if(!esSemilla && candidatasGeneradas.isEmpty()){
         return;
      }

      borrados=0;
      restantes=0;
      sidActual=-1;

      try {
         List<Thread> hilos = crearHilos(coleccion, esSemilla);

         for(Thread hilo : hilos){
            hilo.join();
         }

         ConcurrenteHelper.agregarResultados(/*candidatas*/);
         imprimirEliminados(LOGGER, borrados, restantes);
      } catch (InterruptedException e) {
         LOGGER.log(Level.SEVERE, "Excepción al hacer join de los hilos de concurrente", e);
      } catch (FactoryInstantiationException e) {
         LOGGER.log(Level.SEVERE, "Excepción al crear los hilos de concurrente", e);
      }
   }


   //Métodos copiados de ConcurrenteMine

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

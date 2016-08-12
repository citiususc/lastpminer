package source.busqueda.concurrente.semilla;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.concurrente.ConcurrenteHelper;
import source.busqueda.concurrente.HiloConcurrenteFactory;
import source.busqueda.concurrente.IBusquedaConcurrenteSecuenciaAnotaciones;
import source.busqueda.semilla.SemillaConjuncionDictionaryFinalEvent;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

public class ConcurrenteSemillaConjuncionDFE extends SemillaConjuncionDictionaryFinalEvent implements IBusquedaConcurrenteSecuenciaAnotaciones{
   private static final Logger LOGGER = Logger.getLogger(ConcurrenteSemillaConjuncionDFE.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int sidActual = -1, borrados, restantes;

   public ConcurrenteSemillaConjuncionDFE(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         boolean saveAllAnnotations, IClustering clustering, boolean removePatterns,
         int numHilos) {
      super(executionId, savePatternInstances, saveRemovedEvents, saveAllAnnotations, clustering, removePatterns);
      this.numHilos = numHilos;
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

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      calcularSoporte(candidatas, coleccion, false);
   }

   @Override
   protected void calcularSoporteSemilla(IColeccion coleccion) {
      calcularSoporte(null,coleccion, true);
   }

   /* Copiado de ConcurrenteMineDictionary a 16 de Septiembre de 2014 */

   protected List<Thread> crearHilos(IColeccion coleccion, int tamActual, boolean esSemilla) throws FactoryInstantiationException{
      String hiloClassName = "HiloConcurrente";
      if(esSemilla){
         hiloClassName = "HiloConcurrenteSemilla";
      }else if(tamActual==3){
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

   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion, boolean esSemilla){
      //eventosEliminados.add(new ArrayList<EventoEliminado>());
      borrados=0;
      restantes=0;
      sidActual=-1;

      int tamActual = -1;
      if(candidatas!=null){
         tamActual = candidatas.get(0).size();
      }
      try {
         List<Thread> hilos = crearHilos(coleccion, tamActual, esSemilla);
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

   /*
    * Pasa a ser un método público
    * (non-Javadoc)
    * @see source.busqueda.semilla.SemillaConjuncionDictionaryFinalEvent#posiblesTiposParaAmpliar(java.util.List, java.util.List)
    */
   @Override
   public List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar){
      return super.posiblesTiposParaAmpliar(actual, tiposAmpliar);
   }

}

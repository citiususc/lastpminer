package source.busqueda.concurrente.episodios;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.busqueda.concurrente.ConcurrenteHelper;
import source.busqueda.concurrente.HiloConcurrenteFactory;
import source.busqueda.concurrente.IBusquedaConcurrenteSecuenciaAnotaciones;
import source.busqueda.episodios.MineCEDFE;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;

public class ConcurrenteMineCEDFE extends MineCEDFE implements IBusquedaConcurrenteSecuenciaAnotaciones{
   private static final Logger LOGGER = Logger.getLogger(ConcurrenteMineCEDFE.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   protected int sidActual = -1, borrados, restantes;


   public ConcurrenteMineCEDFE(String executionId,
         boolean savePatternInstances, boolean saveAllAnnotations,
         boolean saveRemovedEvents, IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
      this.numHilos = numHilos;
   }

   protected List<Thread> crearHilos(IColeccion coleccion, int tamActual) throws FactoryInstantiationException{
      String hiloClassName = "HiloConcurrente";
      if(tamActual==3){
         hiloClassName = "HiloConcurrenteTam3";
      }else if(tamActual==4){
         hiloClassName = "HiloConcurrenteTam4Episodios";
      }else if(tamActual>4){
         hiloClassName = "HiloConcurrenteTam5Episodios";
      }
      List<Thread> hilos = new ArrayList<Thread>();
      for(int i=0; i<numHilos; i++){
         Thread hilo = new Thread(HiloConcurrenteFactory.getHiloConcurrente(hiloClassName, i, this, coleccion, mapa, tamActual, getListaEpisodios()));
         hilo.start();
         hilos.add(hilo);
      }
      return hilos;
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      if(candidatas.isEmpty()){
         anotaciones.guardarAnotaciones();
         return;
      }

      borrados=0;
      restantes=0;
      sidActual=-1;

      int tamActual = candidatas.get(0).size();
      try{
         List<Thread> hilos = crearHilos(coleccion, tamActual);
         for(Thread hilo : hilos){
            hilo.join();
         }
         ConcurrenteHelper.agregarResultados(/*candidatas*/);
         anotaciones.guardarAnotaciones();
         imprimirEliminados(LOGGER, borrados, restantes);
      } catch(FactoryInstantiationException e){
         LOGGER.log(Level.SEVERE, "Excepción al crear los hilos", e);
      } catch (InterruptedException e) {
         LOGGER.log(Level.SEVERE, "Excepción al hacer join de los hilos de concurrente", e);
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

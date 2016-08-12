package source.busqueda.paralela;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.modelo.IAsociacionDeHilo;
import source.modelo.IAsociacionTemporal;
import source.modelo.paralelo.IAsociacionAgregable;
import source.patron.Patron;

public final class ParallelHelper {
   private static final Logger LOGGER = Logger.getLogger(ParallelHelper.class.getName());

   private ParallelHelper(){

   }

   /**
    * Incorpora lo que se ha encontrado en un hilo a la lista de candidatos originales
    * @param originales - lista de candidatos generales
    * @param nueva - candidatos del hilo con sus frecuencias, distribuciones, etc., actualizadas
    * @return
    */
   public static void agregarCandidatos(List<IAsociacionTemporal> originales, List<IAsociacionTemporal> nuevos){
      for(int i=0;i<originales.size();i++){
         IAsociacionTemporal mod = nuevos.get(i);
         if(mod.getSoporte() == 0){ continue; }
         ((IAsociacionAgregable)originales.get(i)).agregar(mod);
      }
   }


   public static Map<String,List<IAsociacionTemporal>> copiaMapa(Map<String,List<IAsociacionTemporal>> original,
         List<IAsociacionTemporal> candidatosOriginal, List<IAsociacionTemporal> candidatosClon, Map<String[],IAsociacionTemporal> clonadas, int hilo){
      Map<String,List<IAsociacionTemporal>> copia = new HashMap<String,List<IAsociacionTemporal>>(original.size());
      for(String key : original.keySet()){
         copia.put(key, new ArrayList<IAsociacionTemporal>());
      }

      for(IAsociacionTemporal candidato : candidatosOriginal){
         IAsociacionTemporal clon =((IAsociacionAgregable)candidato).clonar();
         ((IAsociacionDeHilo)clon).setHilo(hilo);
         candidatosClon.add(clon);
         clonadas.put(clon.getTipos(), clon);
         for(String tipo:clon.getTipos()){
            copia.get(tipo).add(clon);
         }
      }
      return copia;
   }

   public static void runAndExcuteThreads(String hiloClassName, List<Thread> hilos, int numHilos,
         IColeccion coleccion, List<IAsociacionTemporal> candidatas, IBusquedaParalela mine,
         int tamActual, List<List<List<Patron>>> actual){
      try {
         for(int i=0; i<numHilos; i++){
            Thread hilo = new Thread(HiloFactory.getInstance(hiloClassName, coleccion, i,
                  actual, candidatas, mine, tamActual));
            hilo.start();
            hilos.add(hilo);
         }
         for(Thread hilo : hilos){
            hilo.join();
         }
      } catch (InterruptedException e) {
         LOGGER.log(Level.SEVERE, "Excepción al hacer join de los hilos de parelelo", e);
      } catch (FactoryInstantiationException e) {
         LOGGER.log(Level.SEVERE, "Excepción al crear los hilos de parelelo", e);
      }
   }
}

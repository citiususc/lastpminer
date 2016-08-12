package source.busqueda.paralela.jerarquia;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.jerarquia.MineDictionary;
import source.busqueda.paralela.IBusquedaParalelaSecuenciaAnotaciones;
import source.busqueda.paralela.ParallelHelper;
import source.evento.IColeccion;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoHilos;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;

/**
 * Los métodos de esta clase están copiados de MineDictionary, excepto
 * el método calcularSoporte
 * @author vanesa.graino
 *
 */
public class ParallelMineDictionary extends MineDictionary implements IBusquedaParalelaSecuenciaAnotaciones{
   private static final Logger LOGGER = Logger.getLogger(ParallelMineDictionary.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   private int numHilosPar;
   protected int sidActual = -1;

   /*{
      associationClassName = "ModeloDictionary";
      patternClassName = "PatronDictionaryFinalEvent";
   }*/

   /*
    * Constructores
    */

   public ParallelMineDictionary(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents, IClustering clustering,
         boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
      this.numHilosPar = numHilos;
      associationClassName += "Paralelo";
   }

   /*
    * Métodos
    */

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      int tamActual = candidatas.get(0).size();
      List<Thread> hilos = new ArrayList<Thread>();
      String hiloClassName = "HiloSoporte";
      if(tamActual == 3){
         hiloClassName = "HiloSoporteTam3";
      }else if(tamActual > 3){
         hiloClassName = "HiloSoporteTam4";
         sidActual=-1;
      }

      ParallelHelper.runAndExcuteThreads(hiloClassName, hilos, numHilosPar, coleccion, candidatas, this, tamActual,
            anotaciones.getActual());
      if(tamActual>2){
         anotaciones.guardarAnotaciones();
      }

   }

   @Override
   public int getSiguienteSecuencia(IColeccion coleccion){
      synchronized (coleccion) {
         if(sidActual < coleccion.size()-1){
            return ++sidActual;
         }
      }
      return -1;
   }

   /* Fachadas para creacion de nodos en arbol */

   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo, Supernodo supernodo, String tipo){
      Nodo n = new NodoHilos(modelo, supernodo);
      supernodo.addNodo(n, tipo);
      return n;
   }

   @Override
   public Nodo creaNodoFachada(IAsociacionTemporal modelo){
      return new NodoHilos(modelo);
   }



}

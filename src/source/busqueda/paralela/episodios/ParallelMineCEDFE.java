package source.busqueda.paralela.episodios;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.episodios.MineCEDFE;
import source.busqueda.paralela.IBusquedaParalelaSecuenciaAnotaciones;
import source.busqueda.paralela.ParallelHelper;
import source.evento.IColeccion;
import source.modelo.IAsociacionTemporal;
import source.modelo.arbol.Nodo;
import source.modelo.arbol.NodoHilos;
import source.modelo.arbol.Supernodo;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/**
 * Versión paralela de HSTP que admite episodios.
 * Se hace una paralelización estilo Map-Reduce en la que cada hilo tiene su copia de las
 * asociaciones temporales. Los hilos se reparten las secuencias y actualizan sus instancias
 * de patrones temporales. Cuando finaliza el cálculo de soporte, estas asociaciones clonadas
 * se agregan en la asociación temporal original.
 * @author vanesa.graino
 *
 */
public class ParallelMineCEDFE extends MineCEDFE implements IBusquedaParalelaSecuenciaAnotaciones{
   private static final Logger LOGGER = Logger.getLogger(ParallelMineCEDFE.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */
   protected int sidActual = -1;
   private int numHilosPar;


   public ParallelMineCEDFE(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveAllAnnotations, saveRemovedEvents, clustering, removePatterns);
      this.numHilosPar = numHilos;
      associationClassName += "Paralelo";
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      if(candidatas.isEmpty()){
         anotaciones.guardarAnotaciones();
         return;
      }
      int tamActual = candidatas.get(0).size();
      LOGGER.info("Tam: " + tamActual);
      sidActual=-1;

      List<Thread> hilos = new ArrayList<Thread>();
      String hiloClassName = "HiloSoporte";
      if(tamActual == 3){
         hiloClassName = "HiloSoporteTam3";
      }else if(tamActual == 4){
         hiloClassName = "HiloSoporteTam4Episodios";
      }else if(tamActual > 4){
         hiloClassName = "HiloSoporteTam5";
      }

      ParallelHelper.runAndExcuteThreads(hiloClassName, hilos, numHilosPar, coleccion,
            candidatas, this, tamActual, anotaciones.getActual());
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

   @Override
   public List<String> posiblesTiposParaAmpliar(List<Patron> ventanaActual, List<String> tiposAmpliar){
      return listaTipos;
   }

   /* Fachadas para creación de nodos */

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

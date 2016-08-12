package source.busqueda.paralela.semilla;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.paralela.IBusquedaParalelaSecuencia;
import source.busqueda.paralela.ParallelHelper;
import source.busqueda.semilla.SemillaConjuncion;
import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;
/**
 * Se paralelizar en las secuencias en lugar de en los modelos para este caso de la semilla
 * Tiene como ventaja que se puede seguir haciendo interval marking con los candidatos
 * y como contra que hay que agregar los resultados
 *
 * Cada hilo incluye los correspondientes eventos eliminados de esta iteracion
 * utilizando el método sincronizado addEventosEliminados
 *
 * TODO: no se está parelelizando el cálculo de la semilla.
*/
public class ParallelSemillaConjuncion extends SemillaConjuncion implements IBusquedaParalelaSecuencia {
   private static final Logger LOGGER = Logger.getLogger(ParallelSemillaConjuncion.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos
    */

   private int numHilosPar;
   protected int sidActual = -1, borrados, restantes;


   /*
    * Constructores
    */

   public ParallelSemillaConjuncion(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns, int numHilos) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
      this.numHilosPar = numHilos;
      associationClassName += "Paralelo";
   }

   /*
    * Métodos
    */

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(
         List<String> tipos, IColeccion coleccion,
         List<ModeloSemilla> semillas, int supmin, int win)
         throws AlgoritmoException {
      sidActual = -1;
      return super.buscarModelosFrecuentes(tipos, coleccion, semillas, supmin, win);
   }

   /** Se paralelizar en las secuencias en lugar de en los modelos para este caso de la semilla
      Tiene como ventaja que se puede seguir haciendo interval marking con los candidatos
      y como contra que hay que agregar los resultados

      Cada hilo incluye los correspondientes eventos eliminados de esta iteracion
      utilizando el método sincronizado addEventosEliminados
   */
   //@Override
   protected void calcularSoporteSemilla(List<IAsociacionTemporal> candidatas, IColeccion coleccion, int ventana){
      borrados=0;
      restantes=0;
      windowSize = ventana;

      List<Thread> hilos = new ArrayList<Thread>();
      ParallelHelper.runAndExcuteThreads("HiloSoporteSemilla", hilos, numHilosPar, coleccion, candidatas, this, 0, null);

      imprimirEliminados(LOGGER, borrados, restantes);
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

   /* El método calcularSoporte es igual al método de ParallelMine */

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      List<Thread> hilos = new ArrayList<Thread>();
      ParallelHelper.runAndExcuteThreads("HiloSoporte", hilos, numHilosPar, coleccion, candidatas, this, 0, null);
   }

}

package source.busqueda.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.busqueda.IBusquedaConEpisodios;
import source.busqueda.Mine;
import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/**
 *   Variante del algoritmo básico que permite trabajar con episodios. Estos episodios se definen identificando
 * un evento de inicio y otro de fin, de forma que una ocurrencia de un evento inicio únicamente se pueda asociar
 * con la siguiente ocurrencia del evento fin que no haya sido ya asociada con un evento inicio, y viceversa.
 * Por ejemplo, si A y B son inicio y fin de episodio, y la ventana contiene AABB, el primer evento A únicamente
 * puede asociarse con el primer evento B, y el segundo evento A únicamente puede asociarse con el segundo evento
 * A, y viceversa.
 *
 *   Estos episodios se suministran por parámetro al algoritmo de búsqueda y se tendrán en cuenta a partir de la
 * búsqueda de patrones frecuentes de tamaño 2, donde se introducirán en las asociaciones temporales. En las
 * iteraciones posteriores estos episodios se heredan en cada asociación temporal a partir de las asociaciones
 * temporales extendidas.
 *
 * @author Miguel
 */
public class MineEpisodes extends Mine implements IBusquedaConEpisodios{
   private static final Logger LOGGER = Logger.getLogger(MineEpisodes.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }

   /*
    * Atributos propios
    */

   //private Map<String,List<IAsociacionTemporal>> mapa; // ¿Realmente hace falta esto aquí?
   protected List<Episodio> listaEpisodios;

   {
      associationClassName = "ModeloEpisodios";
      patternClassName = "Patron";
   }

   public MineEpisodes(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveRemovedEvents, clustering, removePatterns);
   }

   /*
    * Redefinición de métodos plantilla y métodos propios
    */

   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray)
         throws FactoryInstantiationException {
      // Buscar si algún episodio se aplica a la asociación temporal en curso
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacionAmbos(eps, listaEpisodios, Arrays.asList(modArray));
      // En MineCompleteEpisodes está así
      //EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, mod);
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, eps,
            windowSize, getClustering(), numHilos);
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray,
         List<Patron> patrones, GeneradorPatrones genp)
         throws FactoryInstantiationException {
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(modArray), genp.getAsociacionesBase());
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, eps, windowSize,
            patrones, numHilos);
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         int supmin, int win) {
      return calcularDistribuciones(tipos, coleccion, supmin, win, new ArrayList<Episodio>());
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios) {
      List<List<IAsociacionTemporal>> all = buscarModelosFrecuentes(tipos, coleccion, supmin, win, episodios, true);
      if (all != null && all.size() > 1) {
         return all.get(1);
      }
      return Collections.emptyList();
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win){
      return buscarModelosFrecuentes(tipos, coleccion, supmin, win, new ArrayList<Episodio>(), false);
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios){
         return buscarModelosFrecuentes(tipos, coleccion, supmin, win, episodios, false);
   }

   @Override
   protected void calcularSoporte(List<IAsociacionTemporal> candidatas, IColeccion coleccion){
      if(!candidatas.isEmpty()){
         super.calcularSoporte(candidatas, coleccion);
      }
   }

   private List<List<IAsociacionTemporal>> buscarModelosFrecuentes(List<String> tipos, IColeccion coleccion,
         int supmin, int win, List<Episodio> episodios, boolean hastaNivel2){
      listaEpisodios = new ArrayList<Episodio>();
      listaEpisodios.addAll(episodios);
      return super.buscarModelosFrecuentes(tipos, coleccion, supmin, win, hastaNivel2);
   }

   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos, IColeccion coleccion,
         List<IAsociacionTemporal> modelosBase, int supmin, int win,
         List<Episodio> episodios) throws ModelosBaseNoValidosException{
      listaEpisodios = new ArrayList<Episodio>();
      listaEpisodios.addAll(episodios);
      return reiniciarBusqueda(tipos, coleccion, modelosBase, supmin, win);
   }

   @Override
   public List<Episodio> getListaEpisodios(){
      return listaEpisodios;
   }
}
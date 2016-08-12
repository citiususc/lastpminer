package source.busqueda.negacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.busqueda.IBusquedaConEpisodios;
import source.busqueda.episodios.EpisodiosUtils;
import source.evento.Episodio;
import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.negacion.IAsociacionConNegacion;

/**
 * En esta primera versión de negación con episodios se utiliza
 * la información que aportan los tipos de episodios únicamente para la
 * creación de distribuciones de frecuencia que tengan en cuenta este
 * conocimiento del usuario.
 * La negación seguirá siendo de eventos dentro de la ventana, aunque estos
 * eventos sean fin o inicio de un episodio.
 *
 * Al contrario de lo que se hacía en minería de frecuentes positivos con
 * episodios, ahora no pueden eliminarse de la ventana los eventos de fin de
 * episodio cuando salen sus correspondientes eventos de inicio de la misma.
 * No se puede hacer ya que se estaría modificando la información para las
 * asociaciones temporales que nieguen tipos eventos de episodios.
 *
 *
 * @author vanesa.graino
 *
 */
public class NegacionMineEpisodios extends NegacionMine implements IBusquedaConEpisodios{

   /*
    * Atributos
    */

   protected List<Episodio> listaEpisodios;

   /*
    * Constructores
    */

   public NegacionMineEpisodios(String executionId,
         boolean savePatternInstances, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveRemovedEvents, clustering,
            removePatterns);
   }

   /*
    * Métodos
    */

   /*
    * (non-Javadoc)
    * @see source.busqueda.IBusquedaConEpisodios#getListaEpisodios()
    */
   @Override
   public List<Episodio> getListaEpisodios() {
      return listaEpisodios;
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(
         List<String> tipos, IColeccion coleccion, int supmin, int win,
         List<Episodio> episodios) throws AlgoritmoException {
      listaEpisodios = episodios;
      return buscarModelosFrecuentes(tipos, coleccion, supmin, win);
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos,
         IColeccion coleccion, int supmin, int win, List<Episodio> episodios)
         throws AlgoritmoException {
      listaEpisodios = episodios;
      return calcularDistribuciones(tipos, coleccion, supmin, win);
   }


   //TODO creación de los modelos adecuados
   @Override
   IAsociacionConNegacion crearModelo(List<String[]> comb) throws FactoryInstantiationException{
      if(comb.get(0).length==2){
         //2 positivos
         List<Episodio> eps = new ArrayList<Episodio>();
         EpisodiosUtils.episodiosAsociacionAmbos(eps, listaEpisodios, Arrays.asList(comb.get(0)));
         return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
               comb.get(0), comb.get(1), eps, windowSize, getClustering(), supermodelo, numHilos);
      }

      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            comb.get(0), comb.get(1), windowSize, getClustering(), supermodelo, numHilos);
   }

   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos,
         IColeccion coleccion, List<IAsociacionTemporal> modelosBase,
         int supmin, int win, List<Episodio> episodios)
         throws ModelosBaseNoValidosException, AlgoritmoException {
      //TODO
      throw new RuntimeException("Método no implementando");
   }

}

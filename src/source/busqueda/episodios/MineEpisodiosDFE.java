package source.busqueda.episodios;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import source.busqueda.GeneradorPatrones;
import source.busqueda.IBusquedaDiccionarioConEpisodios;
import source.busqueda.IEliminaEventos;
import source.busqueda.jerarquia.MineDictionaryFinalEvent;
import source.evento.Episodio;
import source.evento.Evento;
import source.evento.IColeccion;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.ModelosBaseNoValidosException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.patron.Patron;

/**
 * Version HSTP en la que los episodios NO tienen que estar completos para que se
 * busquen sus asociaciones temporales
 * @author vanesa.graino
 *
 */
public class MineEpisodiosDFE extends MineDictionaryFinalEvent implements IBusquedaDiccionarioConEpisodios, IEliminaEventos{

   protected List<Episodio> listaEpisodios;

   {
      associationClassName = "ModeloEpisodiosDFE";
      patternClassName = "PatronDictionaryFinalEvent";
   }

   public MineEpisodiosDFE(String executionId, boolean savePatternInstances,
         boolean saveAllAnnotations, boolean saveRemovedEvents,
         IClustering clustering, boolean removePatterns) {
      super(executionId, savePatternInstances, saveAllAnnotations,
            saveRemovedEvents, clustering, removePatterns);
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray)
         throws FactoryInstantiationException {
      // Buscar si algún episodio se aplica a la asociación temporal en curso
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacionUno(eps, listaEpisodios, Arrays.asList(modArray));
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, modArray, eps, windowSize,
            getClustering(), numHilos);
   }

   @Override
   protected IAsociacionTemporal crearModelo(String[] modArray,
         List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException {
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(modArray), genp.getAsociacionesBase());
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName,
            modArray, eps, windowSize, patrones, numHilos);
   }

   @Override
   public List<List<IAsociacionTemporal>> buscarModelosFrecuentes(
         List<String> tipos, IColeccion coleccion, int supmin, int win,
         List<Episodio> episodios) throws AlgoritmoException {
      listaEpisodios = new ArrayList<Episodio>(episodios);
      return buscarModelosFrecuentes(tipos, coleccion, supmin, win);
   }

   @Override
   public List<IAsociacionTemporal> calcularDistribuciones(List<String> tipos,
         IColeccion coleccion, int supmin, int win,
         List<Episodio> episodios) {
      listaEpisodios = new ArrayList<Episodio>(episodios);
      return calcularDistribuciones(tipos, coleccion, supmin, win);
   }

   @Override
   public List<List<IAsociacionTemporal>> reiniciarBusqueda(List<String> tipos,
         IColeccion coleccion, List<IAsociacionTemporal> modelosBase,
         int supmin, int win, List<Episodio> episodios)
         throws ModelosBaseNoValidosException {
      listaEpisodios = new ArrayList<Episodio>(episodios);
      return super.reiniciarBusqueda(tipos, coleccion, modelosBase, supmin, win);
   }


   @Override
   public List<String> posiblesTiposParaAmpliarNoEpisodios(List<Patron> actual,
         List<String> tiposAmpliar, Evento evento) {
      // No se utiliza
      return null;
   }

   @Override
   public List<Episodio> posiblesEpisodiosParaAmpliar(List<Episodio> episodiosAmpliar, Evento evento) {
      // No se utiliza
      return null;
   }

   @Override
   public List<Episodio> getListaEpisodios(){
      return listaEpisodios;
   }

}

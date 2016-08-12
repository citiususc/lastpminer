package source.busqueda.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import source.busqueda.GeneradorPatrones;
import source.busqueda.episodios.EpisodiosUtils;
import source.evento.Episodio;
import source.evento.Evento;
import source.excepciones.FactoryInstantiationException;
import source.modelo.AssociationFactory;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.IClustering;
import source.modelo.condensacion.episodios.SuperModeloEpisodios;
import source.modelo.semilla.ModeloSemilla;
import source.patron.Patron;


//TODO implementar con Semilla
public class SemillaConjuncionCEDFESuperModelo extends SemillaConjuncionCEDFE{
   private static final Logger LOGGER = Logger.getLogger(SemillaConjuncionCEDFESuperModelo.class.getName());
   public static Logger getLogger(){
      return LOGGER;
   }


   /*
    * Atributos propios
    */

   protected SuperModeloEpisodios supermodelo;

   {
      associationClassName = "ModeloDictionaryFinalEvent";
      //patternClassName = "PatronDictionaryFinalEvent";
   }


   /*
    * Constructores
    */

   public SemillaConjuncionCEDFESuperModelo(String executionId, boolean savePatternInstances, boolean saveRemovedEvents,
         boolean saveAllAnnotations, IClustering clustering, boolean removePatterns){
      super(executionId, savePatternInstances, saveRemovedEvents, saveAllAnnotations, clustering, removePatterns);
   }

   /*
    * Métodos propios
    */

   @Override
   protected void inicializaEstructuras(List<String> tipos, List<IAsociacionTemporal> candidatos,
         int win, String[] tiposSemilla, List<ModeloSemilla> semillas,
         List<List<IAsociacionTemporal>> semNivel, int cSize) throws FactoryInstantiationException{
      super.inicializaEstructuras(tipos, candidatos, win, tiposSemilla, semillas, semNivel, cSize);
      supermodelo = new SuperModeloEpisodios(tipos.toArray(new String[tipos.size()]), listaEpisodios, win);
   }

   //Tamaño 2
   /*@Override
   protected IAsociacionTemporal crearModelo(List<String> mod, List<Episodio> eps) throws FactoryInstantiationException{
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod,
            eps, windowSize, isSavePatternInstances(), getClustering(), supermodelo, numHilos);
   }*/

   //Otros tamaños
   @Override
   protected IAsociacionTemporal crearModelo(String[] mod, List<Patron> patrones, GeneradorPatrones genp) throws FactoryInstantiationException{
      List<Episodio> eps = new ArrayList<Episodio>();
      EpisodiosUtils.episodiosAsociacion(eps, Arrays.asList(mod), genp.getAsociacionesBase());
      return AssociationFactory.getInstance().getAssociationInstance(associationClassName, mod, windowSize,
            patrones, eps, supermodelo, numHilos);
   }

   @Override
   public List<String> posiblesTiposParaAmpliar(List<Patron> actual, List<String> tiposAmpliar){
      return supermodelo.eventosActivos(tiposAmpliar);
   }

   //@Override
   public List<String> posiblesTiposParaAmpliarNoEpisodios(List<Patron> actual, List<String> tiposAmpliar, Evento evento){
      return supermodelo.eventosActivosNoEpisodios(tiposAmpliar, evento);
   }

   //@Override
   public List<Episodio> posiblesEpisodiosParaAmpliar(List<Episodio> episodiosAmpliar, Evento evento){
      return supermodelo.episodiosActivos(episodiosAmpliar, true);
   }

}

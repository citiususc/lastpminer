package source.busqueda.semilla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.busqueda.paralela.semilla.ParallelSemillaConjuncion;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.Evento;
import source.evento.IColeccion;
import source.evento.ISecuencia;
import source.evento.SecuenciaSimple;
import source.excepciones.AlgoritmoException;
import source.excepciones.FactoryInstantiationException;
import source.excepciones.SemillasNoValidasException;
import source.modelo.IAsociacionTemporal;
import source.modelo.clustering.ClusteringFactory;
import source.modelo.clustering.IClustering;
import source.modelo.semilla.ModeloSemilla;
import source.patron.Patron;
import source.patron.PatronSemilla;
import source.restriccion.RIntervalo;

public class ParallelSemillaConjuncionTest {

   @Test
   public void testParallelSeedSAHSCorto(){
      Patron.setPrintID(false);
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.coleccion = new ColeccionSimple(Arrays.asList(capsula.coleccion.get(1)));
      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
      capsula.params.setMode(Modes.MODE_SEED);
      capsula.mineria();
   }

   @Test
   public void testParallelSeedCorto() throws AlgoritmoException{
      ConfigurationParameters params = new ConfigurationParameters();
      params.setWindowSize(6);
      params.setSavePatternInstances(false);
      params.setSaveRemovedEvents(false);
      params.setMinFreq(1);
      params.setAlgorithm(Algorithms.ALG_PAR);
      params.setMode(Modes.MODE_SEED);
      IClustering clustering = ClusteringFactory.getClustering(params.getClusteringClassName());

      ParallelSemillaConjuncion mine = new ParallelSemillaConjuncion(params.getExecutionId(),
            params.isSavePatternInstances(), params.isSaveRemovedEvents(), clustering, !params.isCompleteResult(),
            ConfigurationParameters.NUM_THREADS);

      List<String> tiposColeccion = Arrays.asList("A", "B", "C");
      IColeccion coleccion = new ColeccionSimple(new ArrayList<ISecuencia>(1));
      ISecuencia secuencia = new SecuenciaSimple();
      secuencia.add(new Evento("A", 22));
      secuencia.add(new Evento("B", 23));
      secuencia.add(new Evento("A", 25));
      secuencia.add(new Evento("C", 27));
      secuencia.add(new Evento("B", 28));
      secuencia.add(new Evento("B", 29));
      coleccion.add(secuencia);

      //Patrón semilla
      String[] tiposSemilla = new String[]{"A", "B"};
      List<RIntervalo> rests = Arrays.asList(new RIntervalo("A","B",0,3));
      PatronSemilla semilla = new PatronSemilla(tiposSemilla, rests, params.isSavePatternInstances());

      List<Patron> patrones = new ArrayList<Patron>();
      List<ModeloSemilla> semillas = new ArrayList<ModeloSemilla>();
      patrones.add(semilla);
      //ModeloSemilla mod = new ModeloSemilla(tiposSemilla, ventana, patrones, ConfigurationParameters.savePatternInstances, clustering);
      ModeloSemilla mod = new ModeloSemilla(semilla.getTipos(), params.getWindowSize(), patrones, clustering);
      semillas.add(mod);

      try {
         List<List<IAsociacionTemporal>> resultados = mine.buscarModelosFrecuentes(tiposColeccion, coleccion, semillas, params.getMinFreq(), params.getWindowSize());
         SemillaConjuncionTest.printResultados(resultados);

      } catch (SemillasNoValidasException e) {
         e.printStackTrace();
      }


   }

   /**
    * TODO
    * Comprueba que el método de clonado funciona correctamente, es decir,
    * al cambiar algo en la copia no se modifica el original
    * @throws FactoryInstantiationException
    */
   @Test public void testColisionMapa() throws FactoryInstantiationException{
      int win = 10;
      ParallelSemillaConjuncion mine = new ParallelSemillaConjuncion("asdf", true, true,
            null, false, ConfigurationParameters.NUM_THREADS);
      List<IAsociacionTemporal> candidatos = new ArrayList<IAsociacionTemporal>();
      List<String> tiposColeccion = new ArrayList<String>(Arrays.asList("A", "B", "C"));
      List<List<IAsociacionTemporal>> semNivel = new ArrayList<List<IAsociacionTemporal>>();

      String[] tiposSemilla = new String[]{"A", "B", "C"};
      PatronSemilla ps = new PatronSemilla(tiposSemilla, Arrays.asList(new RIntervalo("A","B",0,5)), true);
      ModeloSemilla ms = new ModeloSemilla(tiposSemilla, win, new ArrayList<Patron>(Arrays.asList(ps)), null);

      mine.inicializaEstructuras(tiposColeccion, candidatos, win, tiposSemilla, Arrays.asList(ms), semNivel, 0);

      //Map<String, List<IAsociacionTemporal>> copia = mine.copiaMapa(3);
   }
}

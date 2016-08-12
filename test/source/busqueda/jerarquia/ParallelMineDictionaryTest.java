package source.busqueda.jerarquia;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.ComparacionPatrones;
import source.Principal;
import source.PrincipalTest;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.evento.ColeccionSimple;
import source.evento.IColeccion;
import source.patron.PatronDictionaryFinalEvent;

public class ParallelMineDictionaryTest {
   private static final Logger LOGGER = Logger.getLogger(ParallelMineDictionaryTest.class.getName());

   @Test public void testBDRoE15(){
      //Patron.setPrintID(false);
      AllThatYouNeed capsulaHstp = new AllThatYouNeedSinteticas("BDRoE15");
      capsulaHstp.params.setWindowSize(4);
      capsulaHstp.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsulaHstp.params.setMode(Modes.MODE_BASIC);
      capsulaHstp.params.setMinFreq(300);
      capsulaHstp.params.setSavePatternInstances(true);
      capsulaHstp.coleccion = new ColeccionSimple(Arrays.asList(capsulaHstp.coleccion.get(0), capsulaHstp.coleccion.get(1)));
      LOGGER.info("No se utiliza toda la colección!!!!");

      capsulaHstp.params.setTamMaximoPatron(3);
      LOGGER.info("El tamaño máximo de patron a buscar es: " + capsulaHstp.params.getTamMaximoPatron());

      IColeccion copiaCollecion = capsulaHstp.coleccion.clone();
      IColeccion coleccionOriginal = capsulaHstp.coleccion.clone();

      AllThatYouNeed capsulaHpar = new AllThatYouNeed(copiaCollecion, new ArrayList<String>(capsulaHstp.tipos),
            capsulaHstp.episodios, capsulaHstp.ocurrenciasEpisodios, "BDRoE15");
      capsulaHpar.params = capsulaHstp.params.clonar();
      capsulaHpar.params.setAlgorithm(Algorithms.ALG_HPAR);

      String fileHstp = capsulaHstp.mineria();
      //Patron.resetGenerator();

      String fileHpar = capsulaHpar.mineria();
      LOGGER.info("Ficheros de hstp e hpar respectivamente:\n" + fileHstp + "\n"+fileHpar);

      //PrincipalTest.validarResultados(capsula.resultados, copiaCollecion);

      PatronDictionaryFinalEvent p1 = (PatronDictionaryFinalEvent)PrincipalTest.getPatron(capsulaHpar.resultados, 3, 56);
      //PrincipalTest.ocurrenciaFalta(p1, coleccionOriginal);
      //PrincipalTest.ocurrenciasRepetidas(p1);

      PatronDictionaryFinalEvent p2 = (PatronDictionaryFinalEvent)PrincipalTest.getPatron(capsulaHstp.resultados, 3, 56);

      ComparacionPatrones comp = ComparacionPatrones.comparaPatrones(p1, p2);
      PrincipalTest.ocurrenciaFalta(p1, comp.getSoloA(), coleccionOriginal);
      PrincipalTest.ocurrenciaFalta(p2, comp.getSoloB(), coleccionOriginal);

      LOGGER.info("Comparacion patrones: " + comp);

      //Comparacion comp = AbstractMine.compararResultados(capsulaHstp.resultados, capsulaHpar.resultados);
      //LOGGER.info(comp.toString(false));
      //assertTrue(comp.sonIguales());
      assertTrue(Principal.compararFicheros(fileHstp, fileHpar));
   }

}

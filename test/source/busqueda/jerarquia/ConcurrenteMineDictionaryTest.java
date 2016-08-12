package source.busqueda.jerarquia;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Ignore;
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
import source.patron.Patron;

public class ConcurrenteMineDictionaryTest {

   @Ignore("Mismo resultado")
   @Test public void testBDRoE15Corto(){
      //Patron.setPrintID(false);
      AllThatYouNeed capsulaHstp = new AllThatYouNeedSinteticas("BDRoE15");
      capsulaHstp.params.setAlgorithm(Algorithms.ALG_HSTP);
      capsulaHstp.params.setMode(Modes.MODE_BASIC);
      capsulaHstp.params.setMinFreq(300);
      capsulaHstp.params.setWindowSize(4);
      capsulaHstp.params.setSavePatternInstances(true);
      capsulaHstp.coleccion = new ColeccionSimple(Arrays.asList(capsulaHstp.coleccion.get(0), capsulaHstp.coleccion.get(1)));
      System.out.println("No se utiliza toda la colección!!!!");

      capsulaHstp.params.setTamMaximoPatron(5);
      System.out.println("El tamaño máximo de patron a buscar es: " + capsulaHstp.params.getTamMaximoPatron());

      IColeccion copiaCollecion = capsulaHstp.coleccion.clone();
      IColeccion coleccionOriginal = capsulaHstp.coleccion.clone();

      AllThatYouNeed capsulaHcon = new AllThatYouNeed(copiaCollecion, new ArrayList<String>(capsulaHstp.tipos),
            capsulaHstp.episodios, capsulaHstp.ocurrenciasEpisodios, "BDRoE15");

      capsulaHcon.params = capsulaHstp.params.clonar();
      capsulaHcon.params.setAlgorithm(Algorithms.ALG_HCON);

      String fileHstp = capsulaHstp.mineria();
      //Patron.resetGenerator();
      String fileHcon = capsulaHcon.mineria();
      System.out.println("Ficheros de hstp e hpar respectivamente:\n" + fileHstp + "\n"+fileHcon);

      //PrincipalTest.validarResultados(capsula.resultados, copiaCollecion);

      int tam = 5, id=2316;
      Patron pHstp = PrincipalTest.getPatron(capsulaHstp.resultados, tam, id);
      //PrincipalTest.ocurrenciaFalta(pHstp, coleccionOriginal);
      //PrincipalTest.ocurrenciasRepetidas(pHstp);

      Patron pHcon = PrincipalTest.getPatron(capsulaHcon.resultados, tam, id);
      PrincipalTest.ocurrenciaFalta(pHcon, coleccionOriginal);
      PrincipalTest.ocurrenciasRepetidas(pHcon);

      ComparacionPatrones comp = ComparacionPatrones.comparaPatrones(pHstp, pHcon);
      PrincipalTest.ocurrenciaFalta(pHcon, comp.getSoloB(), coleccionOriginal);

      System.out.println("Comparacion patrones: " + comp);

      //Comparacion comp = AbstractMine.compararResultados(capsulaHstp.resultados, capsulaHpar.resultados);
      //System.out.println(comp.toString(false));
      //assertTrue(comp.sonIguales());
      assertTrue(Principal.compararFicheros(fileHstp, fileHcon));
   }

}

package source.busqueda.episodios;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.ComparacionPatrones;
import source.Principal;
import source.PrincipalTest;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.patron.Patron;

public class ConcurrenteMineCEDFETest {

   @Test public void testEpisodesBD4Corto(){
      //Patron.setPrintID(false);
      AllThatYouNeed capsulaCon = new AllThatYouNeedSinteticas("BD4");
      capsulaCon.params.setAlgorithm(Algorithms.ALG_CON);
      capsulaCon.params.setMode(Modes.MODE_EPISODE);
      capsulaCon.params.setMinFreq(300);
      capsulaCon.params.setSavePatternInstances(true);
      capsulaCon.params.setTamMaximoPatron(5);
      String fileCon = capsulaCon.mineria();

      //Patron.resetGenerator();
      AllThatYouNeed capsulaHcon = new AllThatYouNeedSinteticas("BD4");
      capsulaHcon.params.setAlgorithm(Algorithms.ALG_HCON);
      capsulaHcon.params.setMode(Modes.MODE_EPISODE);
      capsulaHcon.params.setMinFreq(300);
      capsulaHcon.params.setSavePatternInstances(true);
      capsulaHcon.params.setTamMaximoPatron(5);
      String fileHcon = capsulaHcon.mineria();

      int id=3551,tam=5;
      Patron patronCon = PrincipalTest.getPatron(capsulaCon.resultados, tam, id);
      PrincipalTest.ocurrenciaFalta(patronCon, capsulaHcon.coleccion);
      PrincipalTest.ocurrenciasRepetidas(patronCon);

      Patron patronHcon = PrincipalTest.getPatron(capsulaHcon.resultados, tam, id);
      ComparacionPatrones comp = ComparacionPatrones.comparaPatrones(patronCon, patronHcon);
      System.out.println("Comparacion patrones: " + comp);

      boolean sonIguales = Principal.compararFicheros(fileCon, fileHcon);
      if(sonIguales){ //borrar el fichero
         new File(fileHcon).delete();
      }
      assertTrue(sonIguales);
   }

}

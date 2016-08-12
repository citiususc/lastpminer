package source.busqueda.episodios;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSAHS;
import source.AllThatYouNeedSinteticas;
import source.configuracion.Algorithms;
import source.configuracion.ExecutionParameters;
import source.configuracion.Modes;
import source.excepciones.SemillasNoValidasException;
import source.patron.Patron;


public class MineAnotacionesEpisodiosTest {


   @Test public void testSAHS() throws SemillasNoValidasException{
      Patron.setPrintID(false);
      AllThatYouNeed capsula = new AllThatYouNeedSAHS();
      capsula.params.setAlgorithm(Algorithms.ALG_ANOT);
      capsula.params.setWindowSize(80);
      capsula.params.setMode(Modes.MODE_EPISODE);
      capsula.params.setValidate(true);
      System.out.println("Parametros:\n"+capsula.params);
      boolean valido = capsula.mineriaFicheros(0);
      if(valido){ //borrar el fichero
         new File(capsula.filePatrones).delete();
      }
      assertTrue(valido);
      System.out.println("Fichero estadísticas: " + capsula.fileEstadisticas);
   }

   @Test public void testSinteticas() throws SemillasNoValidasException{
      Patron.setPrintID(false);
      //for(int i=0; i<ConfigurationParameters.BBDDS.length-2; i++){
      for(int i : new int[]{3}){
         AllThatYouNeed capsula = new AllThatYouNeedSinteticas(ExecutionParameters.BBDD[i].nombre);
         int[] winsCol = ExecutionParameters.BBDD[i].windows;
         capsula.params.setWindowSize(winsCol[winsCol.length-1]);
         capsula.params.setAlgorithm(Algorithms.ALG_ANOT);
         capsula.params.setMode(ExecutionParameters.BBDD[i].modo);
         capsula.params.setValidate(true);
         System.out.println("Parametros:\n"+capsula.params);
         boolean valido = capsula.mineriaFicheros(0);
         if(valido){ //borrar el fichero
            new File(capsula.filePatrones).delete();
         }
         assertTrue(valido);
         System.out.println("Fichero estadísticas: " + capsula.fileEstadisticas);
      }
   }
}

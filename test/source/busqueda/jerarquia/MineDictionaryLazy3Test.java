package source.busqueda.jerarquia;

import static source.PrincipalTestGeneral.PASADO;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.Principal;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.patron.Patron;

public class MineDictionaryLazy3Test {

   @Test
   public void testGeneral(){
      Collection<Object[]> data =Arrays.asList(new Object[][] {
         {"apnea", 80, false}, //0
         {"BDRoE6", 20, false}, //1
         {"BDRoE15", 2, PASADO}, //2
      });
      for(Object[] d : data){
         PrincipalTestGeneral test = new PrincipalTestGeneral(Algorithms.ALG_LAZY, Modes.MODE_BASIC, (String)d[2], (Integer)d[3], (Boolean)d[4]);
         test.test();
      }

   }

   @Test public void testFrecuencia(){
      Algorithms algorithm = Algorithms.ALG_LAZY;
      Modes mode = Modes.MODE_BASIC;
      Integer window = 20;
      String collection = "BDRoE6";

      Patron.setPrintID(false);
      AllThatYouNeed capsula = new AllThatYouNeedSinteticas(collection);
      capsula.params.setAlgorithm(algorithm);
      capsula.params.setWindowSize(window);
      capsula.params.setMode(mode);
      capsula.params.setSavePatternInstances(true);
      //capsula.params.setTamMaximoPatron(4);
      String fileLazy = capsula.mineria();

      AllThatYouNeed capsula2 = new AllThatYouNeedSinteticas(collection);
      capsula2.params = capsula.params;
      capsula2.params.setAlgorithm(Algorithms.ALG_HSTP);
      String fileHstp = capsula2.mineria();

      boolean sonIguales =  Principal.compararFicheros(fileHstp, fileLazy);
      if(sonIguales){ //borrar el fichero
         new File(fileLazy).delete();
      }
      Assert.assertTrue("Falla porque... ",sonIguales);
   }


}

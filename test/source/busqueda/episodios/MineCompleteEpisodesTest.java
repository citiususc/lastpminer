package source.busqueda.episodios;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.AllThatYouNeed;
import source.AllThatYouNeedSinteticas;
import source.PrincipalTest;
import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;
import source.modelo.IAsociacionTemporal;
import source.patron.Patron;

//public class MineCompleteEpisodesTest {

//   @Test
//   public void testFalloFrecuenciaTam2(){
//      Patron.setPrintID(false);
//      AllThatYouNeed capsula = new AllThatYouNeedSinteticas("BD4");
//      capsula.params.setWindowSize(20);
//      capsula.params.setAlgorithm(Algorithms.ALG_ASTP);
//      capsula.params.setMode(Modes.MODE_EPISODE);
//      capsula.params.setTamMaximoPatron(3);
//      capsula.mineria();
//      /*String file = capsula.mineria();
//      String referencia = Principal.getFicheroValidacion(capsula.params);
//      boolean sonIguales =  Principal.compararFicheros(referencia, file);
//      if(sonIguales){ //borrar el fichero
//         new File(file).delete();
//      }
//      Assert.assertTrue("Falla para BD4 con ASTP y modo episodios",sonIguales);*/
//      //List<IAsociacionTemporal> resultadosTam2 = capsula.resultados.get(0);
//      //Buscar b1, f1
//      IAsociacionTemporal b1f1 = PrincipalTest.getAsociacion(capsula.resultados, new String[]{"b1","f1"});
//      System.out.println("b1f1: " + b1f1);
//      Assert.assertEquals(12000, b1f1.getSoporte());
//   }
//}


@RunWith(Parameterized.class)
public class MineCompleteEpisodesTest extends PrincipalTestGeneral{
   @Parameters
   public static Collection<Object[]> data(){
      boolean PASADO = false;
      return Arrays.asList(new Object[][] {
            {"apnea", 80, PASADO}, //0

            {"BD4", 20, PASADO}, //1
            {"BD4", 40, PASADO}, //2
            {"BD4", 60, PASADO}, //3
            {"BD4", 80, PASADO}, //4
            {"BD4", 100, PASADO}, //5
            {"BD4", 120, PASADO}, //6

            {"BD5", 20, PASADO}, //7
            {"BD5", 40, PASADO}, //8
            {"BD5", 60, PASADO}, //9
            {"BD5", 80, PASADO}, //10

            {"BD6", 20, PASADO}, //11
            {"BD6", 40, PASADO}, //12
            {"BD6", 60, PASADO}, //13
            {"BD6", 80, IGNORAR}, //14

            {"BD7", 20, PASADO}, //15
            {"BD7", 40, PASADO}, //16
            {"BD7", 60, IGNORAR}, //17
            {"BD7", 80, IGNORAR}, //18

            {"BDR56", 20, PASADO},//19
            {"BDR56", 40, PASADO},//20
            {"BDR56", 60, PASADO},//21
            {"BDR56", 80, PASADO},//22

            {"BDR57", 20, PASADO},//23
            {"BDR57", 40, PASADO},//24
            {"BDR57", 60, IGNORAR},//25
            {"BDR57", 80, IGNORAR},//26

            {"BDRoG2-100", 5, PASADO},//27
            {"BDRoG2-100", 10, PASADO},//28
      });
   }

   public MineCompleteEpisodesTest(String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_ASTP, Modes.MODE_EPISODE, collection, window, skip);
      //this.tamMaximoPatron = 3; //TODO borrar
      //this.savePatternInstances = true;
      //this.writeStatistics = true;
   }
}


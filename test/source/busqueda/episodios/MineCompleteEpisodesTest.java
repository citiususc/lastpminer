package source.busqueda.episodios;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;




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


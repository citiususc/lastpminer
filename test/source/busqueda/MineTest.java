package source.busqueda;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class MineTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      boolean PASADO = false;
      return Arrays.asList(new Object[][] {
            {"apnea", 80, false, PASADO}, //0
            {"BDRoE6", 20, false, PASADO}, //0
            {"BDRoE15", 2, false, PASADO}, //0
      });
   }

   public MineTest(String collection, Integer window, boolean per, boolean skip) {
      super(Algorithms.ALG_ASTP,  Modes.MODE_BASIC, collection, window, skip);
      //this.tamMaximoPatron = 4; //TODO borrar
      //this.savePatternInstances = true;
      if(per){
         this.currentPercentage = 0.2;
         this.maximumPercentage = 0.4;
      }
      //this.compararConFichero = false;
      //this.writeStatistics = true;
   }
}
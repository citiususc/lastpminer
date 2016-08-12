package source.busqueda.jerarquia;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class MineAhorroExpressTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {"apnea", 80, PASADO}, //0			07/09/2015
            {"BDRoE6", 20, PASADO}, //1		07/09/2015
            {"BDRoE6", 40, PASADO}, //2		07/09/2015
            {"BDRoE15", 2, PASADO}, //3		07/09/2015
            {"BDRoE15", 4, PASADO}, //4		07/09/2015
            {"BDRoE15", 6, IGNORAR}, //5
            {"BDRoE15", 8, IGNORAR}, //6
            {"BDRoE15", 10, IGNORAR}, //7

      });
   }

   public MineAhorroExpressTest(String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_SAVEXP, Modes.MODE_BASIC, collection, window, skip);
   }

}

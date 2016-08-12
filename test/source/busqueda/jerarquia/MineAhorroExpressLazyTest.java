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
public class MineAhorroExpressLazyTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {"apnea", 80, false}, //0
            {"BDRoE6", 20, false}, //1
            {"BDRoE15", 2, false}, //1
      });
   }

   public MineAhorroExpressLazyTest(String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_LESS, Modes.MODE_BASIC, collection, window, skip);
   }


}

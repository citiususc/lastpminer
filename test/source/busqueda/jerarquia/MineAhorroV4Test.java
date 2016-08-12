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
public class MineAhorroV4Test extends PrincipalTestGeneral{


   @Parameters
   public static Collection<Object[]> data(){
      //boolean PASADO = false;
      return Arrays.asList(new Object[][] {
         {Algorithms.ALG_SAV4, Modes.MODE_BASIC, "apnea", 80, PASADO},
         {Algorithms.ALG_SAV4, Modes.MODE_BASIC, "BDRoE6", 20, PASADO},
         {Algorithms.ALG_SAV4, Modes.MODE_BASIC, "BDRoE15", 2, PASADO},
      });

   }

   public MineAhorroV4Test(Algorithms algorithm, Modes mode, String collection,
         Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
   }
}

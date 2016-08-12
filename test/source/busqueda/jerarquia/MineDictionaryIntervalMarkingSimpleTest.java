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
public class MineDictionaryIntervalMarkingSimpleTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {"apnea", 80, PASADO}, //0
            {"BDRoE15", 2, PASADO}, //1
      });
   }

   public MineDictionaryIntervalMarkingSimpleTest(String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_IM2, Modes.MODE_BASIC, collection, window, skip);
   }
}

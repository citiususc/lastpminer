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
public class MineDictionarySuperModeloESTest extends PrincipalTestGeneral{
private static boolean PASADO = false;
   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {"apnea", 80, PASADO}, //0
            {"BDRoE6", 20, PASADO}, //1
            {"BDRoE6", 40, PASADO}, //2
            {"BDRoE15", 2, PASADO}, //3
      });
   }

   public MineDictionarySuperModeloESTest(Integer algorithm, Integer mode,
         String collection, Integer window, boolean skip) {
      super(Algorithms.ALG_SMSAVEXP, Modes.MODE_BASIC, collection, window, skip);
   }

}
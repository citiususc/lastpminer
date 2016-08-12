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
public class MineSuperModeloTest extends PrincipalTestGeneral{

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            {Algorithms.ALG_SASTP, Modes.MODE_BASIC, "apnea", 80, PASADO}, //0
      });
   }

   public MineSuperModeloTest(Algorithms algorithm, Modes mode,
         String collection, Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
   }

}
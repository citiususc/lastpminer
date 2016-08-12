package source;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.configuracion.Algorithms;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class PrincipalTestIncompletos extends PrincipalTestGeneral {

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            //astp
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "BDRoE6", 20, false},
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, "BDRoG2-100", 10, false},
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, "BDRoE6", 20, IGNORAR},
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, "BDRoG2-100", 10, false},


      });
   }

   public PrincipalTestIncompletos(Algorithms algorithm, Modes mode,
         String collection, Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
      this.completeResult = false;
   }

}

package source.busqueda;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import source.PrincipalTestGeneral;
import source.configuracion.Algorithms;
import source.configuracion.ConfigurationParameters;
import source.configuracion.Modes;

@RunWith(Parameterized.class)
public class TestVersionesConBorrado  extends PrincipalTestGeneral {

   @Parameters
   public static Collection<Object[]> data(){
      return Arrays.asList(new Object[][] {
            //astp
            {Algorithms.ALG_ASTP, Modes.MODE_BASIC, ConfigurationParameters.APNEA_DB, 20, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_BASIC, ConfigurationParameters.APNEA_DB, 20, PASADO},
            {Algorithms.ALG_SMEXP, Modes.MODE_BASIC, ConfigurationParameters.APNEA_DB, 20, IGNORAR},

            {Algorithms.ALG_ASTP, Modes.MODE_EPISODE, ConfigurationParameters.APNEA_DB, 20, PASADO},
            {Algorithms.ALG_HSTP, Modes.MODE_EPISODE, ConfigurationParameters.APNEA_DB, 20, PASADO},
            {Algorithms.ALG_SUPER, Modes.MODE_EPISODE, ConfigurationParameters.APNEA_DB, 20, PASADO},
            {Algorithms.ALG_MARKT, Modes.MODE_EPISODE, ConfigurationParameters.APNEA_DB, 20, false},
      });
   }

   public TestVersionesConBorrado(Algorithms algorithm, Modes mode, String collection, Integer window, boolean skip) {
      super(algorithm, mode, collection, window, skip);
      this.completeResult = false;
   }

}
